import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
// Fix API_ENDPOINTS import - create a proper API client interface
interface ApiClient {
  get: (endpoint: string) => Promise<{ data: any }>;
  post: (endpoint: string, data?: any) => Promise<{ data: any }>;
}

// Create API endpoints constants
const API_ENDPOINTS = {
  AUTH: {
    PROFILE: '/auth/me',
    LOGIN: '/auth/login', 
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh'
  }
};

// Mock API client - replace with actual implementation
const createApiClient = (): ApiClient => ({
  get: async (endpoint: string) => {
    // Mock implementation
    const response = await fetch(`${process.env.REACT_APP_API_URL || 'http://localhost:8080/los/api/v1'}${endpoint}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('los_token')}`,
        'Content-Type': 'application/json'
      }
    });
    return { data: await response.json() };
  },
  post: async (endpoint: string, data?: any) => {
    // Mock implementation
    const response = await fetch(`${process.env.REACT_APP_API_URL || 'http://localhost:8080/los/api/v1'}${endpoint}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('los_token')}`,
        'Content-Type': 'application/json'
      },
      body: data ? JSON.stringify(data) : undefined
    });
    return { data: await response.json() };
  }
});

const apiClient = createApiClient();

// User and Auth types
export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: Role[];
  permissions: string[];
  lastLoginDate?: string;
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

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

// Auth actions
type AuthAction =
  | { type: 'LOGIN_START' }
  | { type: 'LOGIN_SUCCESS'; payload: { user: User; token: string } }
  | { type: 'LOGIN_FAILURE'; payload: string }
  | { type: 'LOGOUT' }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'CLEAR_ERROR' }
  | { type: 'UPDATE_USER'; payload: User };

// Initial state
const initialState: AuthState = {
  user: null,
  token: localStorage.getItem('los_token'),
  isAuthenticated: false,
  isLoading: true,
  error: null,
};

// Auth reducer
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
        isAuthenticated: true,
        isLoading: false,
        error: null,
      };
    
    case 'LOGIN_FAILURE':
      return {
        ...state,
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: action.payload,
      };
    
    case 'LOGOUT':
      return {
        ...state,
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
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
    
    default:
      return state;
  }
};

// Auth context interface
interface AuthContextType {
  state: AuthState;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
  hasPermission: (permission: string) => boolean;
  hasRole: (roleName: string) => boolean;
  clearError: () => void;
}

// Create context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Auth provider component
interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Initialize auth state on app start
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('los_token');
      const savedUser = localStorage.getItem('los_user');
      
      if (token && savedUser) {
        try {
          // Verify token with backend
          const response = await apiClient.get(API_ENDPOINTS.AUTH.PROFILE);
          const user = response.data.data;
          
          dispatch({
            type: 'LOGIN_SUCCESS',
            payload: { user, token },
          });
        } catch (error) {
          // Token is invalid, clear storage
          localStorage.removeItem('los_token');
          localStorage.removeItem('los_user');
          dispatch({ type: 'LOGOUT' });
        }
      } else {
        dispatch({ type: 'SET_LOADING', payload: false });
      }
    };

    initializeAuth();
  }, []);

  // Login function
  const login = async (credentials: LoginCredentials): Promise<void> => {
    dispatch({ type: 'LOGIN_START' });
    
    try {
      const response = await apiClient.post(API_ENDPOINTS.AUTH.LOGIN, credentials);
      const { user, token } = response.data.data;
      
      // Save to localStorage
      localStorage.setItem('los_token', token);
      localStorage.setItem('los_user', JSON.stringify(user));
      
      dispatch({
        type: 'LOGIN_SUCCESS',
        payload: { user, token },
      });
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Нэвтрэх үед алдаа гарлаа';
      dispatch({
        type: 'LOGIN_FAILURE',
        payload: errorMessage,
      });
      throw error;
    }
  };

  // Logout function
  const logout = async (): Promise<void> => {
    try {
      // Call logout endpoint
      await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);
    } catch (error) {
      console.warn('Logout endpoint failed, proceeding with local logout');
    } finally {
      // Clear local storage
      localStorage.removeItem('los_token');
      localStorage.removeItem('los_user');
      
      dispatch({ type: 'LOGOUT' });
    }
  };

  // Refresh token function
  const refreshToken = async (): Promise<void> => {
    try {
      const response = await apiClient.post(API_ENDPOINTS.AUTH.REFRESH);
      const { token } = response.data.data;
      
      localStorage.setItem('los_token', token);
      
      // Update token in state (user remains the same)
      if (state.user) {
        dispatch({
          type: 'LOGIN_SUCCESS',
          payload: { user: state.user, token },
        });
      }
    } catch (error) {
      // Refresh failed, logout user
      logout();
      throw error;
    }
  };

  // Check if user has specific permission
  const hasPermission = (permission: string): boolean => {
    if (!state.user) return false;
    return state.user.permissions.includes(permission);
  };

  // Check if user has specific role
  const hasRole = (roleName: string): boolean => {
    if (!state.user) return false;
    return state.user.roles.some(role => role.name === roleName);
  };

  // Clear error
  const clearError = (): void => {
    dispatch({ type: 'CLEAR_ERROR' });
  };

  const contextValue: AuthContextType = {
    state,
    login,
    logout,
    refreshToken,
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

// Custom hook to use auth context
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// Permission constants
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

// Role constants
export const ROLES = {
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  LOAN_OFFICER: 'LOAN_OFFICER',
  CUSTOMER_SERVICE: 'CUSTOMER_SERVICE',
  AUDITOR: 'AUDITOR',
};

// Higher-order component for protected routes
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
      return <div className="flex justify-center items-center h-64">Ачаалж байна...</div>;
    }
    
    if (!state.isAuthenticated) {
      if (options.fallback) {
        const FallbackComponent = options.fallback;
        return <FallbackComponent />;
      }
      return <div className="text-center p-4">Нэвтрэх шаардлагатай</div>;
    }
    
    if (options.requiredPermission && !hasPermission(options.requiredPermission)) {
      return <div className="text-center p-4">Эрх хүрэхгүй байна</div>;
    }
    
    if (options.requiredRole && !hasRole(options.requiredRole)) {
      return <div className="text-center p-4">Эрх хүрэхгүй байна</div>;
    }
    
    return <Component {...props} />;
  };
  
  return AuthenticatedComponent;
};

export default AuthContext;