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

  private getAuthToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  private async refreshToken(): Promise<string | null> {
    if (this.refreshTokenPromise) {
      return this.refreshTokenPromise;
    }

    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      return null;
    }

    this.refreshTokenPromise = this.client
      .post<ApiResponse<AuthToken>>('/auth/refresh', { refreshToken })
      .then((response) => {
        const { accessToken, refreshToken: newRefreshToken } = response.data.data;
        
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
        
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
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    
    // Login хуудас руу чиглүүлэх
    window.location.href = '/login';
    
    message.error('Нэвтрэх эрх дууссан. Дахин нэвтэрнэ үү.');
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
    if (response?.data?.message) {
      message.error(response.data.message);
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