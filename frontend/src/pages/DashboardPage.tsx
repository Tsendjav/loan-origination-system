import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth, PERMISSIONS } from '../contexts/AuthContext';
import { customerService } from '../services/customerService';
import { loanService, LoanStatus } from '../services/loanService';
import { showToast } from '../components/layout/MainLayout';

interface DashboardStats {
  customers: {
    total: number;
    new: number;
    active: number;
  };
  loans: {
    total: number;
    pending: number;
    approved: number;
    rejected: number;
    totalAmount: number;
  };
  documents: {
    total: number;
    pending: number;
  };
}

interface RecentActivity {
  id: string;
  type: 'customer' | 'loan' | 'document' | 'system';
  title: string;
  description: string;
  timestamp: string;
  user?: string;
  status?: string;
}

const DashboardPage: React.FC = () => {
  const { state, hasPermission } = useAuth();
  const [stats, setStats] = useState<DashboardStats>({
    customers: { total: 0, new: 0, active: 0 },
    loans: { total: 0, pending: 0, approved: 0, rejected: 0, totalAmount: 0 },
    documents: { total: 0, pending: 0 },
  });
  const [recentActivities, setRecentActivities] = useState<RecentActivity[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedPeriod, setSelectedPeriod] = useState<'week' | 'month' | 'quarter'>('month');

  // Load dashboard data
  useEffect(() => {
    loadDashboardData();
  }, [selectedPeriod]);

  const loadDashboardData = async () => {
    setIsLoading(true);
    try {
      const [customerStats, loanStats] = await Promise.all([
        hasPermission(PERMISSIONS.CUSTOMER_VIEW) ? customerService.getCustomerStats() : null,
        hasPermission(PERMISSIONS.LOAN_VIEW) ? loanService.getLoanStats() : null,
      ]);

      if (customerStats) {
        setStats(prev => ({
          ...prev,
          customers: {
            total: customerStats.totalCustomers,
            new: customerStats.newCustomersThisMonth,
            active: customerStats.activeCustomers,
          },
        }));
      }

      if (loanStats) {
        setStats(prev => ({
          ...prev,
          loans: {
            total: loanStats.totalApplications,
            pending: loanStats.pendingApplications,
            approved: loanStats.approvedApplications,
            rejected: loanStats.rejectedApplications,
            totalAmount: loanStats.totalDisbursedAmount,
          },
        }));
      }

      // Load recent activities (mock data for now)
      setRecentActivities([
        {
          id: '1',
          type: 'loan',
          title: 'Шинэ зээлийн хүсэлт',
          description: 'Батбаяр Болд 10,000,000₮ зээлийн хүсэлт илгээлээ',
          timestamp: '2025-07-28T10:30:00Z',
          user: 'Батбаяр Болд',
          status: 'pending',
        },
        {
          id: '2',
          type: 'customer',
          title: 'Шинэ харилцагч',
          description: 'Сарангэрэл Батбаяр системд бүртгэгдлээ',
          timestamp: '2025-07-28T09:15:00Z',
          user: 'System',
          status: 'active',
        },
        {
          id: '3',
          type: 'loan',
          title: 'Зээл зөвшөөрөгдсөн',
          description: 'Хүсэлт #002 амжилттай зөвшөөрөгдлөө',
          timestamp: '2025-07-28T08:45:00Z',
          user: state.user?.firstName,
          status: 'approved',
        },
        {
          id: '4',
          type: 'document',
          title: 'Баримт шалгагдсан',
          description: 'Харилцагч #001-ийн орлогын справка баталгаажлаа',
          timestamp: '2025-07-27T16:20:00Z',
          user: 'Документ менежер',
          status: 'verified',
        },
        {
          id: '5',
          type: 'system',
          title: 'Системийн шинэчлэл',
          description: 'LOS систем v1.2.0 амжилттай суулгагдлаа',
          timestamp: '2025-07-27T14:00:00Z',
          user: 'System Admin',
          status: 'completed',
        },
      ]);

    } catch (error: any) {
      console.error('Dashboard data loading error:', error);
      showToast({
        message: 'Хяналтын самбарын өгөгдөл ачаалахад алдаа гарлаа',
        type: 'error',
      });
    } finally {
      setIsLoading(false);
    }
  };

  // Quick action cards
  const quickActions = [
    {
      title: 'Шинэ харилцагч',
      description: 'Харилцагч бүртгэх',
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
        </svg>
      ),
      href: '/customers/new',
      color: 'bg-blue-500',
      permission: PERMISSIONS.CUSTOMER_CREATE,
    },
    {
      title: 'Зээлийн хүсэлт',
      description: 'Шинэ хүсэлт үүсгэх',
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
        </svg>
      ),
      href: '/loans/new',
      color: 'bg-green-500',
      permission: PERMISSIONS.LOAN_CREATE,
    },
    {
      title: 'Баримт шалгах',
      description: 'Хүлээгдэж буй баримтууд',
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      ),
      href: '/documents',
      color: 'bg-yellow-500',
      permission: PERMISSIONS.DOCUMENT_VIEW,
    },
    {
      title: 'Тайлан харах',
      description: 'Систем тайлангууд',
      icon: (
        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
      ),
      href: '/reports',
      color: 'bg-purple-500',
      permission: PERMISSIONS.REPORT_VIEW,
    },
  ];

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('mn-MN', {
      style: 'currency',
      currency: 'MNT',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('mn-MN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'customer':
        return (
          <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
            <svg className="w-4 h-4 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
            </svg>
          </div>
        );
      case 'loan':
        return (
          <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
            <svg className="w-4 h-4 text-green-600" fill="currentColor" viewBox="0 0 20 20">
              <path d="M8.433 7.418c.155-.103.346-.196.567-.267v1.698a2.305 2.305 0 01-.567-.267C8.07 8.34 8 8.114 8 8c0-.114.07-.34.433-.582zM11 12.849v-1.698c.22.071.412.164.567.267.364.243.433.468.433.582 0 .114-.07.34-.433.582a2.305 2.305 0 01-.567.267z" />
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-13a1 1 0 10-2 0v.092a4.535 4.535 0 00-1.676.662C6.602 6.234 6 7.009 6 8c0 .99.602 1.765 1.324 2.246.48.32 1.054.545 1.676.662v1.941c-.391-.127-.68-.317-.843-.504a1 1 0 10-1.51 1.31c.562.649 1.413 1.076 2.353 1.253V15a1 1 0 102 0v-.092a4.535 4.535 0 001.676-.662C13.398 13.766 14 12.991 14 12c0-.99-.602-1.765-1.324-2.246A4.535 4.535 0 0011 9.092V7.151c.391.127.68.317.843.504a1 1 0 101.511-1.31c-.563-.649-1.413-1.076-2.354-1.253V5z" clipRule="evenodd" />
            </svg>
          </div>
        );
      case 'document':
        return (
          <div className="w-8 h-8 bg-yellow-100 rounded-full flex items-center justify-center">
            <svg className="w-4 h-4 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clipRule="evenodd" />
            </svg>
          </div>
        );
      default:
        return (
          <div className="w-8 h-8 bg-gray-100 rounded-full flex items-center justify-center">
            <svg className="w-4 h-4 text-gray-600" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clipRule="evenodd" />
            </svg>
          </div>
        );
    }
  };

  const getStatusBadge = (status?: string) => {
    const statusColors: Record<string, string> = {
      pending: 'bg-yellow-100 text-yellow-800',
      approved: 'bg-green-100 text-green-800',
      rejected: 'bg-red-100 text-red-800',
      active: 'bg-blue-100 text-blue-800',
      verified: 'bg-green-100 text-green-800',
      completed: 'bg-gray-100 text-gray-800',
    };

    if (!status) return null;

    return (
      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusColors[status] || 'bg-gray-100 text-gray-800'}`}>
        {status}
      </span>
    );
  };

  if (isLoading) {
    return (
      <div className="p-6">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-gray-200 rounded w-1/4"></div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="h-32 bg-gray-200 rounded-lg"></div>
            ))}
          </div>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 h-96 bg-gray-200 rounded-lg"></div>
            <div className="h-96 bg-gray-200 rounded-lg"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Сайн байна уу, {state.user?.firstName}! 👋
          </h1>
          <p className="text-gray-600 mt-1">
            Өнөөдрийн системийн ерөнхий байдлыг энд харж болно
          </p>
        </div>
        
        <div className="flex items-center space-x-4">
          <select
            value={selectedPeriod}
            onChange={(e) => setSelectedPeriod(e.target.value as any)}
            className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="week">Энэ 7 хоног</option>
            <option value="month">Энэ сар</option>
            <option value="quarter">Энэ улирал</option>
          </select>
          
          <button
            onClick={loadDashboardData}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Шинэчлэх
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Customers Card */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="flex items-center">
            <div className="p-2 bg-blue-100 rounded-lg">
              <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Харилцагчид</p>
              <p className="text-2xl font-bold text-gray-900">{stats.customers.total.toLocaleString()}</p>
            </div>
          </div>
          <div className="mt-4">
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Идэвхтэй</span>
              <span className="font-medium text-green-600">{stats.customers.active}</span>
            </div>
            <div className="flex items-center justify-between text-sm mt-1">
              <span className="text-gray-600">Шинэ</span>
              <span className="font-medium text-blue-600">+{stats.customers.new}</span>
            </div>
          </div>
        </div>

        {/* Loans Card */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="flex items-center">
            <div className="p-2 bg-green-100 rounded-lg">
              <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Зээлийн хүсэлт</p>
              <p className="text-2xl font-bold text-gray-900">{stats.loans.total.toLocaleString()}</p>
            </div>
          </div>
          <div className="mt-4">
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Хүлээгдэж буй</span>
              <span className="font-medium text-yellow-600">{stats.loans.pending}</span>
            </div>
            <div className="flex items-center justify-between text-sm mt-1">
              <span className="text-gray-600">Зөвшөөрөгдсөн</span>
              <span className="font-medium text-green-600">{stats.loans.approved}</span>
            </div>
          </div>
        </div>

        {/* Amount Card */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="flex items-center">
            <div className="p-2 bg-purple-100 rounded-lg">
              <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Нийт олгосон дүн</p>
              <p className="text-xl font-bold text-gray-900">{formatCurrency(stats.loans.totalAmount)}</p>
            </div>
          </div>
          <div className="mt-4">
            <div className="flex items-center text-sm">
              <span className="text-green-600">↗ 12%</span>
              <span className="text-gray-600 ml-1">өмнөх сартай харьцуулахад</span>
            </div>
          </div>
        </div>

        {/* Documents Card */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <div className="flex items-center">
            <div className="p-2 bg-yellow-100 rounded-lg">
              <svg className="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600">Баримт бичиг</p>
              <p className="text-2xl font-bold text-gray-900">{stats.documents.total.toLocaleString()}</p>
            </div>
          </div>
          <div className="mt-4">
            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-600">Шалгалт хүлээгч</span>
              <span className="font-medium text-orange-600">{stats.documents.pending}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Activities */}
        <div className="lg:col-span-2 bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="px-6 py-4 border-b border-gray-200">
            <h3 className="text-lg font-medium text-gray-900">Сүүлийн үйл ажиллагаа</h3>
          </div>
          <div className="p-6">
            <div className="flow-root">
              <ul className="-mb-8">
                {recentActivities.map((activity, activityIdx) => (
                  <li key={activity.id}>
                    <div className="relative pb-8">
                      {activityIdx !== recentActivities.length - 1 ? (
                        <span className="absolute top-4 left-4 -ml-px h-full w-0.5 bg-gray-200" aria-hidden="true" />
                      ) : null}
                      <div className="relative flex space-x-3">
                        <div>{getActivityIcon(activity.type)}</div>
                        <div className="min-w-0 flex-1 pt-1.5 flex justify-between space-x-4">
                          <div>
                            <p className="text-sm font-medium text-gray-900">{activity.title}</p>
                            <p className="text-sm text-gray-500">{activity.description}</p>
                            {activity.user && (
                              <p className="text-xs text-gray-400">by {activity.user}</p>
                            )}
                          </div>
                          <div className="text-right text-sm whitespace-nowrap">
                            <time className="text-gray-500">{formatDate(activity.timestamp)}</time>
                            {activity.status && (
                              <div className="mt-1">{getStatusBadge(activity.status)}</div>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
            <div className="mt-6">
              <Link
                to="/audit"
                className="w-full flex justify-center items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
              >
                Бүх үйл ажиллагаа харах
              </Link>
            </div>
          </div>
        </div>

        {/* Quick Actions & Pending Tasks */}
        <div className="space-y-6">
          {/* Quick Actions */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">Хурдан үйлдэл</h3>
            </div>
            <div className="p-6">
              <div className="grid grid-cols-2 gap-4">
                {quickActions.map((action) => {
                  if (action.permission && !hasPermission(action.permission)) {
                    return null;
                  }
                  
                  return (
                    <Link
                      key={action.title}
                      to={action.href}
                      className="relative group bg-gray-50 p-4 rounded-lg hover:bg-gray-100 transition-colors"
                    >
                      <div>
                        <span className={`rounded-lg inline-flex p-3 text-white ${action.color}`}>
                          {action.icon}
                        </span>
                      </div>
                      <div className="mt-4">
                        <h4 className="text-sm font-medium text-gray-900 group-hover:text-gray-800">
                          {action.title}
                        </h4>
                        <p className="text-xs text-gray-500 mt-1">{action.description}</p>
                      </div>
                    </Link>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Pending Tasks */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200">
            <div className="px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-medium text-gray-900">Хийх ёстой зүйлс</h3>
            </div>
            <div className="p-6">
              <div className="space-y-4">
                <div className="flex items-center p-3 bg-yellow-50 rounded-lg border border-yellow-200">
                  <div className="flex-shrink-0">
                    <svg className="w-5 h-5 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div className="ml-3 flex-1">
                    <p className="text-sm font-medium text-yellow-800">
                      {stats.loans.pending} зээлийн хүсэлт хянах хэрэгтэй
                    </p>
                    <p className="text-xs text-yellow-600">Зөвшөөрөл хүлээж байна</p>
                  </div>
                  <Link
                    to="/loans?status=pending"
                    className="text-yellow-600 hover:text-yellow-800 text-sm font-medium"
                  >
                    Харах →
                  </Link>
                </div>

                <div className="flex items-center p-3 bg-blue-50 rounded-lg border border-blue-200">
                  <div className="flex-shrink-0">
                    <svg className="w-5 h-5 text-blue-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div className="ml-3 flex-1">
                    <p className="text-sm font-medium text-blue-800">
                      {stats.documents.pending} баримт шалгалт хүлээж байна
                    </p>
                    <p className="text-xs text-blue-600">Баталгаажуулах шаардлагатай</p>
                  </div>
                  <Link
                    to="/documents?status=pending"
                    className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                  >
                    Харах →
                  </Link>
                </div>

                <div className="flex items-center p-3 bg-green-50 rounded-lg border border-green-200">
                  <div className="flex-shrink-0">
                    <svg className="w-5 h-5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <div className="ml-3 flex-1">
                    <p className="text-sm font-medium text-green-800">
                      Системийн байдал: Хэвийн
                    </p>
                    <p className="text-xs text-green-600">Бүх системүүд ажиллаж байна</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;