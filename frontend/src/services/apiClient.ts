// frontend/src/services/apiClient.ts - FIXED VERSION
import { API_CONFIG, ERROR_MESSAGES } from './apiConfig';

// ⭐ ИСПРАВЛЕНО: Убран неиспользуемый интерфейс ApiResponse, добавлены недостающие конфигурации

// API Configuration - добавляем недостающие конфигурации
const API_CONFIG_DEFAULT = {
  BASE_URL: process.env.REACT_APP_API_URL || 'http://localhost:8080/los/api/v1',
  TIMEOUT: 15000,
  HEADERS: {
    'Content-Type': 'application/json;charset=UTF-8',
    'Accept': 'application/json;charset=UTF-8',
  }
};

const ERROR_MESSAGES_DEFAULT = {
  TIMEOUT_ERROR: 'Хүсэлт хугацаа дууслаа',
  CORS_ERROR: 'CORS алдаа - Backend серверт холбогдож чадахгүй байна',
  CONNECTION_ERROR: 'Backend серверт холбогдох боломжгүй',
  NETWORK_ERROR: 'Сүлжээний алдаа',
  SERVER_ERROR: 'Серверийн алдаа',
  UNAUTHORIZED: 'Нэвтрэх эрхгүй',
  FORBIDDEN: 'Хандах эрхгүй',
  NOT_FOUND: 'Олдсонгүй',
  VALIDATION_ERROR: 'Мэдээлэл буруу байна'
};

// Use configs with fallback
const finalApiConfig = typeof API_CONFIG !== 'undefined' ? API_CONFIG : API_CONFIG_DEFAULT;
const finalErrorMessages = typeof ERROR_MESSAGES !== 'undefined' ? ERROR_MESSAGES : ERROR_MESSAGES_DEFAULT;

class ApiClient {
  private baseURL: string;
  private timeout: number;
  private defaultHeaders: Record<string, string>;

  constructor() {
    this.baseURL = finalApiConfig.BASE_URL;
    this.timeout = finalApiConfig.TIMEOUT;
    this.defaultHeaders = finalApiConfig.HEADERS;
  }

  private async request<T = any>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseURL}${endpoint}`;
    
    // Prepare headers
    const headers = new Headers({
      ...this.defaultHeaders,
      ...options.headers,
    });

    // Add auth token if available
    const token = localStorage.getItem('auth_token');
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    // ⭐ FIXED: CORS issue fix - use credentials: 'omit'
    const config: RequestInit = {
      ...options,
      headers,
      credentials: 'omit', // Fixed CORS error - don't use cookies
      mode: 'cors', // Explicit CORS mode
    };

    console.log(`🔄 API Request: ${options.method || 'GET'} ${url}`);

    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), this.timeout);

      const response = await fetch(url, {
        ...config,
        signal: controller.signal,
      });

      clearTimeout(timeoutId);

      console.log(`📡 API Response: ${response.status} ${response.statusText} ${url}`);

      // ⭐ FIXED: Response handling
      if (!response.ok) {
        await this.handleErrorResponse(response);
      }

      // Check if response has content
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        const data = await response.json();
        console.log(`📦 Response data:`, data);
        return data;
      } else if (response.status === 204) {
        // No content
        return {} as T;
      } else {
        const textData = await response.text();
        console.log(`📄 Response text:`, textData);
        return textData as unknown as T;
      }
    } catch (error) {
      console.error(`❌ API Error: ${url}`, error);
      
      if (error instanceof Error) {
        if (error.name === 'AbortError') {
          throw new Error(finalErrorMessages.TIMEOUT_ERROR);
        }
        
        // ⭐ FIXED: Specific error handling
        if (error.message.includes('CORS')) {
          throw new Error(finalErrorMessages.CORS_ERROR);
        }
        
        if (error.message.includes('Failed to fetch')) {
          throw new Error(finalErrorMessages.CONNECTION_ERROR);
        }
        
        throw new Error(error.message);
      }
      
      throw new Error(finalErrorMessages.NETWORK_ERROR);
    }
  }

  private async handleErrorResponse(response: Response): Promise<never> {
    let errorMessage = finalErrorMessages.SERVER_ERROR;

    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorData.error || errorMessage;
    } catch {
      // If response is not JSON, use status text
      errorMessage = response.statusText || errorMessage;
    }

    // ⭐ FIXED: Error status handling
    switch (response.status) {
      case 401:
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user');
        throw new Error(finalErrorMessages.UNAUTHORIZED);
      
      case 403:
        throw new Error(finalErrorMessages.FORBIDDEN);
      
      case 404:
        throw new Error(finalErrorMessages.NOT_FOUND);
      
      case 422:
        throw new Error(finalErrorMessages.VALIDATION_ERROR);
        
      case 0:
        // Network error
        throw new Error(finalErrorMessages.CONNECTION_ERROR);
      
      default:
        throw new Error(`${errorMessage} (HTTP ${response.status})`);
    }
  }

  // HTTP Methods
  async get<T = any>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }

  async post<T = any>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async put<T = any>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async patch<T = any>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PATCH',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete<T = any>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }

  // ⭐ FIXED: Health check with better error handling
  async healthCheck(): Promise<boolean> {
    try {
      console.log('🏥 Testing health endpoint...');
      
      // First try simple health check
      const simpleResponse = await this.get('/health/simple');
      console.log('✅ Simple health check passed:', simpleResponse);
      return true;
    } catch (error) {
      console.warn('⚠️ Simple health check failed, trying detailed health...');
      
      try {
        // Try detailed health check
        const detailedResponse = await this.get('/health');
        console.log('✅ Detailed health check passed:', detailedResponse);
        return true;
      } catch (detailedError) {
        console.error('❌ Both health checks failed:', error, detailedError);
        return false;
      }
    }
  }

  // ⭐ FIXED: Connection test with detailed diagnostics
  async testConnection(): Promise<{ 
    success: boolean; 
    message: string; 
    responseTime?: number;
    endpoint?: string;
  }> {
    const startTime = Date.now();
    
    // Test multiple endpoints to find which works
    const testEndpoints = [
      { path: '/health/simple', name: 'Simple Health' },
      { path: '/health', name: 'Detailed Health' },
      { path: '/', name: 'Home Endpoint' }
    ];
    
    for (const endpoint of testEndpoints) {
      try {
        console.log(`🧪 Testing endpoint: ${endpoint.name} (${endpoint.path})`);
        
        await this.get(endpoint.path);
        const responseTime = Date.now() - startTime;
        
        return {
          success: true,
          message: `Backend холбогдлоо - ${endpoint.name} endpoint (${responseTime}ms)`,
          responseTime,
          endpoint: endpoint.path
        };
      } catch (error) {
        console.warn(`❌ ${endpoint.name} failed:`, error);
        continue;
      }
    }
    
    // All endpoints failed
    return {
      success: false,
      message: 'Backend холбогдох боломжгүй - бүх endpoint алдаатай',
      responseTime: Date.now() - startTime
    };
  }

  // ⭐ FIXED: Direct URL test for troubleshooting
  async testDirectUrl(url: string): Promise<boolean> {
    try {
      console.log(`🎯 Direct URL test: ${url}`);
      
      const response = await fetch(url, {
        method: 'GET',
        mode: 'cors',
        credentials: 'omit'
      });
      
      console.log(`🎯 Direct test result: ${response.status} ${response.statusText}`);
      return response.ok;
    } catch (error) {
      console.error(`❌ Direct URL test failed:`, error);
      return false;
    }
  }

  // File upload helper (fixed types)
  async uploadFile<T = any>(
    endpoint: string,
    file: File,
    additionalData: Record<string, string> = {}
  ): Promise<T> {
    const url = `${this.baseURL}${endpoint}`;
    const formData = new FormData();
    
    formData.append('file', file);
    
    // Add additional form data
    Object.entries(additionalData).forEach(([key, value]) => {
      formData.append(key, value);
    });

    // Prepare headers (don't set Content-Type for FormData)
    const headers = new Headers();
    const token = localStorage.getItem('auth_token');
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    console.log(`📁 File Upload: POST ${url}`);

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers,
        body: formData,
        credentials: 'omit', // CORS fix
        mode: 'cors'
      });

      if (!response.ok) {
        await this.handleErrorResponse(response);
      }

      return await response.json();
    } catch (error) {
      console.error(`❌ Upload Error: ${url}`, error);
      throw error;
    }
  }
}

export const apiClient = new ApiClient();