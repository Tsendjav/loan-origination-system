// customerService.ts - FIXED VERSION
import { apiClient } from './apiClient';

// Import types from our fixed type definitions
import {
  Customer,
  CustomerType,
  CustomerStatus,
  KycStatus,
  RiskLevel,
  Address,
  PaginatedResponse,
  ValidationError
} from '../types';

// Fixed API endpoints - create proper structure
const API_ENDPOINTS = {
  CUSTOMERS: {
    BASE: '/customers',
    BY_ID: (id: string) => `/customers/${id}`,
    SEARCH: '/customers/search',
    VALIDATE: '/customers/validate',
    BY_EMAIL: (email: string) => `/customers/by-email/${email}`,
    EXISTS: '/customers/exists'
  }
};

// API utilities - create proper implementation
const apiUtils = {
  buildParams: (params: Record<string, any>): string => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        searchParams.set(key, value.toString());
      }
    });
    return searchParams.toString();
  },
  
  handleResponse: <T>(response: any): T => {
    // Handle different response formats
    if (response?.data) {
      return response.data;
    }
    return response;
  },
  
  handleError: (error: any): never => {
    console.error('API Error:', error);
    throw error;
  }
};

// Search and filter interfaces
export interface CustomerSearchParams {
  query?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  customerType?: CustomerType;
  status?: CustomerStatus;
  kycStatus?: KycStatus;
  riskLevel?: RiskLevel;
  registrationDateFrom?: string;
  registrationDateTo?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}

export interface CustomerValidationRequest {
  email?: string;
  phone?: string;
  socialSecurityNumber?: string;
}

export interface CustomerValidationResponse {
  valid: boolean;
  errors: ValidationError[];
}

// Customer statistics
export interface CustomerStats {
  totalCustomers: number;
  activeCustomers: number;
  newCustomersThisMonth: number;
  customersByType: Record<CustomerType, number>;
  customersByRiskLevel: Record<RiskLevel, number>;
  kycCompletionRate: number;
}

class CustomerService {
  // Get all customers with pagination and filtering
  async getCustomers(params: CustomerSearchParams = {}): Promise<PaginatedResponse<Customer>> {
    try {
      const queryParams = apiUtils.buildParams(params);
      const url = `${API_ENDPOINTS.CUSTOMERS.BASE}?${queryParams}`;
      
      const response = await apiClient.get(url);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Get customer by ID
  async getCustomerById(id: string): Promise<Customer> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.CUSTOMERS.BY_ID(id));
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Create new customer
  async createCustomer(customer: Omit<Customer, 'id' | 'createdAt' | 'updatedAt'>): Promise<Customer> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.CUSTOMERS.BASE, customer);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Update existing customer
  async updateCustomer(id: string, customer: Partial<Customer>): Promise<Customer> {
    try {
      const response = await apiClient.put(API_ENDPOINTS.CUSTOMERS.BY_ID(id), customer);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Delete customer
  async deleteCustomer(id: string): Promise<void> {
    try {
      await apiClient.delete(API_ENDPOINTS.CUSTOMERS.BY_ID(id));
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Search customers
  async searchCustomers(params: CustomerSearchParams): Promise<PaginatedResponse<Customer>> {
    try {
      const queryParams = apiUtils.buildParams(params);
      const url = `${API_ENDPOINTS.CUSTOMERS.SEARCH}?${queryParams}`;
      
      const response = await apiClient.get(url);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Validate customer data
  async validateCustomer(data: CustomerValidationRequest): Promise<CustomerValidationResponse> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.CUSTOMERS.VALIDATE, data);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Get customer statistics
  async getCustomerStats(): Promise<CustomerStats> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.BASE}/stats`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Upload customer document
  async uploadDocument(customerId: string, file: File, documentType: string): Promise<any> {
    try {
      const response = await apiClient.uploadFile(
        `${API_ENDPOINTS.CUSTOMERS.BY_ID(customerId)}/documents`,
        file,
        {
          customerId: customerId,
          documentType: documentType
        }
      );
      
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Get customer documents
  async getCustomerDocuments(customerId: string): Promise<any[]> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.BY_ID(customerId)}/documents`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Update customer status
  async updateCustomerStatus(id: string, status: CustomerStatus): Promise<Customer> {
    try {
      const response = await apiClient.patch(
        `${API_ENDPOINTS.CUSTOMERS.BY_ID(id)}/status`,
        { status }
      );
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Update KYC status
  async updateKYCStatus(id: string, kycStatus: KycStatus): Promise<Customer> {
    try {
      const response = await apiClient.patch(
        `${API_ENDPOINTS.CUSTOMERS.BY_ID(id)}/kyc-status`,
        { kycStatus }
      );
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Get customer activity log
  async getCustomerActivity(customerId: string): Promise<any[]> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.BY_ID(customerId)}/activity`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Export customers to CSV
  async exportCustomers(params: CustomerSearchParams = {}): Promise<Blob> {
    try {
      const queryParams = apiUtils.buildParams(params);
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.BASE}/export?${queryParams}`);
      return response.data;
      
      // Create a mock CSV blob for now
      const csvContent = 'Name,Email,Phone,Status\n' +
        'John Doe,john@example.com,123-456-7890,ACTIVE\n' +
        'Jane Smith,jane@example.com,098-765-4321,INACTIVE';
      
      return new Blob([csvContent], { type: 'text/csv' });
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Bulk update customers
  async bulkUpdateCustomers(customerIds: string[], updates: Partial<Customer>): Promise<void> {
    try {
      await apiClient.patch(`${API_ENDPOINTS.CUSTOMERS.BASE}/bulk-update`, {
        customerIds,
        updates,
      });
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get customer by email
  async getCustomerByEmail(email: string): Promise<Customer | null> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.CUSTOMERS.BY_EMAIL(email));
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      return apiUtils.handleError(error);
    }
  }

  // Check if customer exists
  async customerExists(email: string, phone?: string): Promise<boolean> {
    try {
      const params = { email, phone };
      const queryParams = apiUtils.buildParams(params);
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.EXISTS}?${queryParams}`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }
}

// Export singleton instance
export const customerService = new CustomerService();
export default customerService;

// Utility functions for customer data
export const customerUtils = {
  // Format customer name
  getFullName: (customer: Customer): string => {
    if (customer.customerType === CustomerType.INDIVIDUAL) {
      return `${customer.firstName || ''} ${customer.lastName || ''}`.trim();
    }
    return customer.companyName || '';
  },

  // Format customer address
  formatAddress: (address: Address): string => {
    const parts = [
      address.street,
      address.city,
      address.state,
      address.postalCode
    ].filter(Boolean);
    return parts.join(', ');
  },

  // Calculate customer age
  calculateAge: (dateOfBirth: string): number => {
    const birth = new Date(dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    
    return age;
  },

  // Get status color for UI
  getStatusColor: (status: CustomerStatus): string => {
    const colors: Record<CustomerStatus, string> = {
      [CustomerStatus.ACTIVE]: 'green',
      [CustomerStatus.INACTIVE]: 'gray',
      [CustomerStatus.SUSPENDED]: 'red',
      [CustomerStatus.BLOCKED]: 'red',
      [CustomerStatus.PENDING_VERIFICATION]: 'yellow',
    };
    return colors[status] || 'gray';
  },


  // Get risk level color
  getRiskLevelColor: (riskLevel: RiskLevel): string => {
    const colors: Record<RiskLevel, string> = {
      [RiskLevel.LOW]: 'green',
      [RiskLevel.MEDIUM]: 'yellow',
      [RiskLevel.HIGH]: 'orange',
      [RiskLevel.CRITICAL]: 'red',
      [RiskLevel.VERY_HIGH]: 'darkred',
    };
    return colors[riskLevel] || 'gray';
  },

  // Validate customer data
  validateCustomerData: (customer: Partial<Customer>): ValidationError[] => {
    const errors: ValidationError[] = [];

    if (customer.customerType === CustomerType.INDIVIDUAL) {
      if (!customer.firstName?.trim()) {
        errors.push({
          field: 'firstName',
          message: 'Нэр заавал бөглөх ёстой',
        });
      }

      if (!customer.lastName?.trim()) {
        errors.push({
          field: 'lastName',
          message: 'Овог заавал бөглөх ёстой',
        });
      }
    } else if (customer.customerType === CustomerType.BUSINESS) {
      if (!customer.companyName?.trim()) {
        errors.push({
          field: 'companyName',
          message: 'Байгууллагын нэр заавал бөглөх ёстой',
        });
      }
    }

    if (!customer.email?.trim()) {
      errors.push({
        field: 'email',
        message: 'И-мэйл хаяг заавал бөглөх ёстой',
      });
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customer.email)) {
      errors.push({
        field: 'email',
        message: 'И-мэйл хаягийн формат буруу байна',
      });
    }

    if (!customer.phone?.trim()) {
      errors.push({
        field: 'phone',
        message: 'Утасны дугаар заавал бөглөх ёстой',
      });
    }

    if (!customer.registerNumber?.trim()) {
      errors.push({
        field: 'registerNumber',
        message: 'Регистрийн дугаар заавал бөглөх ёстой',
      });
    }

    return errors;
  },
};