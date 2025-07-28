import apiClient, { API_ENDPOINTS, apiUtils, PaginatedResponse } from '../config/api';

// Customer interfaces
export interface Customer {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  dateOfBirth: string;
  socialSecurityNumber: string;
  address: Address;
  employmentInfo: EmploymentInfo;
  creditScore?: number;
  customerType: CustomerType;
  status: CustomerStatus;
  registrationDate?: string;
  lastUpdated?: string;
  kycStatus: KYCStatus;
  riskLevel: RiskLevel;
  preferredLanguage: string;
  communicationPreferences: CommunicationPreferences;
}

export interface Address {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  addressType: 'PRIMARY' | 'SECONDARY' | 'WORK';
}

export interface EmploymentInfo {
  employerName: string;
  jobTitle: string;
  employmentType: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'SELF_EMPLOYED' | 'UNEMPLOYED';
  monthlyIncome: number;
  employmentStartDate: string;
  workPhone?: string;
  workAddress?: Address;
}

export interface CommunicationPreferences {
  emailNotifications: boolean;
  smsNotifications: boolean;
  phoneNotifications: boolean;
  marketingConsent: boolean;
  preferredContactTime: 'MORNING' | 'AFTERNOON' | 'EVENING' | 'ANYTIME';
}

export enum CustomerType {
  INDIVIDUAL = 'INDIVIDUAL',
  BUSINESS = 'BUSINESS',
  VIP = 'VIP',
}

export enum CustomerStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  PENDING_VERIFICATION = 'PENDING_VERIFICATION',
}

export enum KYCStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED',
}

export enum RiskLevel {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  VERY_HIGH = 'VERY_HIGH',
}

// Search and filter interfaces
export interface CustomerSearchParams {
  query?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  customerType?: CustomerType;
  status?: CustomerStatus;
  kycStatus?: KYCStatus;
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

export interface ValidationError {
  field: string;
  message: string;
  code: string;
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
      apiUtils.handleError(error);
    }
  }

  // Get customer by ID
  async getCustomerById(id: number): Promise<Customer> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.CUSTOMERS.BY_ID(id));
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Create new customer
  async createCustomer(customer: Omit<Customer, 'id' | 'registrationDate' | 'lastUpdated'>): Promise<Customer> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.CUSTOMERS.BASE, customer);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Update existing customer
  async updateCustomer(id: number, customer: Partial<Customer>): Promise<Customer> {
    try {
      const response = await apiClient.put(API_ENDPOINTS.CUSTOMERS.BY_ID(id), customer);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Delete customer
  async deleteCustomer(id: number): Promise<void> {
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
      apiUtils.handleError(error);
    }
  }

  // Validate customer data
  async validateCustomer(data: CustomerValidationRequest): Promise<CustomerValidationResponse> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.CUSTOMERS.VALIDATE, data);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get customer statistics
  async getCustomerStats(): Promise<CustomerStats> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.BASE}/stats`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Upload customer document
  async uploadDocument(customerId: number, file: File, documentType: string): Promise<any> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('customerId', customerId.toString());
      formData.append('documentType', documentType);

      const response = await apiClient.post(
        `${API_ENDPOINTS.CUSTOMERS.BY_ID(customerId)}/documents`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get customer documents
  async getCustomerDocuments(customerId: number): Promise<any[]> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.BY_ID(customerId)}/documents`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Update customer status
  async updateCustomerStatus(id: number, status: CustomerStatus): Promise<Customer> {
    try {
      const response = await apiClient.patch(
        `${API_ENDPOINTS.CUSTOMERS.BY_ID(id)}/status`,
        { status }
      );
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Update KYC status
  async updateKYCStatus(id: number, kycStatus: KYCStatus): Promise<Customer> {
    try {
      const response = await apiClient.patch(
        `${API_ENDPOINTS.CUSTOMERS.BY_ID(id)}/kyc-status`,
        { kycStatus }
      );
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get customer activity log
  async getCustomerActivity(customerId: number): Promise<any[]> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.BY_ID(customerId)}/activity`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Export customers to CSV
  async exportCustomers(params: CustomerSearchParams = {}): Promise<Blob> {
    try {
      const queryParams = apiUtils.buildParams(params);
      const url = `${API_ENDPOINTS.CUSTOMERS.BASE}/export?${queryParams}`;
      
      const response = await apiClient.get(url, {
        responseType: 'blob',
      });
      
      return response.data;
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Bulk update customers
  async bulkUpdateCustomers(customerIds: number[], updates: Partial<Customer>): Promise<void> {
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
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.BASE}/by-email/${email}`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      apiUtils.handleError(error);
    }
  }

  // Check if customer exists
  async customerExists(email: string, phone?: string): Promise<boolean> {
    try {
      const params = { email, phone };
      const queryParams = apiUtils.buildParams(params);
      const response = await apiClient.get(`${API_ENDPOINTS.CUSTOMERS.BASE}/exists?${queryParams}`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
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
    return `${customer.firstName} ${customer.lastName}`.trim();
  },

  // Format customer address
  formatAddress: (address: Address): string => {
    return `${address.street}, ${address.city}, ${address.state} ${address.zipCode}`;
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
    const colors = {
      [CustomerStatus.ACTIVE]: 'green',
      [CustomerStatus.INACTIVE]: 'gray',
      [CustomerStatus.SUSPENDED]: 'red',
      [CustomerStatus.PENDING_VERIFICATION]: 'yellow',
    };
    return colors[status] || 'gray';
  },

  // Get risk level color
  getRiskLevelColor: (riskLevel: RiskLevel): string => {
    const colors = {
      [RiskLevel.LOW]: 'green',
      [RiskLevel.MEDIUM]: 'yellow',
      [RiskLevel.HIGH]: 'orange',
      [RiskLevel.VERY_HIGH]: 'red',
    };
    return colors[riskLevel] || 'gray';
  },

  // Validate customer data
  validateCustomerData: (customer: Partial<Customer>): ValidationError[] => {
    const errors: ValidationError[] = [];

    if (!customer.firstName?.trim()) {
      errors.push({
        field: 'firstName',
        message: 'Нэр заавал бөглөх ёстой',
        code: 'REQUIRED',
      });
    }

    if (!customer.lastName?.trim()) {
      errors.push({
        field: 'lastName',
        message: 'Овог заавал бөглөх ёстой',
        code: 'REQUIRED',
      });
    }

    if (!customer.email?.trim()) {
      errors.push({
        field: 'email',
        message: 'И-мэйл хаяг заавал бөглөх ёстой',
        code: 'REQUIRED',
      });
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customer.email)) {
      errors.push({
        field: 'email',
        message: 'И-мэйл хаягийн формат буруу байна',
        code: 'INVALID_FORMAT',
      });
    }

    if (!customer.phone?.trim()) {
      errors.push({
        field: 'phone',
        message: 'Утасны дугаар заавал бөглөх ёстой',
        code: 'REQUIRED',
      });
    }

    return errors;
  },
};