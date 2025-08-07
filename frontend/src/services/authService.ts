// frontend/src/services/authService.ts - FINAL BACKEND-COMPATIBLE VERSION
import axios, { AxiosResponse } from 'axios';
import { API_CONFIG } from '../config/api';
import type { 
  LoginCredentials, 
  User, 
  LoginResponse, 
  AuthState, 
  ValidationTestResponse,
  UserRole
} from '../types/index';

// ⭐ API Configuration - BACKEND AuthController-тай бүрэн тохирсон ⭐
const API_BASE_URL = API_CONFIG.BASE_URL;
const AUTH_ENDPOINTS = {
  LOGIN: '/auth/login',
  LOGOUT: '/auth/logout', 
  ME: '/auth/me',
  REFRESH: '/auth/refresh',
  TEST: '/auth/test',    // Backend /auth/test endpoint
  VALIDATE: '/auth/validate',  
  TEST_USERS: '/auth/test-users',
  DEBUG_JSON: '/auth/debug-json', // Debug endpoint
  TEST_VALIDATION: '/auth/test-validation' // Validation test endpoint
};

// Health check endpoints
const HEALTH_ENDPOINTS = {
  HEALTH: '/auth/health',  // Backend AuthController health
  SIMPLE: '/health'    // General health
};

// ⭐ ERROR TYPE HELPER - TypeScript safe ⭐
function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  if (typeof error === 'string') {
    return error;
  }
  if (error && typeof error === 'object' && 'message' in error) {
    return String((error as any).message);
  }
  return 'Тодорхойгүй алдаа гарлаа';
}

// ⭐ HTTP Client Configuration - Backend-тай бүрэн тохирсон ⭐
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000, // 15 секунд - backend-тай холболт удаан болж магадгүй
  headers: {
    'Content-Type': 'application/json;charset=UTF-8', // Backend-тай тохирсон
    'Accept': 'application/json;charset=UTF-8',
  },
});

// Request interceptor - Token нэмэх
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('los_auth_token');
    if (token && !config.url?.includes('/login')) { // Login endpoint-д token бүү нэм
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Debug logging
    console.log(`📤 API Request: ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
    if (config.data && !config.url?.includes('/login')) {
      console.log('📋 Request data:', config.data);
    }

    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Response interceptor - Error handling сайжруулсан
apiClient.interceptors.response.use(
  (response) => {
    console.log(`✅ API Response: ${response.config.method?.toUpperCase()} ${response.config.url} - ${response.status}`);
    return response;
  },
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url;

    console.error(`❌ API Error: ${error.config?.method?.toUpperCase()} ${url} - ${status}`);

    // Response data debug
    if (error.response?.data) {
      console.error('❌ Error details:', error.response.data);
    }

    // 401 алдаа - token хүчингүй
    if (status === 401 && !url?.includes('/login')) {
      console.warn('🔓 Token expired, logging out...');
      authService.logout();
      if (typeof window !== 'undefined') {
        window.location.href = '/login';
      }
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
    this.listeners.forEach(listener => {
      try {
        listener(state);
      } catch (error) {
        console.error('❌ Auth state listener error:', error);
      }
    });
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

  // ⭐ FINAL LOGIN METHOD - Backend AuthController format-тай бүрэн тохирсон ⭐
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    console.log('🔐 Starting login process for:', credentials.username);

    try {
      // Validate input parameters
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

      // ⭐ BACKEND-COMPATIBLE NORMALIZATION - toLowerCase() хасагдсан ⭐
      const loginRequest = {
        username: credentials.username.trim(), // ⭐ toLowerCase() хасагдсан ⭐
        password: credentials.password.trim()
      };

      // Empty check
      if (!loginRequest.username || !loginRequest.password) {
        const errorMessage = 'Хэрэглэгчийн нэр болон нууц үг хоосон байж болохгүй';
        this.notifyListeners({
          isAuthenticated: false,
          user: null,
          loading: false,
          error: errorMessage,
        });
        throw new Error(errorMessage);
      }

      // Frontend validation - Backend LoginRequestDto-тай тохирсон
      const validationError = this.validateCredentials(loginRequest);
      if (validationError) {
        console.warn('⚠️ Frontend validation failed:', validationError);
        this.notifyListeners({
          isAuthenticated: false,
          user: null,
          loading: false,
          error: validationError,
        });
        throw new Error(validationError);
      }

      // Notify loading state
      this.notifyListeners({
        isAuthenticated: false,
        user: null,
        loading: true,
        error: null,
      });

      console.log('📤 Sending login request to backend...');
      console.log('📋 Request payload:', { 
        username: loginRequest.username, 
        password: '[HIDDEN]',
        length: loginRequest.password.length 
      });

      // ⭐ BACKEND AuthController /auth/login ENDPOINT ⭐
      const response: AxiosResponse<LoginResponse> = await apiClient.post(AUTH_ENDPOINTS.LOGIN, loginRequest);

      console.log('✅ Login response received:', {
        status: response.status,
        success: response.data.success,
        hasToken: !!response.data.token,
        hasUser: !!response.data.user
      });

      // ⭐ BACKEND AuthController RESPONSE FORMAT HANDLING ⭐
      if (response.data.success && response.data.token && response.data.user) {
        // Backend response format-тай тохирсон user object normalization
        const normalizedUser: User = {
          id: response.data.user.id,
          username: response.data.user.username,
          role: response.data.user.role,
          name: response.data.user.fullName || response.data.user.name || response.data.user.username,
          email: response.data.user.email,
          fullName: response.data.user.fullName || response.data.user.name,
          roles: response.data.user.roles || [response.data.user.role]
        };

        // Store authentication data
        localStorage.setItem('los_auth_token', response.data.token);
        if (response.data.refreshToken) {
          localStorage.setItem('los_refresh_token', response.data.refreshToken);
        }
        localStorage.setItem('los_user_info', JSON.stringify(normalizedUser));

        // Success state notification
        this.notifyListeners({
          isAuthenticated: true,
          user: normalizedUser,
          loading: false,
          error: null,
        });

        console.log('🎉 Login successful for user:', normalizedUser.username);

        return {
          success: true,
          token: response.data.token,
          refreshToken: response.data.refreshToken,
          tokenType: response.data.tokenType || 'Bearer',
          expiresIn: response.data.expiresIn,
          user: normalizedUser,
          message: response.data.message || 'Амжилттай нэвтэрлээ'
        };
      } else {
        // Backend error response
        const errorMessage = response.data.message || response.data.error || 'Нэвтрэхэд алдаа гарлаа';
        throw new Error(errorMessage);
      }

    } catch (error: unknown) {
      console.error('❌ Login process failed:', error);

      let errorMessage = 'Нэвтрэхэд алдаа гарлаа';

      // ⭐ ENHANCED ERROR HANDLING - Backend error format-тай тохирсон ⭐
      const axiosError = error as any;

      if (axiosError.response?.data) {
        const errorData = axiosError.response.data;

        // Backend validation error format
        if (errorData.details) {
          const details = errorData.details;
          if (details.username) {
            errorMessage = details.username;
          } else if (details.password) {
            errorMessage = details.password;
          } else {
            errorMessage = errorData.message || 'Validation алдаа';
          }
        } 
        // Direct error message
        else if (errorData.error) {
          errorMessage = errorData.error;
        } 
        // Message field
        else if (errorData.message) {
          errorMessage = errorData.message;
        }
      } 
      // Network and HTTP status errors
      else if (axiosError.response?.status === 401) {
        errorMessage = 'Хэрэглэгчийн нэр эсвэл нууц үг буруу байна';
      } else if (axiosError.response?.status === 400) {
        errorMessage = 'Нэвтрэх мэдээлэл буруу байна';
      } else if (axiosError.response?.status === 500) {
        errorMessage = 'Серверийн алдаа гарлаа. Дахин оролдоно уу';
      } else if (axiosError.code === 'ECONNREFUSED' || axiosError.code === 'NETWORK_ERROR') {
        errorMessage = 'Backend серверт холбогдохгүй байна. Сервер ажиллаж байгааг шалгана уу';
      } else if (axiosError.message) {
        errorMessage = axiosError.message;
      }

      // Error state notification
      this.notifyListeners({
        isAuthenticated: false,
        user: null,
        loading: false,
        error: errorMessage,
      });

      throw new Error(errorMessage);
    }
  }

  // ⭐ BACKEND-COMPATIBLE VALIDATION - LoginRequestDto тохирсон ⭐
  private validateCredentials(credentials: LoginCredentials): string | null {
    const { username, password } = credentials;

    // Username validation - Backend LoginRequestDto-тай тохирсон
    if (username.length < 3 || username.length > 50) {
      return 'Хэрэглэгчийн нэр 3-50 тэмдэгт байх ёстой';
    }

    // Username pattern validation - Backend Pattern-тай тохирсон
    const usernamePattern = /^[a-zA-Z0-9._@-]+$/;
    if (!usernamePattern.test(username)) {
      return 'Хэрэглэгчийн нэр зөвхөн үсэг, тоо, цэг, дэд зураас, @ тэмдэг агуулах боломжтой';
    }

    // Password validation - Backend LoginRequestDto-тай тохирсон
    if (password.length < 6 || password.length > 100) {
      return 'Нууц үг 6-100 тэмдэгт байх ёстой';
    }

    return null; // Validation passed
  }

  // ⭐ BACKEND VALIDATION TEST - /auth/test-validation endpoint ⭐
  async testValidation(credentials: LoginCredentials): Promise<ValidationTestResponse> {
    try {
      console.log('🧪 Testing validation with backend...');

      const testRequest = {
        username: credentials.username?.trim() || '',
        password: credentials.password?.trim() || ''
      };

      const response = await apiClient.post<ValidationTestResponse>(AUTH_ENDPOINTS.TEST_VALIDATION, testRequest);
      console.log('📊 Backend validation result:', response.data);

      return response.data;
    } catch (error: unknown) {
      console.error('❌ Validation test failed:', getErrorMessage(error));
      return {
        valid: false,
        error: 'Validation тест хийхэд алдаа гарлаа',
        success: false
      };
    }
  }

  // ⭐ JSON DEBUG - Backend /auth/debug-json endpoint ⭐
  async debugRequest(data: any): Promise<any> {
    try {
      console.log('🔍 Debug request to backend:', data);

      const response = await apiClient.post(AUTH_ENDPOINTS.DEBUG_JSON, data);
      console.log('📊 Debug response:', response.data);

      return response.data;
    } catch (error: unknown) {
      console.error('❌ Debug request failed:', getErrorMessage(error));
      throw error;
    }
  }

  // ⭐ LOGOUT - Backend /auth/logout endpoint ⭐
  async logout(): Promise<void> {
    console.log('🚪 Starting logout process...');

    try {
      // Backend logout endpoint дуудах
      await apiClient.post(AUTH_ENDPOINTS.LOGOUT);
      console.log('✅ Backend logout successful');
    } catch (error: unknown) {
      console.warn('⚠️ Backend logout failed, continuing with local logout:', getErrorMessage(error));
    } finally {
      // Local storage цэвэрлэх
      localStorage.removeItem('los_auth_token');
      localStorage.removeItem('los_refresh_token');
      localStorage.removeItem('los_user_info');

      // Logout state notification
      this.notifyListeners({
        isAuthenticated: false,
        user: null,
        loading: false,
        error: null,
      });

      console.log('✅ Logout process completed');
    }
  }

  // ⭐ GET CURRENT USER - Backend /auth/me endpoint ⭐
  async getCurrentUser(): Promise<User | null> {
    try {
      if (!this.isAuthenticated()) {
        return null;
      }

      console.log('👤 Fetching current user from backend...');

      // Backend AuthController /me endpoint format
      const response = await apiClient.get<any>(AUTH_ENDPOINTS.ME);

      // Backend response format handling
      let userData = null;
      if (response.data.success && response.data.data) {
        userData = response.data.data; // Nested data format
      } else if (response.data.success && response.data.id) {
        userData = response.data; // Direct format
      }

      if (userData) {
        // Normalize user object
        const normalizedUser: User = {
          id: userData.id,
          username: userData.username,
          role: userData.role || userData.roles?.[0],
          name: userData.fullName || userData.firstName + ' ' + userData.lastName || userData.username,
          email: userData.email,
          fullName: userData.fullName,
          roles: userData.roles || [userData.role]
        };

        // Update stored user data
        localStorage.setItem('los_user_info', JSON.stringify(normalizedUser));
        console.log('✅ Current user updated:', normalizedUser.username);

        return normalizedUser;
      }

      return null;
    } catch (error: unknown) {
      console.error('❌ Failed to get current user:', getErrorMessage(error));

      // Token хүчингүй байж магадгүй
      this.logout();
      return null;
    }
  }

  // ⭐ TOKEN REFRESH - Backend /auth/refresh endpoint ⭐
  async refreshToken(): Promise<boolean> {
    try {
      const refreshToken = localStorage.getItem('los_refresh_token');
      if (!refreshToken) {
        console.warn('⚠️ No refresh token available');
        return false;
      }

      console.log('🔄 Refreshing token...');

      const response = await apiClient.post<LoginResponse>(AUTH_ENDPOINTS.REFRESH, {
        refreshToken: refreshToken
      });

      if (response.data.success && response.data.token) {
        localStorage.setItem('los_auth_token', response.data.token);
        if (response.data.refreshToken) {
          localStorage.setItem('los_refresh_token', response.data.refreshToken);
        }

        console.log('✅ Token refreshed successfully');
        return true;
      }

      return false;
    } catch (error: unknown) {
      console.error('❌ Token refresh failed:', getErrorMessage(error));
      this.logout();
      return false;
    }
  }

  // ⭐ TOKEN VALIDATION - Backend /auth/validate endpoint ⭐
  async validateToken(): Promise<boolean> {
    try {
      if (!this.getToken()) {
        return false;
      }

      console.log('🔍 Validating token with backend...');

      const response = await apiClient.get<{valid: boolean; success: boolean}>(AUTH_ENDPOINTS.VALIDATE);

      const isValid = response.data.success && response.data.valid;
      console.log(isValid ? '✅ Token is valid' : '❌ Token is invalid');

      return isValid;
    } catch (error: unknown) {
      console.error('❌ Token validation failed:', getErrorMessage(error));
      return false;
    }
  }

  // ⭐ CONNECTION TEST - Backend health endpoints ⭐
  async testConnection(): Promise<{success: boolean; message: string; endpoint?: string}> {
    console.log('🔗 Testing backend connection...');

    // Try auth health endpoint first
    try {
      const response = await apiClient.get(HEALTH_ENDPOINTS.HEALTH);
      if (response.status === 200 && response.data?.success) {
        console.log('✅ Auth service connection successful');
        return {
          success: true,
          message: 'Backend холбогдсон (Auth service)',
          endpoint: HEALTH_ENDPOINTS.HEALTH
        };
      }
    } catch (authError) {
      console.warn('⚠️ Auth health check failed, trying general health...');
    }

    // Try general health endpoint
    try {
      const response = await apiClient.get(HEALTH_ENDPOINTS.SIMPLE);
      if (response.status === 200) {
        console.log('✅ General health connection successful');
        return {
          success: true,
          message: 'Backend холбогдсон (General health)',
          endpoint: HEALTH_ENDPOINTS.SIMPLE
        };
      }
    } catch (generalError) {
      console.error('❌ General health check failed');
    }

    // All connection attempts failed
    console.error('❌ All connection attempts failed');
    return {
      success: false,
      message: 'Backend серверт холбогдохгүй байна'
    };
  }

  // ⭐ GET TEST USERS - Backend /auth/test-users endpoint ⭐
  async getTestUsers(): Promise<any[]> {
    try {
      console.log('👥 Loading test users from backend...');

      const response = await apiClient.get<{success: boolean; testUsers: any[]}>(AUTH_ENDPOINTS.TEST_USERS);

      if (response.data.success && response.data.testUsers) {
        console.log(`✅ Loaded ${response.data.testUsers.length} test users from backend`);
        return response.data.testUsers;
      }

      return DEFAULT_TEST_USERS;
    } catch (error: unknown) {
      console.warn('⚠️ Failed to get test users from backend, using default:', getErrorMessage(error));
      return DEFAULT_TEST_USERS;
    }
  }

  // Authentication status methods
  isAuthenticated(): boolean {
    const token = localStorage.getItem('los_auth_token');
    const user = localStorage.getItem('los_user_info');
    return !!(token && user);
  }

  getStoredUser(): User | null {
    const userStr = localStorage.getItem('los_user_info');
    return userStr ? JSON.parse(userStr) : null;
  }

  getToken(): string | null {
    return localStorage.getItem('los_auth_token');
  }

  // Role-based access methods - Fixed type issues
  hasRole(role: UserRole): boolean {
    const user = this.getStoredUser();
    return user?.role === role || user?.roles?.includes(role) || false;
  }

  hasAnyRole(roles: UserRole[]): boolean {
    const user = this.getStoredUser();
    return user ? roles.some(role => user.role === role || user.roles?.includes(role)) : false;
  }

  isAdmin(): boolean {
    return this.hasAnyRole(['SUPER_ADMIN' as UserRole, 'ADMIN' as UserRole]);
  }

  isManager(): boolean {
    return this.hasRole('MANAGER' as UserRole);
  }

  isLoanOfficer(): boolean {
    return this.hasRole('LOAN_OFFICER' as UserRole);
  }

  // Utility methods
  getUserDisplayName(): string {
    const user = this.getStoredUser();
    return user?.name || user?.fullName || user?.username || 'Хэрэглэгч';
  }

  getLoginInfo(): {isLoggedIn: boolean; username?: string; role?: string} {
    const user = this.getStoredUser();
    return {
      isLoggedIn: this.isAuthenticated(),
      username: user?.username,
      role: user?.role
    };
  }

  // ⭐ SERVICE INITIALIZATION ⭐
  async initialize(): Promise<void> {
    console.log('🚀 Initializing auth service...');

    if (this.isAuthenticated()) {
      try {
        // Validate existing token
        const isValid = await this.validateToken();
        if (isValid) {
          const user = await this.getCurrentUser();
          if (user) {
            this.notifyListeners({
              isAuthenticated: true,
              user,
              loading: false,
              error: null,
            });
            console.log('✅ Auth service initialized with valid session');
            return;
          }
        }

        // Token invalid, logout
        console.warn('⚠️ Invalid token during initialization, logging out');
        this.logout();
      } catch (error: unknown) {
        console.error('❌ Auth initialization failed:', getErrorMessage(error));
        this.logout();
      }
    }

    console.log('ℹ️ Auth service initialized without session');
  }

  // ⭐ DEBUG AND DIAGNOSTICS ⭐
  getDebugInfo(): any {
    return {
      apiBaseUrl: API_BASE_URL,
      endpoints: AUTH_ENDPOINTS,
      isAuthenticated: this.isAuthenticated(),
      hasToken: !!this.getToken(),
      hasRefreshToken: !!localStorage.getItem('los_refresh_token'),
      hasUser: !!this.getStoredUser(),
      userInfo: this.getStoredUser(),
      loginInfo: this.getLoginInfo(),
      timestamp: new Date().toISOString()
    };
  }

  async getSystemStatus(): Promise<any> {
    try {
      const connectionTest = await this.testConnection();
      const response = connectionTest.success ? 
        await apiClient.get(AUTH_ENDPOINTS.TEST) : null;

      return {
        connection: connectionTest,
        backend: connectionTest.success ? 'UP' : 'DOWN',
        authService: 'UP',
        serverInfo: response?.data || null,
        debugInfo: this.getDebugInfo(),
        timestamp: new Date().toISOString()
      };
    } catch (error: unknown) {
      return {
        backend: 'DOWN',
        authService: 'UP',
        error: getErrorMessage(error),
        debugInfo: this.getDebugInfo(),
        timestamp: new Date().toISOString()
      };
    }
  }
}

// ⭐ SINGLETON INSTANCE ⭐
export const authService = new AuthService();

// ⭐ BACKEND-COMPATIBLE TEST USERS ⭐ - EXPORT ADDED
export const TEST_USERS = [
  { 
    username: 'admin', 
    password: 'admin123', 
    role: 'SUPER_ADMIN', 
    name: 'Системийн админ',
    fullName: 'Системийн Админ' 
  },
  { 
    username: 'manager', 
    password: 'manager123', 
    role: 'MANAGER', 
    name: 'Салбарын менежер',
    fullName: 'Бат Болд' 
  },
  { 
    username: 'loan_officer', 
    password: 'loan123', 
    role: 'LOAN_OFFICER', 
    name: 'Зээлийн мэргэжилтэн',
    fullName: 'Цэрэн Дорж' 
  },
  { 
    username: 'reviewer', 
    password: 'admin123', 
    role: 'DOCUMENT_REVIEWER', 
    name: 'Баримт хянагч',
    fullName: 'Оюунаа Гантулга' 
  },
  { 
    username: 'customer_service', 
    password: 'admin123', 
    role: 'CUSTOMER_SERVICE', 
    name: 'Харилцагчийн үйлчилгээ',
    fullName: 'Сайхан Оюун' 
  }
];

export const DEFAULT_TEST_USERS = TEST_USERS;

export default authService;