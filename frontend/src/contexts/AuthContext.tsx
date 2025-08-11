import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';

// ⭐ API CLIENT INTERFACE ⭐
interface ApiClient {
  get: (endpoint: string) => Promise<{ data: any }>;
  post: (endpoint: string, data?: any) => Promise<{ data: any }>;
}

// ⭐ API ENDPOINTS CONSTANTS ⭐
const API_ENDPOINTS = {
  AUTH: {
    PROFILE: '/auth/me',
    LOGIN: '/auth/login', 
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    VALIDATE: '/auth/validate'
  }
};

// ⭐ CREATE API CLIENT ⭐
const createApiClient = (): ApiClient => {
  const baseURL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/los/api/v1';
  
  return {
    get: async (endpoint: string) => {
      const token = localStorage.getItem('los_token');
      const response = await fetch(`${baseURL}${endpoint}`, {
        method: 'GET',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json;charset=UTF-8',
          'Accept': 'application/json'
        }
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      return { data };
    },
    
    post: async (endpoint: string, requestData?: any) => {
      const token = localStorage.getItem('los_token');
      const response = await fetch(`${baseURL}${endpoint}`, {
        method: 'POST',
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json;charset=UTF-8',
          'Accept': 'application/json'
        },
        body: requestData ? JSON.stringify(requestData) : undefined
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      return { data };
    }
  };
};

const apiClient = createApiClient();

// ⭐ ENHANCED USER AND AUTH TYPES ⭐
export interface User {
  id: string | number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  roles: Role[];
  permissions: string[];
  lastLoginDate?: string;
  lastLoginAt?: string;
  isActive: boolean;
}

export interface Role {
  id: number;
  name: string;
  description: string;
  permissions: Permission[];
}

export interface Permission {
  id: number;
  name: string;
  description: string;
}

// ⭐ ENHANCED LOGIN REQUEST INTERFACE ⭐ (LoginForm.tsx нийцүүлэн)
export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
  deviceInfo?: string;
  userAgent?: string;
  ipAddress?: string | null;
  timestamp?: number;
  clientVersion?: string;
  timezone?: string;
  platform?: string;
}

// Legacy support
export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

// ⭐ ENHANCED AUTH ACTIONS ⭐
type AuthAction =
  | { type: 'LOGIN_START' }
  | { type: 'LOGIN_SUCCESS'; payload: { user: User; token: string; refreshToken?: string } }
  | { type: 'LOGIN_FAILURE'; payload: string }
  | { type: 'LOGOUT' }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'CLEAR_ERROR' }
  | { type: 'UPDATE_USER'; payload: User }
  | { type: 'REFRESH_TOKEN_SUCCESS'; payload: { token: string; refreshToken?: string } }
  | { type: 'REFRESH_TOKEN_FAILURE' }
  | { type: 'CHECK_AUTH_SUCCESS'; payload: { user: User; token: string } }
  | { type: 'CHECK_AUTH_FAILURE' };

// ⭐ INITIAL STATE ⭐
const initialState: AuthState = {
  user: null,
  token: localStorage.getItem('los_token'),
  refreshToken: localStorage.getItem('los_refresh_token'),
  isAuthenticated: false,
  isLoading: true,
  error: null,
};

// ⭐ ENHANCED AUTH REDUCER ⭐
const authReducer = (state: AuthState, action: AuthAction): AuthState => {
  switch (action.type) {
    case 'LOGIN_START':
      return {
        ...state,
        isLoading: true,
        error: null,
      };
    
    case 'LOGIN_SUCCESS':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken || state.refreshToken,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      };
    
    case 'LOGIN_FAILURE':
      return {
        ...state,
        user: null,
        token: null,
        refreshToken: null,
        isAuthenticated: false,
        isLoading: false,
        error: action.payload,
      };
    
    case 'LOGOUT':
      return {
        ...initialState,
        isLoading: false,
        token: null,
        refreshToken: null,
      };
    
    case 'SET_LOADING':
      return {
        ...state,
        isLoading: action.payload,
      };
    
    case 'CLEAR_ERROR':
      return {
        ...state,
        error: null,
      };
    
    case 'UPDATE_USER':
      return {
        ...state,
        user: action.payload,
      };
    
    case 'REFRESH_TOKEN_SUCCESS':
      return {
        ...state,
        token: action.payload.token,
        refreshToken: action.payload.refreshToken || state.refreshToken,
        error: null,
      };
    
    case 'REFRESH_TOKEN_FAILURE':
      return {
        ...state,
        isAuthenticated: false,
        user: null,
        token: null,
        refreshToken: null,
        error: 'Token сэргээх амжилтгүй боллоо',
      };
    
    case 'CHECK_AUTH_SUCCESS':
      return {
        ...state,
        isAuthenticated: true,
        user: action.payload.user,
        token: action.payload.token,
        isLoading: false,
        error: null,
      };
    
    case 'CHECK_AUTH_FAILURE':
      return {
        ...state,
        isAuthenticated: false,
        user: null,
        token: null,
        refreshToken: null,
        isLoading: false,
      };
    
    default:
      return state;
  }
};

// ⭐ ENHANCED AUTH CONTEXT INTERFACE ⭐
interface AuthContextType {
  state: AuthState;
  login: (loginData: LoginRequest | LoginCredentials) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<boolean>;
  checkAuthStatus: () => Promise<void>;
  hasPermission: (permission: string) => boolean;
  hasRole: (roleName: string) => boolean;
  clearError: () => void;
}

// ⭐ CREATE CONTEXT ⭐
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// ⭐ AUTH PROVIDER COMPONENT ⭐
interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // ⭐ INITIALIZE AUTH STATE ON APP START ⭐
  useEffect(() => {
    checkAuthStatus();
  }, []);

  // ⭐ AUTO-REFRESH TOKEN SETUP ⭐
  useEffect(() => {
    if (state.token && state.refreshToken) {
      const tokenRefreshInterval = setInterval(() => {
        refreshTokenSilently();
      }, 25 * 60 * 1000); // Refresh 5 minutes before expiry

      return () => clearInterval(tokenRefreshInterval);
    }
  }, [state.token, state.refreshToken]);

  // ⭐ LOGIN FUNCTION ⭐ (Enhanced for backend compatibility)
  const login = async (loginData: LoginRequest | LoginCredentials): Promise<void> => {
    dispatch({ type: 'LOGIN_START' });
    
    try {
      console.log('🔐 AuthContext: Starting login process...');
      const response = await apiClient.post(API_ENDPOINTS.AUTH.LOGIN, loginData);
      
      console.log('📡 AuthContext: Login response:', response);
      
      if (response.data && response.data.success) {
        const userData = response.data.user;
        const user: User = {
          id: userData.id,
          username: userData.username,
          email: userData.email || '',
          firstName: userData.firstName || '',
          lastName: userData.lastName || '',
          fullName: userData.fullName || userData.username,
          roles: userData.roles || [{ 
            id: 1, 
            name: userData.role || 'USER', 
            description: 'Default role',
            permissions: [] 
          }],
          permissions: userData.permissions || [],
          isActive: true,
          lastLoginAt: userData.lastLoginAt,
        };

        // Save to localStorage
        localStorage.setItem('los_token', response.data.token);
        if (response.data.refreshToken) {
          localStorage.setItem('los_refresh_token', response.data.refreshToken);
        }
        localStorage.setItem('los_user', JSON.stringify(user));
        
        dispatch({
          type: 'LOGIN_SUCCESS',
          payload: { 
            user, 
            token: response.data.token,
            refreshToken: response.data.refreshToken 
          },
        });
        
        console.log('✅ AuthContext: Login successful');
      } else {
        throw new Error(response.data?.error || response.data?.message || 'Нэвтрэх амжилтгүй боллоо');
      }
    } catch (error: any) {
      console.error('❌ AuthContext: Login error:', error);
      const errorMessage = error.message || 'Сервертэй холбогдоход алдаа гарлаа';
      dispatch({
        type: 'LOGIN_FAILURE',
        payload: errorMessage,
      });
      throw error;
    }
  };

  // ⭐ LOGOUT FUNCTION ⭐
  const logout = async (): Promise<void> => {
    try {
      if (state.token) {
        await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);
      }
    } catch (error) {
      console.warn('Logout endpoint failed, proceeding with local logout:', error);
    } finally {
      // Clear local storage
      localStorage.removeItem('los_token');
      localStorage.removeItem('los_refresh_token');
      localStorage.removeItem('los_user');
      localStorage.removeItem('los_remember_username');
      localStorage.removeItem('los_remember_me');
      
      dispatch({ type: 'LOGOUT' });
    }
  };

  // ⭐ REFRESH TOKEN FUNCTION ⭐
  const refreshToken = async (): Promise<boolean> => {
    try {
      if (!state.refreshToken) {
        throw new Error('Refresh token байхгүй');
      }

      const response = await apiClient.post(API_ENDPOINTS.AUTH.REFRESH, {
        refreshToken: state.refreshToken
      });

      if (response.data.success) {
        localStorage.setItem('los_token', response.data.token);
        if (response.data.refreshToken) {
          localStorage.setItem('los_refresh_token', response.data.refreshToken);
        }

        dispatch({
          type: 'REFRESH_TOKEN_SUCCESS',
          payload: {
            token: response.data.token,
            refreshToken: response.data.refreshToken,
          },
        });

        return true;
      } else {
        throw new Error(response.data.error || 'Token сэргээх амжилтгүй');
      }
    } catch (error) {
      console.error('Token refresh error:', error);
      dispatch({ type: 'REFRESH_TOKEN_FAILURE' });
      
      // Clear tokens and redirect to login
      localStorage.removeItem('los_token');
      localStorage.removeItem('los_refresh_token');
      localStorage.removeItem('los_user');
      
      return false;
    }
  };

  // ⭐ SILENT TOKEN REFRESH ⭐
  const refreshTokenSilently = async () => {
    try {
      await refreshToken();
    } catch (error) {
      console.warn('Silent token refresh failed:', error);
    }
  };

  // ⭐ CHECK AUTH STATUS ⭐
  const checkAuthStatus = async () => {
    try {
      dispatch({ type: 'SET_LOADING', payload: true });

      const storedToken = localStorage.getItem('los_token');
      const storedUser = localStorage.getItem('los_user');

      if (!storedToken || !storedUser) {
        dispatch({ type: 'CHECK_AUTH_FAILURE' });
        return;
      }

      try {
        // Try to validate token with backend
        const response = await apiClient.get(API_ENDPOINTS.AUTH.VALIDATE);
        
        if (response.data.success && response.data.valid) {
          const user = JSON.parse(storedUser);
          dispatch({
            type: 'CHECK_AUTH_SUCCESS',
            payload: {
              user,
              token: storedToken,
            },
          });
        } else {
          throw new Error('Token validation failed');
        }
      } catch (error) {
        console.warn('Token validation failed, but proceeding with stored data:', error);
        // If backend validation fails, still proceed with stored data
        try {
          const user = JSON.parse(storedUser);
          dispatch({
            type: 'CHECK_AUTH_SUCCESS',
            payload: {
              user,
              token: storedToken,
            },
          });
        } catch (parseError) {
          dispatch({ type: 'CHECK_AUTH_FAILURE' });
        }
      }
    } catch (error) {
      console.error('Auth check error:', error);
      dispatch({ type: 'CHECK_AUTH_FAILURE' });
    }
  };

  // ⭐ PERMISSION CHECKING ⭐
  const hasPermission = (permission: string): boolean => {
    if (!state.user) return false;
    return state.user.permissions.includes(permission);
  };

  // ⭐ ROLE CHECKING ⭐
  const hasRole = (roleName: string): boolean => {
    if (!state.user) return false;
    return state.user.roles.some(role => role.name === roleName);
  };

  // ⭐ CLEAR ERROR ⭐
  const clearError = (): void => {
    dispatch({ type: 'CLEAR_ERROR' });
  };

  const contextValue: AuthContextType = {
    state,
    login,
    logout,
    refreshToken,
    checkAuthStatus,
    hasPermission,
    hasRole,
    clearError,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

// ⭐ CUSTOM HOOK TO USE AUTH CONTEXT ⭐
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// ⭐ PERMISSION CONSTANTS ⭐
export const PERMISSIONS = {
  // Customer permissions
  CUSTOMER_VIEW: 'customer:view',
  CUSTOMER_CREATE: 'customer:create',
  CUSTOMER_UPDATE: 'customer:update',
  CUSTOMER_DELETE: 'customer:delete',
  
  // Loan application permissions
  LOAN_VIEW: 'loan:view',
  LOAN_CREATE: 'loan:create',
  LOAN_UPDATE: 'loan:update',
  LOAN_DELETE: 'loan:delete',
  LOAN_APPROVE: 'loan:approve',
  LOAN_REJECT: 'loan:reject',
  
  // Document permissions
  DOCUMENT_VIEW: 'document:view',
  DOCUMENT_UPLOAD: 'document:upload',
  DOCUMENT_DELETE: 'document:delete',
  DOCUMENT_DOWNLOAD: 'document:download',
  
  // System permissions
  SYSTEM_ADMIN: 'system:admin',
  USER_MANAGE: 'user:manage',
  ROLE_MANAGE: 'role:manage',
  AUDIT_VIEW: 'audit:view',
  
  // Reporting permissions
  REPORT_VIEW: 'report:view',
  REPORT_EXPORT: 'report:export',
};

// ⭐ ROLE CONSTANTS ⭐
export const ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  LOAN_OFFICER: 'LOAN_OFFICER',
  CUSTOMER_SERVICE: 'CUSTOMER_SERVICE',
  AUDITOR: 'AUDITOR',
  USER: 'USER',
};

// ⭐ HIGHER-ORDER COMPONENT FOR PROTECTED ROUTES ⭐
interface WithAuthProps {
  requiredPermission?: string;
  requiredRole?: string;
  fallback?: React.ComponentType;
}

export const withAuth = <P extends object>(
  Component: React.ComponentType<P>,
  options: WithAuthProps = {}
) => {
  const AuthenticatedComponent: React.FC<P> = (props) => {
    const { state, hasPermission, hasRole } = useAuth();
    
    if (state.isLoading) {
      return (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <span className="ml-2">Ачаалж байна...</span>
        </div>
      );
    }
    
    if (!state.isAuthenticated) {
      if (options.fallback) {
        const FallbackComponent = options.fallback;
        return <FallbackComponent />;
      }
      return (
        <div className="text-center p-8">
          <h3 className="text-lg font-medium text-gray-900 mb-2">Нэвтрэх шаардлагатай</h3>
          <p className="text-gray-600">Энэ хуудсыг үзэхийн тулд нэвтэрнэ үү.</p>
        </div>
      );
    }
    
    if (options.requiredPermission && !hasPermission(options.requiredPermission)) {
      return (
        <div className="text-center p-8">
          <h3 className="text-lg font-medium text-red-600 mb-2">Эрх хүрэхгүй</h3>
          <p className="text-gray-600">Танд энэ үйлдлийг гүйцэтгэх эрх байхгүй байна.</p>
        </div>
      );
    }
    
    if (options.requiredRole && !hasRole(options.requiredRole)) {
      return (
        <div className="text-center p-8">
          <h3 className="text-lg font-medium text-red-600 mb-2">Эрх хүрэхгүй</h3>
          <p className="text-gray-600">Танд энэ хуудсыг үзэх эрх байхгүй байна.</p>
        </div>
      );
    }
    
    return <Component {...props} />;
  };
  
  return AuthenticatedComponent;
};

export default AuthContext;