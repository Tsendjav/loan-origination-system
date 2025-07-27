// frontend/src/services/authService.ts
import { apiClient } from './apiClient';
import { ENDPOINTS } from './apiConfig';

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface User {
  id: string;
  username: string;
  role: string;
  name: string;
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
    const token = localStorage.getItem('auth_token');
    const userStr = localStorage.getItem('user');
    
    return {
      isAuthenticated: !!token,
      user: userStr ? JSON.parse(userStr) : null,
      loading: false,
      error: null,
    };
  }

  // Login method with better validation
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    console.log('🔐 Attempting login for:', credentials.username);
    
    // Validate credentials before sending
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
    
    try {
      // Notify listeners that login is in progress
      this.notifyListeners({
        isAuthenticated: false,
        user: null,
        loading: true,
        error: null,
      });

      console.log('📤 Sending login request with credentials:', { 
        username: cleanCredentials.username, 
        password: '[HIDDEN]' 
      });

      const response = await apiClient.post<LoginResponse>(
        ENDPOINTS.AUTH.LOGIN,
        cleanCredentials
      );

      console.log('✅ Login response:', { 
        ...response, 
        token: response.token ? '[HIDDEN]' : undefined 
      });

      if (response.success && response.token && response.user) {
        // Store auth data
        localStorage.setItem('auth_token', response.token);
        localStorage.setItem('user', JSON.stringify(response.user));

        // Notify listeners of successful login
        this.notifyListeners({
          isAuthenticated: true,
          user: response.user,
          loading: false,
          error: null,
        });

        console.log('🎉 Login successful:', response.user.username);
        return response;
      } else {
        throw new Error(response.message || 'Нэвтрэхэд алдаа гарлаа');
      }
    } catch (error) {
      console.error('❌ Login failed:', error);
      
      let errorMessage = 'Нэвтрэхэд алдаа гарлаа';
      
      if (error instanceof Error) {
        errorMessage = error.message;
      } else if (typeof error === 'string') {
        errorMessage = error;
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

  // Logout method
  async logout(): Promise<void> {
    console.log('🚪 Logging out...');
    
    try {
      // Try to call logout endpoint
      await apiClient.post(ENDPOINTS.AUTH.LOGOUT);
    } catch (error) {
      console.warn('Logout endpoint failed, but continuing with local logout:', error);
    } finally {
      // Always clear local storage
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user');

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

  // Get current user from server
  async getCurrentUser(): Promise<User | null> {
    try {
      if (!this.isAuthenticated()) {
        return null;
      }

      const user = await apiClient.get<User>(ENDPOINTS.AUTH.ME);
      
      // Update stored user data
      localStorage.setItem('user', JSON.stringify(user));
      
      return user;
    } catch (error) {
      console.error('Failed to get current user:', error);
      
      // If getting current user fails, probably token is invalid
      this.logout();
      return null;
    }
  }

  // Check if user is authenticated
  isAuthenticated(): boolean {
    const token = localStorage.getItem('auth_token');
    return !!token;
  }

  // Get stored user data
  getStoredUser(): User | null {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  // Get stored token
  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  // Check if user has specific role
  hasRole(role: string): boolean {
    const user = this.getStoredUser();
    return user?.role === role;
  }

  // Check if user has any of the specified roles
  hasAnyRole(roles: string[]): boolean {
    const user = this.getStoredUser();
    return user ? roles.includes(user.role) : false;
  }

  // Initialize auth service (call this when app starts)
  async initialize(): Promise<void> {
    console.log('🚀 Initializing auth service...');
    
    if (this.isAuthenticated()) {
      try {
        // Verify token is still valid by fetching current user
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

  // Refresh token (placeholder for future implementation)
  async refreshToken(): Promise<boolean> {
    // TODO: Implement token refresh logic
    console.log('🔄 Token refresh not implemented yet');
    return false;
  }

  // Test login with default users
  async testLogin(): Promise<void> {
    console.log('🧪 Testing login with default user...');
    
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
}

// Create singleton instance
export const authService = new AuthService();

// Default users for testing
export const DEFAULT_USERS = [
  { username: 'admin', password: 'admin123', role: 'ADMIN', name: 'Админ хэрэглэгч' },
  { username: 'loan_officer', password: 'loan123', role: 'LOAN_OFFICER', name: 'Зээлийн ажилтан' },
  { username: 'manager', password: 'manager123', role: 'MANAGER', name: 'Менежер' },
];