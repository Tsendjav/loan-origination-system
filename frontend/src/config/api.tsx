// API Configuration and HTTP client setup
import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';

// API Base Configuration
export const API_CONFIG = {
  BASE_URL: process.env.REACT_APP_API_URL || 'http://localhost:8080/los/api/v1',
  TIMEOUT: 30000,
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000,
};

// Create axios instance with default config
const apiClient: AxiosInstance = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// Request interceptor - add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('los_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // Add request timestamp for debugging
    config.metadata = { startTime: new Date() };
    
    console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor - handle common errors
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    // Calculate request duration
    const duration = new Date().getTime() - response.config.metadata?.startTime?.getTime();
    console.log(`✅ API Response: ${response.config.method?.toUpperCase()} ${response.config.url} (${duration}ms)`);
    
    return response;
  },
  (error: AxiosError) => {
    const duration = new Date().getTime() - error.config?.metadata?.startTime?.getTime();
    console.error(`❌ API Error: ${error.config?.method?.toUpperCase()} ${error.config?.url} (${duration}ms)`, error.response?.status);
    
    // Handle specific error cases
    if (error.response?.status === 401) {
      // Unauthorized - clear token and redirect to login
      localStorage.removeItem('los_token');
      localStorage.removeItem('los_user');
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      // Forbidden - show error message
      console.error('Access denied. You do not have permission to access this resource.');
    } else if (error.response?.status >= 500) {
      // Server error - show generic message
      console.error('Server error. Please try again later.');
    }
    
    return Promise.reject(error);
  }
);

// API Response types
export interface ApiResponse<T = any> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
}

export interface ApiError {
  success: false;
  error: string;
  message: string;
  timestamp: string;
  path?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// API endpoint URLs
export const API_ENDPOINTS = {
  // Authentication
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    PROFILE: '/auth/profile',
  },
  
  // Customers
  CUSTOMERS: {
    BASE: '/customers',
    BY_ID: (id: number) => `/customers/${id}`,
    SEARCH: '/customers/search',
    VALIDATE: '/customers/validate',
  },
  
  // Loan Applications
  LOANS: {
    BASE: '/loan-applications',
    BY_ID: (id: number) => `/loan-applications/${id}`,
    BY_CUSTOMER: (customerId: number) => `/loan-applications/customer/${customerId}`,
    SUBMIT: '/loan-applications/submit',
    APPROVE: (id: number) => `/loan-applications/${id}/approve`,
    REJECT: (id: number) => `/loan-applications/${id}/reject`,
    STATUS: (id: number) => `/loan-applications/${id}/status`,
  },
  
  // Documents
  DOCUMENTS: {
    BASE: '/documents',
    BY_ID: (id: number) => `/documents/${id}`,
    BY_APPLICATION: (applicationId: number) => `/documents/application/${applicationId}`,
    UPLOAD: '/documents/upload',
    DOWNLOAD: (id: number) => `/documents/${id}/download`,
    TYPES: '/documents/types',
  },
  
  // Loan Products
  PRODUCTS: {
    BASE: '/loan-products',
    BY_ID: (id: number) => `/loan-products/${id}`,
    ACTIVE: '/loan-products/active',
  },
  
  // System
  SYSTEM: {
    HEALTH: '/health',
    CONFIG: '/system/config',
    USERS: '/users',
    ROLES: '/roles',
    AUDIT: '/audit',
  },
};

// Utility functions for API calls
export const apiUtils = {
  // Handle API response
  handleResponse: <T>(response: AxiosResponse<ApiResponse<T>>): T => {
    if (response.data.success) {
      return response.data.data;
    }
    throw new Error(response.data.message || 'API call failed');
  },
  
  // Handle API error
  handleError: (error: AxiosError<ApiError>): never => {
    const message = error.response?.data?.message || error.message || 'An unexpected error occurred';
    throw new Error(message);
  },
  
  // Build query parameters
  buildParams: (params: Record<string, any>): string => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        searchParams.append(key, String(value));
      }
    });
    return searchParams.toString();
  },
  
  // Retry failed requests
  retry: async <T>(
    fn: () => Promise<T>, 
    attempts: number = API_CONFIG.RETRY_ATTEMPTS,
    delay: number = API_CONFIG.RETRY_DELAY
  ): Promise<T> => {
    try {
      return await fn();
    } catch (error) {
      if (attempts > 1) {
        console.warn(`Retrying request... (${API_CONFIG.RETRY_ATTEMPTS - attempts + 1}/${API_CONFIG.RETRY_ATTEMPTS})`);
        await new Promise(resolve => setTimeout(resolve, delay));
        return apiUtils.retry(fn, attempts - 1, delay * 2);
      }
      throw error;
    }
  },
};

// Export configured axios instance
export default apiClient;

// Environment-specific configurations
export const getApiConfig = () => {
  const env = process.env.NODE_ENV || 'development';
  
  const configs = {
    development: {
      API_URL: 'http://localhost:8080/los/api/v1',
      ENABLE_LOGGING: true,
      MOCK_API: false,
    },
    production: {
      API_URL: process.env.REACT_APP_API_URL || 'https://los-api.company.com/api/v1',
      ENABLE_LOGGING: false,
      MOCK_API: false,
    },
    test: {
      API_URL: 'http://localhost:8080/los/api/v1',
      ENABLE_LOGGING: false,
      MOCK_API: true,
    },
  };
  
  return configs[env as keyof typeof configs] || configs.development;
};