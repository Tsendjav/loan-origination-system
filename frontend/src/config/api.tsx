// frontend/src/config/api.tsx - ЗАСВАРЛАСАН
import axios, { AxiosInstance, AxiosResponse, AxiosError, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios';
import { message } from 'antd';

// Extend AxiosRequestConfig to include metadata
interface ExtendedAxiosRequestConfig extends InternalAxiosRequestConfig {
  metadata?: {
    startTime: Date;
  };
  __isRetryRequest?: boolean;
}

// API Base Configuration
export const API_CONFIG = {
  BASE_URL: process.env.REACT_APP_API_URL || 'http://localhost:8080/los/api/v1',
  TIMEOUT: 30000,
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000,
};

// Type definitions
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp?: string;
}

export interface PaginatedResponse<T> {
  success: boolean;
  data: {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
  message?: string;
}

export interface AuthToken {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

// API Client Class
class ApiClient {
  private client: AxiosInstance;
  private refreshTokenPromise: Promise<string | null> | null = null;

  constructor() {
    this.client = axios.create({
      baseURL: API_CONFIG.BASE_URL,
      timeout: API_CONFIG.TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // Request interceptor
    this.client.interceptors.request.use(
      async (config: InternalAxiosRequestConfig) => {
        const token = this.getAuthToken();
        if (token) {
          config.headers['Authorization'] = `Bearer ${token}`;
        }

        // Add request timestamp for debugging - using type assertion
        (config as ExtendedAxiosRequestConfig).metadata = { startTime: new Date() };

        console.log(`📤 API Request: ${config.method?.toUpperCase()} ${config.url}`);
        return config;
      },
      (error) => {
        console.error('❌ Request Error:', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response: AxiosResponse) => {
        // Calculate request duration
        const configExt = response.config as ExtendedAxiosRequestConfig;
        const duration = new Date().getTime() - (configExt.metadata?.startTime?.getTime() || 0);
        console.log(`✅ API Response: ${response.config.method?.toUpperCase()} ${response.config.url} (${duration}ms)`);

        return response;
      },
      async (error: AxiosError) => {
        const configExt = error.config as ExtendedAxiosRequestConfig;
        const duration = new Date().getTime() - (configExt?.metadata?.startTime?.getTime() || 0);
        console.error(`❌ API Error: ${error.config?.method?.toUpperCase()} ${error.config?.url} (${duration}ms)`, error.response?.status);

        // Handle token refresh for 401 errors
        if (error.response?.status === 401 && error.config && !(configExt?.__isRetryRequest)) {
          try {
            const newToken = await this.refreshToken();
            if (newToken && error.config) {
              error.config.headers['Authorization'] = `Bearer ${newToken}`;
              (configExt).__isRetryRequest = true;
              return this.client.request(error.config);
            }
          } catch (refreshError) {
            this.handleAuthError();
          }
        }

        this.handleError(error);
        return Promise.reject(error);
      }
    );
  }

  private getAuthToken(): string | null {
    return localStorage.getItem('los_auth_token');
  }

  private async refreshToken(): Promise<string | null> {
    if (this.refreshTokenPromise) {
      return this.refreshTokenPromise;
    }

    const refreshToken = localStorage.getItem('los_refresh_token');
    if (!refreshToken) {
      return null;
    }

    this.refreshTokenPromise = this.client
      .post<ApiResponse<AuthToken>>('/auth/refresh', { refreshToken })
      .then((response) => {
        const { accessToken, refreshToken: newRefreshToken } = response.data.data;

        localStorage.setItem('los_auth_token', accessToken);
        localStorage.setItem('los_refresh_token', newRefreshToken);

        this.refreshTokenPromise = null;
        return accessToken;
      })
      .catch((error) => {
        this.refreshTokenPromise = null;
        throw error;
      });

    return this.refreshTokenPromise;
  }

  private handleAuthError(): void {
    localStorage.removeItem('los_auth_token');
    localStorage.removeItem('los_refresh_token');
    localStorage.removeItem('los_user_info');

    // Login хуудас руу чиглүүлэх
    window.location.href = '/login';

    if (typeof message !== 'undefined') {
      message.error('Нэвтрэх эрх дууссан. Дахин нэвтэрнэ үү.');
    }
  }

  private handleError(error: AxiosError): void {
    const response = error.response;

    if (process.env.NODE_ENV === 'development') {
      console.error(`❌ ${error.config?.method?.toUpperCase()} ${error.config?.url}`, {
        status: response?.status,
        data: response?.data,
        message: error.message,
      });
    }

    // Алдааны мэдээллийг хэрэглэгчид харуулах
    if (typeof message !== 'undefined') {
      const responseData = response?.data as any;
      if (responseData?.message) {
        message.error(responseData.message);
      } else if (response?.status === 500) {
        message.error('Серверийн алдаа гарлаа. Дахин оролдоно уу.');
      } else if (response?.status === 404) {
        message.error('Хүссэн мэдээлэл олдсонгүй.');
      } else if (response?.status === 403) {
        message.error('Энэ үйлдлийг хийх эрхгүй байна.');
      } else if (error.code === 'ECONNABORTED') {
        message.error('Хүсэлтийн хугацаа дууссан. Дахин оролдоно уу.');
      } else if (!navigator.onLine) {
        message.error('Интернэт холболт тасарсан байна.');
      } else {
        message.error('Алдаа гарлаа. Дахин оролдоно уу.');
      }
    }
  }

  // Generic CRUD үйлдлүүд
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.get<ApiResponse<T>>(url, config);
    return response.data.data;
  }

  async post<T, D = any>(url: string, data?: D, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.post<ApiResponse<T>>(url, data, config);
    return response.data.data;
  }

  async put<T, D = any>(url: string, data?: D, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.put<ApiResponse<T>>(url, data, config);
    return response.data.data;
  }

  async patch<T, D = any>(url: string, data?: D, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.patch<ApiResponse<T>>(url, data, config);
    return response.data.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.delete<ApiResponse<T>>(url, config);
    return response.data.data;
  }

  // Paginated хүсэлт
  async getPaginated<T>(
    url: string, 
    params?: { page?: number; size?: number; sort?: string; [key: string]: any }
  ): Promise<PaginatedResponse<T>> {
    const response = await this.client.get<PaginatedResponse<T>>(url, { params });
    return response.data;
  }

  // Файл upload
  async uploadFile(url: string, file: File, onProgress?: (progress: number) => void): Promise<any> {
    const formData = new FormData();
    formData.append('file', file);

    const config: AxiosRequestConfig = {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    };

    if (onProgress) {
      config.onUploadProgress = (progressEvent) => {
        const progress = Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1));
        onProgress(progress);
      };
    }

    return this.post(url, formData, config);
  }

  // Health check
  async healthCheck(): Promise<{ status: string; timestamp: string }> {
    return this.get('/health');
  }

  // Raw client-т хандах (шаардлагатай үед)
  getRawClient(): AxiosInstance {
    return this.client;
  }
}

// Singleton instance
export const apiClient = new ApiClient();

// Convenience functions
export const api = {
  // Auth
  auth: {
    login: (credentials: { username: string; password: string }) =>
      apiClient.post<AuthToken>('/auth/login', credentials),

    logout: () => apiClient.post('/auth/logout'),

    refreshToken: (refreshToken: string) =>
      apiClient.post<AuthToken>('/auth/refresh', { refreshToken }),

    getCurrentUser: () => apiClient.get<any>('/auth/me'),
  },

  // Customers
  customers: {
    getAll: (params?: any) => apiClient.getPaginated<any>('/customers', params),
    getById: (id: string) => apiClient.get<any>(`/customers/${id}`),
    create: (data: any) => apiClient.post<any>('/customers', data),
    update: (id: string, data: any) => apiClient.put<any>(`/customers/${id}`, data),
    delete: (id: string) => apiClient.delete(`/customers/${id}`),
    search: (query: string) => apiClient.get<any[]>(`/customers/search?q=${encodeURIComponent(query)}`),
  },

  // Loan Applications
  loanApplications: {
    getAll: (params?: any) => apiClient.getPaginated<any>('/loan-applications', params),
    getById: (id: string) => apiClient.get<any>(`/loan-applications/${id}`),
    create: (data: any) => apiClient.post<any>('/loan-applications', data),
    update: (id: string, data: any) => apiClient.put<any>(`/loan-applications/${id}`, data),
    delete: (id: string) => apiClient.delete(`/loan-applications/${id}`),
    updateStatus: (id: string, status: string, note?: string) =>
      apiClient.patch<any>(`/loan-applications/${id}/status`, { status, note }),
    assess: (id: string) => apiClient.post<any>(`/loan-applications/${id}/assess`),
  },

  // Documents
  documents: {
    getAll: (params?: any) => apiClient.getPaginated<any>('/documents', params),
    getById: (id: string) => apiClient.get<any>(`/documents/${id}`),
    upload: (file: File, onProgress?: (progress: number) => void) =>
      apiClient.uploadFile('/documents/upload', file, onProgress),
    delete: (id: string) => apiClient.delete(`/documents/${id}`),
    download: (id: string) => apiClient.getRawClient().get(`/documents/${id}/download`, {
      responseType: 'blob',
    }),
  },

  // System
  system: {
    health: () => apiClient.healthCheck(),
    version: () => apiClient.get<{ version: string; buildTime: string }>('/system/version'),
    stats: () => apiClient.get<any>('/system/stats'),
  },
};

export default api;