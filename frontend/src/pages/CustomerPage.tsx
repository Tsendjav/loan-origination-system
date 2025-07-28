import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth, PERMISSIONS } from '../contexts/AuthContext';
import { Customer, CustomerSearchParams, CustomerStatus, KYCStatus, customerService, customerUtils } from '../services/customerService';
import CustomerForm from '../components/customer/CustomerForm';
import { showToast } from '../components/layout/MainLayout';

const CustomerPage: React.FC = () => {
  const { hasPermission } = useAuth();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [formMode, setFormMode] = useState<'create' | 'edit' | 'view'>('create');
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  
  // Search and filter state
  const [searchFilters, setSearchFilters] = useState<CustomerSearchParams>({
    query: searchParams.get('query') || '',
    status: (searchParams.get('status') as CustomerStatus) || undefined,
    kycStatus: (searchParams.get('kycStatus') as KYCStatus) || undefined,
    page: parseInt(searchParams.get('page') || '0'),
    size: parseInt(searchParams.get('size') || '20'),
    sort: searchParams.get('sort') || 'lastUpdated',
    direction: (searchParams.get('direction') as 'ASC' | 'DESC') || 'DESC',
  });

  // Load customers
  useEffect(() => {
    loadCustomers();
  }, [searchFilters]);

  // Update URL params when filters change
  useEffect(() => {
    const params = new URLSearchParams();
    Object.entries(searchFilters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        params.set(key, value.toString());
      }
    });
    setSearchParams(params);
  }, [searchFilters, setSearchParams]);

  const loadCustomers = async () => {
    if (!hasPermission(PERMISSIONS.CUSTOMER_VIEW)) {
      showToast({
        message: 'Харилцагчийн мэдээлэл харах эрх байхгүй',
        type: 'error',
      });
      return;
    }

    setIsLoading(true);
    try {
      const result = await customerService.getCustomers(searchFilters);
      setCustomers(result.content);
      setTotalPages(result.totalPages);
      setTotalElements(result.totalElements);
    } catch (error: any) {
      console.error('Error loading customers:', error);
      showToast({
        message: 'Харилцагчийн мэдээлэл ачаалахад алдаа гарлаа',
        type: 'error',
      });
    } finally {
      setIsLoading(false);
    }
  };

  // Handle search
  const handleSearch = (query: string) => {
    setSearchFilters(prev => ({
      ...prev,
      query,
      page: 0,
    }));
  };

  // Handle filter change
  const handleFilterChange = (key: keyof CustomerSearchParams, value: any) => {
    setSearchFilters(prev => ({
      ...prev,
      [key]: value,
      page: 0,
    }));
  };

  // Handle pagination
  const handlePageChange = (page: number) => {
    setSearchFilters(prev => ({ ...prev, page }));
  };

  // Handle sort
  const handleSort = (sortField: string) => {
    setSearchFilters(prev => ({
      ...prev,
      sort: sortField,
      direction: prev.sort === sortField && prev.direction === 'ASC' ? 'DESC' : 'ASC',
      page: 0,
    }));
  };

  // Handle create customer
  const handleCreateCustomer = () => {
    if (!hasPermission(PERMISSIONS.CUSTOMER_CREATE)) {
      showToast({
        message: 'Харилцагч үүсгэх эрх байхгүй',
        type: 'error',
      });
      return;
    }
    setSelectedCustomer(null);
    setFormMode('create');
    setShowForm(true);
  };

  // Handle edit customer
  const handleEditCustomer = (customer: Customer) => {
    if (!hasPermission(PERMISSIONS.CUSTOMER_UPDATE)) {
      showToast({
        message: 'Харилцагчийн мэдээлэл засах эрх байхгүй',
        type: 'error',
      });
      return;
    }
    setSelectedCustomer(customer);
    setFormMode('edit');
    setShowForm(true);
  };

  // Handle view customer
  const handleViewCustomer = (customer: Customer) => {
    setSelectedCustomer(customer);
    setFormMode('view');
    setShowForm(true);
  };

  // Handle delete customer
  const handleDeleteCustomer = async (customer: Customer) => {
    if (!hasPermission(PERMISSIONS.CUSTOMER_DELETE)) {
      showToast({
        message: 'Харилцагч устгах эрх байхгүй',
        type: 'error',
      });
      return;
    }

    if (!window.confirm(`${customerUtils.getFullName(customer)} харилцагчийг устгахдаа итгэлтэй байна уу?`)) {
      return;
    }

    try {
      await customerService.deleteCustomer(customer.id!);
      showToast({
        message: 'Харилцагч амжилттай устгагдлаа',
        type: 'success',
      });
      loadCustomers();
    } catch (error: any) {
      showToast({
        message: error.message || 'Харилцагч устгахад алдаа гарлаа',
        type: 'error',
      });
    }
  };

  // Handle form submit
  const handleFormSubmit = async (customerData: Partial<Customer>) => {
    setIsSubmitting(true);
    try {
      if (formMode === 'create') {
        await customerService.createCustomer(customerData as Omit<Customer, 'id' | 'registrationDate' | 'lastUpdated'>);
      } else if (formMode === 'edit' && selectedCustomer) {
        await customerService.updateCustomer(selectedCustomer.id!, customerData);
      }
      
      setShowForm(false);
      setSelectedCustomer(null);
      loadCustomers();
    } catch (error: any) {
      throw error; // Let CustomerForm handle the error display
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle form cancel
  const handleFormCancel = () => {
    setShowForm(false);
    setSelectedCustomer(null);
  };

  // Export customers
  const handleExport = async () => {
    try {
      const blob = await customerService.exportCustomers(searchFilters);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = `customers-${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      
      showToast({
        message: 'Харилцагчийн жагсаалт амжилттай татагдлаа',
        type: 'success',
      });
    } catch (error: any) {
      showToast({
        message: 'Экспорт хийхэд алдаа гарлаа',
        type: 'error',
      });
    }
  };

  const getSortIcon = (field: string) => {
    if (searchFilters.sort !== field) {
      return (
        <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
        </svg>
      );
    }
    
    return searchFilters.direction === 'ASC' ? (
      <svg className="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4h13M3 8h9m-9 4h6m4 0l4-4m0 0l4 4m-4-4v12" />
      </svg>
    ) : (
      <svg className="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4" />
      </svg>
    );
  };

  // Render customer form modal
  if (showForm) {
    return (
      <CustomerForm
        customer={selectedCustomer || undefined}
        onSubmit={handleFormSubmit}
        onCancel={handleFormCancel}
        isLoading={isSubmitting}
        mode={formMode}
      />
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Харилцагчид</h1>
          <p className="text-gray-600 mt-1">
            Нийт {totalElements.toLocaleString()} харилцагч бүртгэгдсэн
          </p>
        </div>
        
        <div className="flex items-center space-x-3">
          <button
            onClick={handleExport}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            📊 Экспорт
          </button>
          
          {hasPermission(PERMISSIONS.CUSTOMER_CREATE) && (
            <button
              onClick={handleCreateCustomer}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              ➕ Шинэ харилцагч
            </button>
          )}
        </div>
      </div>

      {/* Search and Filters */}
      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Search */}
          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Хайх
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <input
                type="text"
                placeholder="Нэр, и-мэйл, утас..."
                value={searchFilters.query || ''}
                onChange={(e) => handleSearch(e.target.value)}
                className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>

          {/* Status Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Статус
            </label>
            <select
              value={searchFilters.status || ''}
              onChange={(e) => handleFilterChange('status', e.target.value || undefined)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">Бүх статус</option>
              <option value={CustomerStatus.ACTIVE}>Идэвхтэй</option>
              <option value={CustomerStatus.INACTIVE}>Идэвхгүй</option>
              <option value={CustomerStatus.SUSPENDED}>Хязгаарлагдсан</option>
              <option value={CustomerStatus.PENDING_VERIFICATION}>Баталгаажуулалт хүлээгч</option>
            </select>
          </div>

          {/* KYC Status Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              KYC Статус
            </label>
            <select
              value={searchFilters.kycStatus || ''}
              onChange={(e) => handleFilterChange('kycStatus', e.target.value || undefined)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="">Бүх статус</option>
              <option value={KYCStatus.NOT_STARTED}>Эхлээгүй</option>
              <option value={KYCStatus.IN_PROGRESS}>Хийгдэж байгаа</option>
              <option value={KYCStatus.COMPLETED}>Дууссан</option>
              <option value={KYCStatus.REJECTED}>Татгалзсан</option>
              <option value={KYCStatus.EXPIRED}>Хугацаа дууссан</option>
            </select>
          </div>
        </div>
      </div>

      {/* Results */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        {/* Results header */}
        <div className="px-6 py-4 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <p className="text-sm text-gray-700">
              <span className="font-medium">{totalElements.toLocaleString()}</span> харилцагчаас{' '}
              <span className="font-medium">
                {searchFilters.page! * searchFilters.size! + 1}-
                {Math.min((searchFilters.page! + 1) * searchFilters.size!, totalElements)}
              </span>{' '}
              харуулж байна
            </p>
            
            <div className="flex items-center space-x-2">
              <label className="text-sm text-gray-700">Хуудсанд:</label>
              <select
                value={searchFilters.size}
                onChange={(e) => handleFilterChange('size', parseInt(e.target.value))}
                className="px-2 py-1 border border-gray-300 rounded text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
              >
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
                <option value={100}>100</option>
              </select>
            </div>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                  onClick={() => handleSort('firstName')}
                >
                  <div className="flex items-center space-x-1">
                    <span>Нэр</span>
                    {getSortIcon('firstName')}
                  </div>
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                  onClick={() => handleSort('email')}
                >
                  <div className="flex items-center space-x-1">
                    <span>И-мэйл</span>
                    {getSortIcon('email')}
                  </div>
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                  onClick={() => handleSort('phone')}
                >
                  <div className="flex items-center space-x-1">
                    <span>Утас</span>
                    {getSortIcon('phone')}
                  </div>
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  Статус
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                >
                  KYC
                </th>
                <th
                  scope="col"
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer hover:bg-gray-100"
                  onClick={() => handleSort('registrationDate')}
                >
                  <div className="flex items-center space-x-1">
                    <span>Бүртгэсэн огноо</span>
                    {getSortIcon('registrationDate')}
                  </div>
                </th>
                <th scope="col" className="relative px-6 py-3">
                  <span className="sr-only">Үйлдэл</span>
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {isLoading ? (
                // Loading skeleton
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="h-8 w-8 bg-gray-200 rounded-full animate-pulse"></div>
                        <div className="ml-4">
                          <div className="h-4 bg-gray-200 rounded w-24 animate-pulse"></div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="h-4 bg-gray-200 rounded w-32 animate-pulse"></div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="h-4 bg-gray-200 rounded w-20 animate-pulse"></div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="h-6 bg-gray-200 rounded-full w-16 animate-pulse"></div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="h-6 bg-gray-200 rounded-full w-16 animate-pulse"></div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="h-4 bg-gray-200 rounded w-20 animate-pulse"></div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="h-4 bg-gray-200 rounded w-16 animate-pulse"></div>
                    </td>
                  </tr>
                ))
              ) : customers.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center">
                    <div className="text-gray-500">
                      <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                      </svg>
                      <p className="text-lg font-medium">Харилцагч олдсонгүй</p>
                      <p className="text-sm text-gray-400 mt-1">
                        {searchFilters.query ? 'Хайлтын үр дүн олдсонгүй' : 'Хараахан харилцагч бүртгэгдээгүй байна'}
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                customers.map((customer) => (
                  <tr key={customer.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="h-8 w-8 bg-blue-100 rounded-full flex items-center justify-center">
                          <span className="text-sm font-medium text-blue-600">
                            {customer.firstName?.charAt(0)}{customer.lastName?.charAt(0)}
                          </span>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">
                            {customerUtils.getFullName(customer)}
                          </div>
                          <div className="text-sm text-gray-500">
                            {customer.customerType}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{customer.email}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">{customer.phone}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        customer.status === CustomerStatus.ACTIVE ? 'bg-green-100 text-green-800' :
                        customer.status === CustomerStatus.INACTIVE ? 'bg-gray-100 text-gray-800' :
                        customer.status === CustomerStatus.SUSPENDED ? 'bg-red-100 text-red-800' :
                        'bg-yellow-100 text-yellow-800'
                      }`}>
                        {customer.status === CustomerStatus.ACTIVE ? 'Идэвхтэй' :
                         customer.status === CustomerStatus.INACTIVE ? 'Идэвхгүй' :
                         customer.status === CustomerStatus.SUSPENDED ? 'Хязгаарлагдсан' :
                         'Баталгаажуулалт хүлээгч'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        customer.kycStatus === KYCStatus.COMPLETED ? 'bg-green-100 text-green-800' :
                        customer.kycStatus === KYCStatus.IN_PROGRESS ? 'bg-blue-100 text-blue-800' :
                        customer.kycStatus === KYCStatus.REJECTED ? 'bg-red-100 text-red-800' :
                        customer.kycStatus === KYCStatus.EXPIRED ? 'bg-orange-100 text-orange-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {customer.kycStatus === KYCStatus.COMPLETED ? 'Дууссан' :
                         customer.kycStatus === KYCStatus.IN_PROGRESS ? 'Хийгдэж байгаа' :
                         customer.kycStatus === KYCStatus.REJECTED ? 'Татгалзсан' :
                         customer.kycStatus === KYCStatus.EXPIRED ? 'Хугацаа дууссан' :
                         'Эхлээгүй'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {customer.registrationDate ? new Date(customer.registrationDate).toLocaleDateString('mn-MN') : '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end space-x-2">
                        <button
                          onClick={() => handleViewCustomer(customer)}
                          className="text-blue-600 hover:text-blue-900"
                          title="Харах"
                        >
                          👁️
                        </button>
                        
                        {hasPermission(PERMISSIONS.CUSTOMER_UPDATE) && (
                          <button
                            onClick={() => handleEditCustomer(customer)}
                            className="text-green-600 hover:text-green-900"
                            title="Засах"
                          >
                            ✏️
                          </button>
                        )}
                        
                        <Link
                          to={`/loans/new?customerId=${customer.id}`}
                          className="text-purple-600 hover:text-purple-900"
                          title="Зээлийн хүсэлт үүсгэх"
                        >
                          💰
                        </Link>
                        
                        {hasPermission(PERMISSIONS.CUSTOMER_DELETE) && (
                          <button
                            onClick={() => handleDeleteCustomer(customer)}
                            className="text-red-600 hover:text-red-900"
                            title="Устгах"
                          >
                            🗑️
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
            <div className="flex-1 flex justify-between sm:hidden">
              <button
                onClick={() => handlePageChange(searchFilters.page! - 1)}
                disabled={searchFilters.page === 0}
                className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Өмнөх
              </button>
              <button
                onClick={() => handlePageChange(searchFilters.page! + 1)}
                disabled={searchFilters.page! >= totalPages - 1}
                className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Дараах
              </button>
            </div>
            <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
              <div>
                <p className="text-sm text-gray-700">
                  <span className="font-medium">{searchFilters.page! * searchFilters.size! + 1}</span> - <span className="font-medium">{Math.min((searchFilters.page! + 1) * searchFilters.size!, totalElements)}</span> of{' '}
                  <span className="font-medium">{totalElements}</span> үр дүн
                </p>
              </div>
              <div>
                <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                  <button
                    onClick={() => handlePageChange(searchFilters.page! - 1)}
                    disabled={searchFilters.page === 0}
                    className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <span className="sr-only">Өмнөх</span>
                    <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  </button>
                  
                  {Array.from({ length: Math.min(totalPages, 7) }, (_, i) => {
                    let pageNumber;
                    if (totalPages <= 7) {
                      pageNumber = i;
                    } else if (searchFilters.page! < 3) {
                      pageNumber = i;
                    } else if (searchFilters.page! > totalPages - 4) {
                      pageNumber = totalPages - 7 + i;
                    } else {
                      pageNumber = searchFilters.page! - 3 + i;
                    }
                    
                    return (
                      <button
                        key={pageNumber}
                        onClick={() => handlePageChange(pageNumber)}
                        className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                          pageNumber === searchFilters.page
                            ? 'z-10 bg-blue-50 border-blue-500 text-blue-600'
                            : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                        }`}
                      >
                        {pageNumber + 1}
                      </button>
                    );
                  })}
                  
                  <button
                    onClick={() => handlePageChange(searchFilters.page! + 1)}
                    disabled={searchFilters.page! >= totalPages - 1}
                    className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <span className="sr-only">Дараах</span>
                    <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                    </svg>
                  </button>
                </nav>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CustomerPage;