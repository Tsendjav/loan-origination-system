// frontend/src/services/authService.ts
import axios, { AxiosResponse } from 'axios';

// API Configuration
const API_BASE_URL = 'http://localhost:8080/los/api/v1';
const AUTH_ENDPOINTS = {
  LOGIN: '/auth/login',
  LOGOUT: '/auth/logout',
  ME: '/auth/me',
  REFRESH: '/auth/refresh',
  TEST: '/auth/test'
};

// Types
export interface LoginCredentials {
  username: string;
  password: string;
}

export interface User {
  id: string;
  username: string;
  role: string;
  name: string;
  email?: string;
}

export interface LoginResponse {
  success: boolean;
  token?: string;
  user?: User;
  message?: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  loading: boolean;
  error: string | null;
}

// HTTP Client Configuration
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Token нэмэх
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('los_auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log(`📤 API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor - 401 алдаа шалгах
apiClient.interceptors.response.use(
  (response) => {
    console.log(`✅ API Response: ${response.config.method?.toUpperCase()} ${response.config.url} - ${response.status}`);
    return response;
  },
  (error) => {
    console.error(`❌ API Error: ${error.config?.method?.toUpperCase()} ${error.config?.url} - ${error.response?.status}`);
    
    if (error.response?.status === 401) {
      // Token хүчингүй болсон - гарах
      authService.logout();
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

class AuthService {
  private listeners: Array<(state: AuthState) => void> = [];

  // Subscribe to auth state changes
  subscribe(listener: (state: AuthState) => void) {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  // Notify all listeners of state changes
  private notifyListeners(state: AuthState) {
    this.listeners.forEach(listener => listener(state));
  }

  // Get current auth state
  getAuthState(): AuthState {
    const token = localStorage.getItem('los_auth_token');
    const userStr = localStorage.getItem('los_user_info');
    
    return {
      isAuthenticated: !!token,
      user: userStr ? JSON.parse(userStr) : null,
      loading: false,
      error: null,
    };
  }

  // Нэвтрэх
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    console.log('🔐 Attempting login for:', credentials.username);
    
    try {
      // Validate credentials
      if (!credentials || !credentials.username || !credentials.password) {
        const errorMessage = 'Хэрэглэгчийн нэр болон нууц үг оруулна уу';
        this.notifyListeners({
          isAuthenticated: false,
          user: null,
          loading: false,
          error: errorMessage,
        });
        throw new Error(errorMessage);
      }

      // Trim whitespace
      const cleanCredentials = {
        username: credentials.username.trim(),
        password: credentials.password.trim()
      };

      if (!cleanCredentials.username || !cleanCredentials.password) {
        const errorMessage = 'Хэрэглэгчийн нэр болон нууц үг хоосон байж болохгүй';
        this.notifyListeners({
          isAuthenticated: false,
          user: null,
          loading: false,
          error: errorMessage,
        });
        throw new Error(errorMessage);
      }
      
      // Notify listeners that login is in progress
      this.notifyListeners({
        isAuthenticated: false,
        user: null,
        loading: true,
        error: null,
      });

      console.log('📤 Sending login request:', { username: cleanCredentials.username, password: '[HIDDEN]' });

      // API дуудалт
      const response: AxiosResponse<LoginResponse> = await apiClient.post(AUTH_ENDPOINTS.LOGIN, cleanCredentials);

      console.log('✅ Login response:', { ...response.data, token: response.data.token ? '[HIDDEN]' : undefined });

      if (response.data.success && response.data.token && response.data.user) {
        // Store auth data
        localStorage.setItem('los_auth_token', response.data.token);
        localStorage.setItem('los_user_info', JSON.stringify(response.data.user));

        // Notify listeners of successful login
        this.notifyListeners({
          isAuthenticated: true,
          user: response.data.user,
          loading: false,
          error: null,
        });

        console.log('🎉 Login successful:', response.data.user.username);
        return response.data;
      } else {
        throw new Error(response.data.message || 'Нэвтрэхэд алдаа гарлаа');
      }
    } catch (error: any) {
      console.error('❌ Login failed:', error);
      
      let errorMessage = 'Нэвтрэхэд алдаа гарлаа';
      
      if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      // Notify listeners of login error
      this.notifyListeners({
        isAuthenticated: false,
        user: null,
        loading: false,
        error: errorMessage,
      });

      throw new Error(errorMessage);
    }
  }

  // Гарах
  async logout(): Promise<void> {
    console.log('🚪 Logging out...');
    
    try {
      // Backend logout endpoint дуудах
      await apiClient.post(AUTH_ENDPOINTS.LOGOUT);
    } catch (error) {
      console.warn('Logout endpoint failed, but continuing with local logout:', error);
    } finally {
      // Local storage цэвэрлэх
      localStorage.removeItem('los_auth_token');
      localStorage.removeItem('los_user_info');

      // Notify listeners of logout
      this.notifyListeners({
        isAuthenticated: false,
        user: null,
        loading: false,
        error: null,
      });

      console.log('✅ Logout completed');
    }
  }

  // Одоогийн хэрэглэгчийн мэдээлэл авах
  async getCurrentUser(): Promise<User | null> {
    try {
      if (!this.isAuthenticated()) {
        return null;
      }

      const response = await apiClient.get<{success: boolean; data: User}>(AUTH_ENDPOINTS.ME);
      
      if (response.data.success && response.data.data) {
        // Update stored user data
        localStorage.setItem('los_user_info', JSON.stringify(response.data.data));
        return response.data.data;
      }
      
      return null;
    } catch (error) {
      console.error('Failed to get current user:', error);
      
      // Token хүчингүй байх магадлалтай
      this.logout();
      return null;
    }
  }

  // Нэвтэрсэн эсэхийг шалгах
  isAuthenticated(): boolean {
    const token = localStorage.getItem('los_auth_token');
    const user = localStorage.getItem('los_user_info');
    return !!(token && user);
  }

  // Хадгалагдсан хэрэглэгчийн мэдээлэл авах
  getStoredUser(): User | null {
    const userStr = localStorage.getItem('los_user_info');
    return userStr ? JSON.parse(userStr) : null;
  }

  // Token авах
  getToken(): string | null {
    return localStorage.getItem('los_auth_token');
  }

  // Role шалгах
  hasRole(role: string): boolean {
    const user = this.getStoredUser();
    return user?.role === role;
  }

  // Олон role шалгах
  hasAnyRole(roles: string[]): boolean {
    const user = this.getStoredUser();
    return user ? roles.includes(user.role) : false;
  }

  // Service эхлүүлэх
  async initialize(): Promise<void> {
    console.log('🚀 Initializing auth service...');
    
    if (this.isAuthenticated()) {
      try {
        // Token-ыг баталгаажуулах
        const user = await this.getCurrentUser();
        if (user) {
          this.notifyListeners({
            isAuthenticated: true,
            user,
            loading: false,
            error: null,
          });
        }
      } catch (error) {
        console.warn('Token validation failed during initialization');
        this.logout();
      }
    }
  }

  // Token сэргээх
  async refreshToken(): Promise<boolean> {
    try {
      const response = await apiClient.post<{success: boolean; token: string}>(AUTH_ENDPOINTS.REFRESH, {
        refreshToken: this.getToken()
      });
      
      if (response.data.success && response.data.token) {
        localStorage.setItem('los_auth_token', response.data.token);
        return true;
      }
      
      return false;
    } catch (error) {
      console.error('Token refresh failed:', error);
      this.logout();
      return false;
    }
  }

  // Backend холболт тест
  async testConnection(): Promise<boolean> {
    try {
      const response = await apiClient.get(AUTH_ENDPOINTS.TEST);
      return response.status === 200;
    } catch (error) {
      console.error('Backend connection test failed:', error);
      return false;
    }
  }

  // Test login with default users
  async testLogin(): Promise<void> {
    console.log('🧪 Testing login with default admin user...');
    
    try {
      await this.login({
        username: 'admin',
        password: 'admin123'
      });
      console.log('✅ Test login successful');
    } catch (error) {
      console.error('❌ Test login failed:', error);
    }
  }

  // Admin эрх шалгах
  isAdmin(): boolean {
    return this.hasAnyRole(['SUPER_ADMIN', 'ADMIN']);
  }

  // Manager эрх шалгах
  isManager(): boolean {
    return this.hasRole('MANAGER');
  }

  // Loan Officer эрх шалгах
  isLoanOfficer(): boolean {
    return this.hasRole('LOAN_OFFICER');
  }
}

// Singleton instance үүсгэх
export const authService = new AuthService();

// Default users for testing
export const DEFAULT_TEST_USERS = [
  { username: 'admin', password: 'admin123', role: 'SUPER_ADMIN', name: 'Системийн админ' },
  { username: 'manager', password: 'manager123', role: 'MANAGER', name: 'Салбарын менежер' },
  { username: 'loan_officer', password: 'loan123', role: 'LOAN_OFFICER', name: 'Зээлийн мэргэжилтэн' },
  { username: 'reviewer', password: 'admin123', role: 'DOCUMENT_REVIEWER', name: 'Баримт хянагч' },
  { username: 'customer_service', password: 'admin123', role: 'CUSTOMER_SERVICE', name: 'Харилцагчийн үйлчилгээ' },
];

export default authService;