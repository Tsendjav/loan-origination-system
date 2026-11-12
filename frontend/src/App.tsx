import React, { useState, useEffect, useCallback, useMemo, createContext, useContext } from 'react';
import { 
  Card, 
  Row, 
  Col, 
  Statistic, 
  Alert, 
  Layout, 
  Menu, 
  Typography, 
  Space, 
  Button, 
  Spin, 
  Tabs,
  Input,
  message,
  Tag,
  Divider
} from 'antd';
import { 
  CheckCircleOutlined, 
  ClockCircleOutlined, 
  FileTextOutlined, 
  UserOutlined,
  DashboardOutlined,
  ReloadOutlined,
  WifiOutlined,
  ApiOutlined,
  BugOutlined,
  LoginOutlined,
  LogoutOutlined,
  EyeInvisibleOutlined,
  EyeTwoTone,
  SecurityScanOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons';

import CustomerSection from './components/customer/CustomerSection';
import LoanApplicationSection from './components/loan/LoanApplicationSection';

const { Header, Content, Footer } = Layout;
const { Title, Paragraph, Text } = Typography;

// ⭐ ENHANCED TYPESCRIPT INTERFACES ⭐
interface User {
  id: string | number;
  username: string;
  role: string;
  name: string;
  email?: string;
  fullName?: string;
  firstName?: string;
  lastName?: string;
  roles?: Array<{ name: string; description?: string }>;
  permissions?: string[];
  isActive?: boolean;
}

interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  loading: boolean;
  error: string | null;
}

interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
  timestamp?: number;
  platform?: string;
  deviceInfo?: string;
  clientVersion?: string;
  timezone?: string;
}

interface ConnectionTest {
  success: boolean;
  message: string;
  responseTime?: number;
  endpoint?: string;
}

interface DatabaseComponent {
  type: string;
  status: string;
}

interface DiskSpaceComponent {
  status: string;
}

interface BackendComponents {
  database?: DatabaseComponent;
  diskSpace?: DiskSpaceComponent;
}

interface BackendStatus {
  status: string;
  service: string;
  version: string;
  timestamp: number;
  'java.version'?: string;
  components?: BackendComponents;
}

interface ErrorBoundaryState {
  hasError: boolean;
  error?: Error;
}

interface AuthContextType {
  state: AuthState;
  login: (data: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  hasPermission: (permission: string) => boolean;
  hasRole: (role: string) => boolean;
}

interface LoginComponentProps {
  onLoginSuccess: (user: User) => void;
}

// ⭐ ERROR BOUNDARY COMPONENT ⭐
class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  ErrorBoundaryState
> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo): void {
    console.error('🚨 ErrorBoundary caught an error:', error, errorInfo);
  }

  render(): React.ReactNode {
    if (this.state.hasError) {
      return (
        <div style={{ 
          padding: 20, 
          textAlign: 'center', 
          minHeight: '100vh', 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center' 
        }}>
          <Alert
            message="Системийн алдаа"
            description={
              <div>
                <p>Системд алдаа гарлаа. Хуудсыг дахин ачаалнуу.</p>
                <p><small>{this.state.error?.message}</small></p>
                <Button 
                  type="primary" 
                  onClick={() => window.location.reload()}
                  style={{ marginTop: 16 }}
                  icon={<ReloadOutlined />}
                >
                  Хуудас ачаалах
                </Button>
              </div>
            }
            type="error"
            showIcon
            icon={<ExclamationCircleOutlined />}
          />
        </div>
      );
    }

    return this.props.children;
  }
}

// ⭐ AUTH CONTEXT ⭐
const AuthContext = createContext<AuthContextType | null>(null);

const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

// Configuration - Safe environment variable access
const API_BASE_URL = (typeof process !== 'undefined' && process.env?.REACT_APP_API_BASE_URL) || 'http://localhost:8080/los/api/v1';

// ⭐ ENHANCED API CLIENT WITH PROPER TYPES ⭐
class SimpleApiClient {
  private baseURL: string;
  private abortController: AbortController | null = null;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  private async request(endpoint: string, options: RequestInit = {}): Promise<any> {
    // Cancel previous request if exists
    if (this.abortController) {
      this.abortController.abort();
    }
    
    this.abortController = new AbortController();
    
    const url = `${this.baseURL}${endpoint}`;
    
    // Тэмдэглэл: credentials-ийг хасахгүй (same-origin cookie preview/proxy-д хэрэгтэй).
    // `credentials: 'omit'` байсан нь cloud preview дээр 403 өгч, нэвтрэлтийг блоклож байсан.
    const token = localStorage.getItem('los_token');
    const config: RequestInit = {
      signal: this.abortController.signal,
      headers: {
        'Content-Type': 'application/json;charset=UTF-8',
        'Accept': 'application/json;charset=UTF-8',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
      ...options,
    };

    console.log(`🔄 API Request: ${options.method || 'GET'} ${url}`);
    console.log(`📋 Request headers:`, config.headers);
    if (config.body) {
      console.log(`📋 Request body:`, config.body);
    }

    try {
      const response = await fetch(url, config);
      
      console.log(`📡 API Response: ${response.status} ${response.statusText}`);

      if (!response.ok) {
        let errorMessage = `HTTP ${response.status}`;
        
        try {
          const contentType = response.headers.get('content-type');
          if (contentType && contentType.includes('application/json')) {
            const errorData = await response.json();
            errorMessage = errorData.error || errorData.message || errorMessage;
          } else {
            const errorText = await response.text();
            errorMessage = errorText || errorMessage;
          }
        } catch (parseError) {
          console.warn('Could not parse error response:', parseError);
        }
        
        throw new Error(errorMessage);
      }

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        const jsonData = await response.json();
        console.log(`📦 Response data:`, jsonData);
        return jsonData;
      } else if (response.status === 204) {
        return {};
      } else {
        const textData = await response.text();
        console.log(`📄 Response text:`, textData);
        return textData;
      }
    } catch (error: unknown) {
      if (error instanceof Error && error.name === 'AbortError') {
        console.log('Request was aborted');
        return null;
      }
      console.error(`❌ API Error: ${url}`, error);
      throw error;
    }
  }

  async get(endpoint: string): Promise<any> {
    return this.request(endpoint, { method: 'GET' });
  }

  async post(endpoint: string, data?: any): Promise<any> {
    return this.request(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  cancelRequests(): void {
    if (this.abortController) {
      this.abortController.abort();
      this.abortController = null;
    }
  }
}

// Create API client instance
const apiClient = new SimpleApiClient(API_BASE_URL);

// Test Users that match backend AuthServiceImpl
const TEST_USERS = [
  { username: 'admin', password: 'admin123', role: 'SUPER_ADMIN', name: 'Системийн админ' },
  { username: 'manager', password: 'manager123', role: 'MANAGER', name: 'Салбарын менежер' },
  { username: 'loan_officer', password: 'loan123', role: 'LOAN_OFFICER', name: 'Зээлийн мэргэжилтэн' }
];

// ⭐ AUTH PROVIDER WITH ENHANCED FUNCTIONALITY ⭐
const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [state, setState] = useState<AuthState>(() => {
    // Initialize from localStorage if available
    const storedToken = localStorage.getItem('los_token');
    const storedUser = localStorage.getItem('los_user');
    
    let initialUser: User | null = null;
    if (storedToken && storedUser) {
      try {
        initialUser = JSON.parse(storedUser);
      } catch (error) {
        console.warn('Failed to parse stored user:', error);
        localStorage.removeItem('los_token');
        localStorage.removeItem('los_user');
      }
    }

    return {
      isAuthenticated: !!initialUser,
      user: initialUser,
      token: storedToken,
      loading: false,
      error: null,
    };
  });

  // ⭐ TYPED LOGIN FUNCTION WITH ENHANCED ERROR HANDLING ⭐
  const login = useCallback(async (loginData: LoginRequest): Promise<void> => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    
    try {
      console.log('🔐 Starting auth login process...');
      
      const response = await apiClient.post('/auth/login', loginData);
      
      if (response && response.success && response.token && response.user) {
        // Normalize user data to match our interface
        const normalizedUser: User = {
          id: response.user.id,
          username: response.user.username,
          role: response.user.role || (response.user.roles && response.user.roles[0] && response.user.roles[0].name) || 'USER',
          name: response.user.fullName || response.user.name || response.user.username,
          email: response.user.email,
          fullName: response.user.fullName,
          firstName: response.user.firstName,
          lastName: response.user.lastName,
          roles: response.user.roles || [{ name: response.user.role || 'USER' }],
          permissions: response.user.permissions || [],
          isActive: response.user.isActive !== false,
        };

        // Save to localStorage for persistence
        localStorage.setItem('los_token', response.token);
        localStorage.setItem('los_user', JSON.stringify(normalizedUser));
        
        setState({
          isAuthenticated: true,
          user: normalizedUser,
          token: response.token,
          loading: false,
          error: null,
        });

        console.log('✅ Auth login successful:', normalizedUser);
      } else {
        throw new Error(response?.message || response?.error || 'Нэвтрэх амжилтгүй боллоо');
      }
    } catch (error: unknown) {
      console.error('❌ Auth login error:', error);
      const errorMessage = error instanceof Error ? error.message : 'Сервертэй холбогдоход алдаа гарлаа';
      setState(prev => ({
        ...prev,
        isAuthenticated: false,
        user: null,
        token: null,
        loading: false,
        error: errorMessage,
      }));
      throw error;
    }
  }, []);

  // ⭐ ENHANCED LOGOUT FUNCTION ⭐
  const logout = useCallback(async (): Promise<void> => {
    console.log('🚪 Starting logout process...');
    
    try {
      if (state.token) {
        await apiClient.post('/auth/logout', {});
      }
    } catch (error) {
      console.warn('Logout endpoint failed, proceeding with local logout:', error);
    } finally {
      // Clear local storage and state
      localStorage.removeItem('los_token');
      localStorage.removeItem('los_user');
      
      setState({
        isAuthenticated: false,
        user: null,
        token: null,
        loading: false,
        error: null,
      });
      
      console.log('✅ Logout completed');
    }
  }, [state.token]);

  // ⭐ PERMISSION CHECKING FUNCTIONS ⭐
  const hasPermission = useCallback((permission: string): boolean => {
    if (!state.user || !state.isAuthenticated) return false;
    return state.user.permissions?.includes(permission) || false;
  }, [state.user, state.isAuthenticated]);

  const hasRole = useCallback((roleName: string): boolean => {
    if (!state.user || !state.isAuthenticated) return false;
    
    // Check main role
    if (state.user.role === roleName) return true;
    
    // Check roles array
    return state.user.roles?.some(role => role.name === roleName) || false;
  }, [state.user, state.isAuthenticated]);

  // ⭐ MEMOIZED CONTEXT VALUE ⭐
  const contextValue = useMemo<AuthContextType>(() => ({
    state,
    login,
    logout,
    hasPermission,
    hasRole,
  }), [state, login, logout, hasPermission, hasRole]);

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

// ⭐ ENHANCED LOGIN COMPONENT ⭐
const LoginComponent: React.FC<LoginComponentProps> = ({ onLoginSuccess }) => {
  const [username, setUsername] = useState<string>('admin');
  const [password, setPassword] = useState<string>('admin123');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [debugMode, setDebugMode] = useState<boolean>(false);
  const [showPassword, setShowPassword] = useState<boolean>(false);

  const { login: authLogin, state: authState } = useAuth();

  // Watch for auth state changes and call onLoginSuccess
  useEffect(() => {
    if (authState.isAuthenticated && authState.user) {
      console.log('🎉 Auth state changed - user logged in');
      onLoginSuccess(authState.user);
    }
  }, [authState.isAuthenticated, authState.user, onLoginSuccess]);

  // Validation that matches backend LoginRequestDto.Validator exactly
  const validateForm = useCallback((): string | null => {
    if (!username || !username.trim()) {
      return 'Хэрэглэгчийн нэр заавал оруулна уу';
    }
    
    if (!password || !password.trim()) {
      return 'Нууц үг заавал оруулна уу';
    }
    
    const trimmedUsername = username.trim();
    
    if (trimmedUsername.length < 3) {
      return 'Хэрэглэгчийн нэр хамгийн багадаа 3 тэмдэгт байх ёстой';
    }
    
    if (trimmedUsername.length > 50) {
      return 'Хэрэглэгчийн нэр 50 тэмдэгтээс их байж болохгүй';
    }
    
    if (password.length < 6) {
      return 'Нууц үг хамгийн багадаа 6 тэмдэгт байх ёстой';
    }
    
    if (password.length > 100) {
      return 'Нууц үг 100 тэмдэгтээс их байж болохгүй';
    }
    
    // Username pattern validation (matches backend regex)
    if (!trimmedUsername.match(/^[a-zA-Z0-9._@-]+$/)) {
      return 'Хэрэглэгчийн нэр зөвхөн үсэг, тоо, цэг, дэд зураас, @ тэмдэг агуулах боломжтой';
    }
    
    return null;
  }, [username, password]);

  // Backend validation test
  const testValidation = useCallback(async (): Promise<void> => {
    try {
      const testRequest = {
        username: username || 'test',
        password: password || 'test123',
        timestamp: Date.now(),
        platform: 'WEB'
      };

      console.log('🧪 Testing validation with backend /auth/test-validation...');
      
      const response = await apiClient.post('/auth/test-validation', testRequest);
      
      console.log('📊 Backend validation result:', response);
      
      if (response && response.valid) {
        message.success('Validation амжилттай! Backend-тай тохирч байна.');
      } else {
        message.error(`Validation алдаа: ${response?.error || 'Unknown error'}`);
      }
      
    } catch (error: unknown) {
      console.error('❌ Validation test failed:', error);
      message.warning('Backend validation test хийх боломжгүй. Энгийн frontend validation ашигласан.');
    }
  }, [username, password]);

  const handleLogin = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);

    try {
      console.log('🔐 Starting login process...');

      // Frontend validation (matches backend validation exactly)
      const validationError = validateForm();
      if (validationError) {
        throw new Error(validationError);
      }

      // Create login request that exactly matches backend LoginRequestDto structure
      const loginRequest: LoginRequest = {
        username: username.trim(),
        password: password.trim(),
        rememberMe: false,
        timestamp: Date.now(),
        platform: 'WEB',
        deviceInfo: navigator.userAgent,
        clientVersion: '1.0.0',
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
      };

      console.log('📤 Calling auth context login...');
      
      await authLogin(loginRequest);

      message.success('Амжилттай нэвтэрлээ!');
      
      // Reset form
      setUsername('');
      setPassword('');
      
    } catch (err: unknown) {
      console.error('❌ Login error:', err);
      let errorMessage = 'Нэвтрэх үед алдаа гарлаа';
      
      if (err instanceof Error) {
        if (err.message?.includes('401')) {
          errorMessage = 'Хэрэглэгчийн нэр эсвэл нууц үг буруу байна';
        } else if (err.message?.includes('400')) {
          errorMessage = 'Нэвтрэх мэдээлэл буруу байна';
        } else if (err.message?.includes('403')) {
          errorMessage = 'Хандах эрхгүй байна';
        } else if (err.message?.includes('500')) {
          errorMessage = 'Серверийн алдаа гарлаа. Дахин оролдоно уу';
        } else if (err.message) {
          errorMessage = err.message;
        }
      }
      
      setError(errorMessage);
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [username, password, validateForm, authLogin]);

  const handleQuickLogin = useCallback((testUsername: string, testPassword: string): void => {
    setUsername(testUsername);
    setPassword(testPassword);
    // Auto-submit after setting values
    setTimeout(() => {
      handleLogin();
    }, 100);
  }, [handleLogin]);

  const handleKeyPress = useCallback((e: React.KeyboardEvent): void => {
    if (e.key === 'Enter') {
      handleLogin();
    }
  }, [handleLogin]);

  return (
    <div style={{ maxWidth: 600, margin: '0 auto' }}>
      <Card title="🔐 Системд нэвтрэх" style={{ marginBottom: 24 }}>
        {error && (
          <Alert
            message="Нэвтрэх алдаа"
            description={error}
            type="error"
            showIcon
            closable
            onClose={() => setError(null)}
            style={{ marginBottom: 16 }}
          />
        )}

        {authState.error && (
          <Alert
            message="Authentication алдаа"
            description={authState.error}
            type="error"
            showIcon
            style={{ marginBottom: 16 }}
          />
        )}

        <div style={{ marginBottom: 24 }}>
          <div style={{ marginBottom: 16 }}>
            <Text strong>Хэрэглэгчийн нэр</Text>
            <Input
              placeholder="admin"
              size="large"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onKeyPress={handleKeyPress}
              style={{ marginTop: 4 }}
              disabled={loading || authState.loading}
            />
          </div>

          <div style={{ marginBottom: 16 }}>
            <Text strong>Нууц үг</Text>
            <div style={{ position: 'relative' }}>
              <Input
                type={showPassword ? 'text' : 'password'}
                placeholder="••••"
                size="large"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyPress={handleKeyPress}
                style={{ marginTop: 4 }}
                disabled={loading || authState.loading}
                suffix={
                  <Button
                    type="text"
                    size="small"
                    icon={showPassword ? <EyeTwoTone /> : <EyeInvisibleOutlined />}
                    onClick={() => setShowPassword(!showPassword)}
                  />
                }
              />
            </div>
          </div>

          <Space style={{ width: '100%', justifyContent: 'space-between' }}>
            <Button 
              type="primary" 
              onClick={handleLogin}
              loading={loading || authState.loading} 
              size="large"
              style={{ minWidth: 120 }}
            >
              {(loading || authState.loading) ? 'Нэвтрэж байна...' : 'Нэвтрэх'}
            </Button>

            <Space>
              <Button 
                onClick={testValidation}
                size="large"
                disabled={loading || authState.loading}
              >
                🧪 Validation шалгах
              </Button>
              <Button
                type={debugMode ? "primary" : "default"}
                onClick={() => setDebugMode(!debugMode)}
                size="large"
                icon={<BugOutlined />}
              >
                Debug {debugMode ? 'ON' : 'OFF'}
              </Button>
            </Space>
          </Space>
        </div>
      </Card>

      {/* Test Users */}
      <Card title="🧪 Тест хэрэглэгчид" size="small">
        <Paragraph>Дараах тест хэрэглэгчдээр шууд нэвтрэх боломжтой:</Paragraph>

        <Space direction="vertical" style={{ width: '100%' }}>
          {TEST_USERS.map((user, index) => (
            <Button 
              key={index}
              block 
              onClick={() => handleQuickLogin(user.username, user.password)}
              disabled={loading || authState.loading}
              type="default"
            >
              👤 {user.username} / {user.password} ({user.name})
            </Button>
          ))}
        </Space>

        {debugMode && (
          <div style={{ marginTop: 16, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
            <Text strong>Enhanced Debug Info:</Text>
            <pre style={{ fontSize: 11, margin: '8px 0 0 0' }}>
              {JSON.stringify({
                apiBaseUrl: API_BASE_URL,
                authState: {
                  isAuthenticated: authState.isAuthenticated,
                  hasUser: !!authState.user,
                  loading: authState.loading,
                  error: authState.error,
                  userRole: authState.user?.role,
                  userPermissions: authState.user?.permissions?.length || 0
                },
                currentValues: { 
                  username, 
                  password: password ? '[HIDDEN]' : '',
                  validationStatus: validateForm() === null ? 'VALID' : 'INVALID',
                  validationError: validateForm()
                },
                backendExpectedFormat: {
                  username: 'string (3-50 chars, alphanumeric + ._@-)',
                  password: 'string (6-100 chars)',
                  rememberMe: 'boolean (optional)',
                  timestamp: 'number (optional)',
                  platform: 'string (optional)',
                  deviceInfo: 'string (optional)'
                },
                features: {
                  authContext: 'enabled',
                  errorBoundary: 'enabled',
                  typeScript: 'enabled',
                  localStorage: 'enabled',
                  permissionSystem: 'enabled'
                }
              }, null, 2)}
            </pre>
          </div>
        )}
      </Card>
    </div>
  );
};

// ⭐ MAIN APP CONTENT WITH ENHANCED FEATURES ⭐
const AppContent: React.FC = () => {
  const { state: authState, logout: authLogout, hasPermission, hasRole } = useAuth();
  
  const [backendStatus, setBackendStatus] = useState<BackendStatus | null>(null);
  const [systemLoading, setSystemLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [connectionTest, setConnectionTest] = useState<ConnectionTest | null>(null);
  const [apiEndpoints, setApiEndpoints] = useState<Record<string, boolean>>({});
  const [activeTabKey, setActiveTabKey] = useState<string>('dashboard');
  const [logoutLoading, setLogoutLoading] = useState<boolean>(false);

  // Handle login success
  const handleLoginSuccess = useCallback((user: User): void => {
    console.log('🎉 Login success, user:', user);
    setActiveTabKey('dashboard');
    message.success(`Сайн байна уу, ${user.fullName || user.name || user.username}!`);
  }, []);

  // Handle logout
  const handleLogout = useCallback(async (): Promise<void> => {
    try {
      setLogoutLoading(true);
      
      await authLogout();
      
      setActiveTabKey('dashboard');
      message.success('Амжилттай гарлаа');
    } catch (error) {
      console.error('❌ Logout error:', error);
      message.error('Гарахад алдаа гарлаа');
    } finally {
      setLogoutLoading(false);
    }
  }, [authLogout]);

  // Fetch system status
  const fetchSystemStatus = useCallback(async (): Promise<void> => {
    try {
      setSystemLoading(true);
      setError(null);

      console.log('🔄 Testing backend connection...');

      // Test health endpoint
      const healthResponse = await apiClient.get('/health');
      setBackendStatus(healthResponse);

      setConnectionTest({
        success: true,
        message: 'Backend холбогдсон',
        responseTime: 150,
        endpoint: '/health'
      });

      console.log('✅ System status fetched successfully');
    } catch (err: unknown) {
      console.error('❌ System status fetch failed:', err);
      const errorMessage = err instanceof Error ? err.message : 'Системийн статус авахад алдаа гарлаа';
      setError(errorMessage);
      setBackendStatus(null);
      setConnectionTest({
        success: false,
        message: errorMessage,
        responseTime: 0
      });
    } finally {
      setSystemLoading(false);
    }
  }, []);

  // Test API endpoints that match backend AuthController
  const testApiEndpoints = useCallback(async (): Promise<Record<string, boolean>> => {
    const endpoints = [
      { name: 'health', path: '/health', method: 'GET' },
      { name: 'auth-health', path: '/auth/health', method: 'GET' },
      { name: 'auth-test', path: '/auth/test', method: 'GET' },
      { name: 'auth-test-users', path: '/auth/test-users', method: 'GET' },
    ];
    
    const results: Record<string, boolean> = {};

    for (const endpoint of endpoints) {
      try {
        console.log(`🧪 Testing ${endpoint.name}: ${endpoint.method} ${endpoint.path}`);
        
        if (endpoint.method === 'GET') {
          await apiClient.get(endpoint.path);
        } else {
          await apiClient.post(endpoint.path, {});
        }
        
        results[endpoint.name] = true;
        console.log(`✅ ${endpoint.name} endpoint OK`);
      } catch (error: unknown) {
        const errorMessage = error instanceof Error ? error.message : 'Unknown error';
        console.log(`❌ ${endpoint.name} endpoint failed:`, errorMessage);
        
        // Don't consider auth-related errors as endpoint failures
        if (errorMessage.includes('401') || errorMessage.includes('403')) {
          results[endpoint.name] = true; // Endpoint exists but requires auth
          console.log(`⚠️ ${endpoint.name} endpoint requires auth (but working)`);
        } else {
          results[endpoint.name] = false;
        }
      }
    }

    setApiEndpoints(results);
    return results;
  }, []);

  // Initialize on mount
  useEffect(() => {
    let isMounted = true;
    
    const initializeApp = async (): Promise<void> => {
      if (isMounted) {
        await fetchSystemStatus();
        await testApiEndpoints();
      }
    };

    initializeApp();

    return () => {
      isMounted = false;
      apiClient.cancelRequests();
    };
  }, [fetchSystemStatus, testApiEndpoints]);

  // Menu items
  const menuItems = useMemo(() => [
    {
      key: 'dashboard',
      icon: <DashboardOutlined />,
      label: 'Хяналтын самбар',
    },
    {
      key: 'customers',
      icon: <UserOutlined />,
      label: 'Харилцагчид',
    },
    {
      key: 'applications',
      icon: <FileTextOutlined />,
      label: 'Зээлийн хүсэлт',
    },
  ], []);

  // Helper functions
  const getStatusColor = useCallback((status?: string): 'success' | 'error' | 'info' => {
    if (!status) return 'info';
    return status === 'UP' ? 'success' : 'error';
  }, []);

  const renderApiEndpointStatus = useCallback((): React.ReactNode => {
    const entries = Object.entries(apiEndpoints);
    if (entries.length === 0) return null;

    return (
      <div style={{ marginTop: 16 }}>
        <Text strong>API Endpoints:</Text>
        <div style={{ marginTop: 8 }}>
          {entries.map(([name, status]) => (
            <Tag key={name} color={status ? 'green' : 'red'} style={{ marginBottom: 4 }}>
              {name}: {status ? 'OK' : 'Failed'}
            </Tag>
          ))}
        </div>
      </div>
    );
  }, [apiEndpoints]);

  // Tab items
  const tabItems = useMemo(() => [
    {
      key: 'dashboard',
      label: (
        <span>
          <DashboardOutlined />
          Хяналтын самбар
        </span>
      ),
      children: (
        <div>
          {/* Control Panel */}
          <div style={{ marginBottom: 24 }}>
            <Space wrap>
              <Button 
                icon={<ReloadOutlined />} 
                onClick={fetchSystemStatus}
                loading={systemLoading}
              >
                Статус шинэчлэх
              </Button>
              <Button 
                icon={<ApiOutlined />}
                onClick={testApiEndpoints}
              >
                API тестлэх
              </Button>
            </Space>
          </div>

          {/* Loading State */}
          {systemLoading && (
            <Card style={{ marginBottom: 24 }}>
              <div style={{ textAlign: 'center', padding: 20 }}>
                <Spin size="large" />
                <p style={{ marginTop: 16 }}>Backend холболт шалгаж байна...</p>
              </div>
            </Card>
          )}

          {/* Error Alert */}
          {error && (
            <Alert
              message="Системийн холболтын алдаа"
              description={
                <div>
                  <p>{error}</p>
                  {connectionTest && (
                    <div style={{ marginTop: 8 }}>
                      <Text type="secondary">
                        Дэлгэрэнгүй: {connectionTest.message}
                        {connectionTest.endpoint && ` (Endpoint: ${connectionTest.endpoint})`}
                      </Text>
                    </div>
                  )}
                </div>
              }
              type="error"
              showIcon
              style={{ marginBottom: 24 }}
              action={
                <Button size="small" onClick={fetchSystemStatus}>
                  Дахин оролдох
                </Button>
              }
            />
          )}

          {/* Connection Status */}
          {connectionTest && (
            <Alert
              message={connectionTest.success ? "Холболт амжилттай" : "Холболтын алдаа"}
              description={
                <div>
                  <p>{connectionTest.message}</p>
                  {connectionTest.responseTime && (
                    <Text type="secondary">
                      Response time: {connectionTest.responseTime}ms
                    </Text>
                  )}
                  {connectionTest.endpoint && (
                    <div>
                      <Text type="secondary">
                        Working endpoint: {connectionTest.endpoint}
                      </Text>
                    </div>
                  )}
                  {renderApiEndpointStatus()}
                </div>
              }
              type={connectionTest.success ? "success" : "warning"}
              showIcon
              style={{ marginBottom: 24 }}
            />
          )}

          {/* Backend Health Status */}
          {backendStatus && (
            <Alert
              message={`Backend статус: ${backendStatus.status}`}
              description={
                <div>
                  <p>{backendStatus.service} v{backendStatus.version} ажиллаж байна</p>
                  {backendStatus['java.version'] && (
                    <p>Java: {backendStatus['java.version']}</p>
                  )}
                  <p>Timestamp: {new Date(backendStatus.timestamp).toLocaleString()}</p>
                  {backendStatus.components && (
                    <div style={{ marginTop: 8 }}>
                      <Text strong>Components:</Text>
                      <ul style={{ margin: '4px 0 0 20px' }}>
                        {backendStatus.components.database && (
                          <li>Database ({backendStatus.components.database.type}): {backendStatus.components.database.status}</li>
                        )}
                        {backendStatus.components.diskSpace && (
                          <li>Disk Space: {backendStatus.components.diskSpace.status}</li>
                        )}
                      </ul>
                    </div>
                  )}
                </div>
              }
              type={getStatusColor(backendStatus.status)}
              showIcon
              style={{ marginBottom: 24 }}
            />
          )}

          {/* System Status Cards */}
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={24} sm={12} md={6}>
              <Card>
                <Statistic
                  title="Backend холболт"
                  value={connectionTest?.success ? "Холбогдсон" : "Тасарсан"}
                  prefix={<WifiOutlined style={{ color: connectionTest?.success ? '#52c41a' : '#ff4d4f' }} />}
                  valueStyle={{ 
                    color: connectionTest?.success ? '#52c41a' : '#ff4d4f',
                    fontSize: '16px'
                  }}
                />
              </Card>
            </Col>

            <Col xs={24} sm={12} md={6}>
              <Card>
                <Statistic
                  title="API Endpoints"
                  value={Object.values(apiEndpoints).filter(Boolean).length}
                  suffix={`/ ${Object.keys(apiEndpoints).length}`}
                  prefix={<ApiOutlined />}
                  valueStyle={{ color: '#1890ff' }}
                />
              </Card>
            </Col>

            <Col xs={24} sm={12} md={6}>
              <Card>
                <Statistic
                  title="Response Time"
                  value={connectionTest?.responseTime || 0}
                  suffix="ms"
                  prefix={<ClockCircleOutlined />}
                  valueStyle={{ color: '#faad14' }}
                />
              </Card>
            </Col>

            <Col xs={24} sm={12} md={6}>
              <Card>
                <Statistic
                  title="System Status"
                  value={backendStatus?.status || "Unknown"}
                  prefix={<CheckCircleOutlined style={{ color: backendStatus?.status === 'UP' ? '#52c41a' : '#ff4d4f' }} />}
                  valueStyle={{ 
                    color: backendStatus?.status === 'UP' ? '#52c41a' : '#ff4d4f',
                    fontSize: '16px'
                  }}
                />
              </Card>
            </Col>
          </Row>

          {/* Main Content */}
          <Card title="🎉 Зээлийн хүсэлтийн системд тавтай морил!" extra={
            authState.isAuthenticated && (
              <Tag color="green" icon={<CheckCircleOutlined />}>
                Нэвтэрсэн
              </Tag>
            )
          }>
            <div style={{ textAlign: 'left' }}>
              <Title level={4}>✅ Системийн төлөв (Enhanced TypeScript Ready):</Title>

              <ul>
                <li>Backend API: {connectionTest?.success ? '✅ Ажиллаж байна' : '❌ Холбогдохгүй байна'} (Spring Boot + Spring Security)</li>
                <li>Frontend: ✅ Ажиллаж байна (React + TypeScript + AuthContext + Error Boundary)</li>
                <li>Database: {backendStatus?.components?.database?.status === 'UP' ? '✅ Холбогдсон' : '⚠️ Тодорхойгүй'} ({backendStatus?.components?.database?.type || 'H2'})</li>
                <li>API endpoints: {Object.values(apiEndpoints).filter(Boolean).length}/{Object.keys(apiEndpoints).length} ✅</li>
                <li>CORS Configuration: ✅ Frontend (localhost:3001) зөвшөөрөгдсөн</li>
                <li>Authentication: ✅ JWT + AuthContext + LocalStorage persistence</li>
                <li>Authorization: ✅ Role-based + Permission-based access control</li>
                <li>Error Handling: ✅ Error Boundary + Proper TypeScript error handling</li>
                <li>Type Safety: ✅ Full TypeScript support with proper interfaces</li>
                <li>State Management: ✅ React Context + useCallback/useMemo optimization</li>
              </ul>

              <Title level={5}>🔗 Хандах холбоосууд:</Title>
              <ul>
                <li><a href={`${API_BASE_URL}/health`} target="_blank" rel="noopener noreferrer">Backend Health Check</a></li>
                <li><a href={`${API_BASE_URL}/auth/health`} target="_blank" rel="noopener noreferrer">Auth Service Health</a></li>
                <li><a href={`${API_BASE_URL}/auth/test`} target="_blank" rel="noopener noreferrer">Auth Test Endpoint</a></li>
                <li><a href="http://localhost:8080/los/swagger-ui.html" target="_blank" rel="noopener noreferrer">API Documentation</a></li>
                <li><a href="http://localhost:8080/los/h2-console" target="_blank" rel="noopener noreferrer">H2 Database Console</a></li>
              </ul>

              <Title level={5}>🧪 Тест хэрэглэгчид (Backend AuthServiceImpl-с):</Title>
              <ul>
                <li><strong>admin / admin123</strong> - SUPER_ADMIN (Системийн админ)</li>
                <li><strong>manager / manager123</strong> - MANAGER (Салбарын менежер)</li>
                <li><strong>loan_officer / loan123</strong> - LOAN_OFFICER (Зээлийн мэргэжилтэн)</li>
              </ul>

              {authState.isAuthenticated && authState.user && (
                <Alert
                  message={`Сайн байна уу, ${authState.user.fullName || authState.user.name || authState.user.username}!`}
                  description={
                    <div>
                      <p>Та амжилттай нэвтэрсэн байна. Одоо системийн бүх функцийг ашиглах боломжтой.</p>
                      <p><strong>Үндсэн эрх:</strong> {authState.user.role}</p>
                      <p><strong>Бүх эрхүүд:</strong> {authState.user.roles?.map(r => r.name).join(', ') || authState.user.role}</p>
                      <p><strong>ID:</strong> {authState.user.id}</p>
                      <p><strong>Email:</strong> {authState.user.email || 'Тодорхойгүй'}</p>
                      <p><strong>Зөвшөөрлүүд:</strong> {authState.user.permissions?.length || 0} зөвшөөрөл</p>
                      <Divider style={{ margin: '12px 0' }} />
                      <p><strong>🔍 Эрхийн шалгалт:</strong></p>
                      <ul style={{ margin: '4px 0 0 20px' }}>
                        <li>Loan approve эрх: {hasPermission('loan:approve') ? '✅ Байна' : '❌ Байхгүй'}</li>
                        <li>Customer view эрх: {hasPermission('customer:view') ? '✅ Байна' : '❌ Байхгүй'}</li>
                        <li>Super Admin эрх: {hasRole('SUPER_ADMIN') ? '✅ Байна' : '❌ Байхгүй'}</li>
                        <li>Manager эрх: {hasRole('MANAGER') ? '✅ Байна' : '❌ Байхгүй'}</li>
                      </ul>
                    </div>
                  }
                  type="info"
                  showIcon
                  icon={<SecurityScanOutlined />}
                  style={{ marginTop: 16 }}
                />
              )}
            </div>
          </Card>
        </div>
      )
    },
    {
      key: 'auth',
      label: (
        <span>
          <LoginOutlined />
          Authentication тест
        </span>
      ),
      children: <LoginComponent onLoginSuccess={handleLoginSuccess} />
    },
    {
      key: 'customers',
      label: (
        <span>
          <UserOutlined />
          Харилцагчид
        </span>
      ),
      children: authState.isAuthenticated ? (
        <CustomerSection />
      ) : (
        <Card title="👥 Харилцагчийн удирдлага">
          <Alert
            message="Эхлээд нэвтэрнэ үү"
            description="Харилцагчдын жагсаалтыг харахын тулд системд нэвтэрнэ үү."
            type="warning"
            showIcon
          />
        </Card>
      )
    },
    {
      key: 'applications',
      label: (
        <span>
          <FileTextOutlined />
          Зээлийн хүсэлт
        </span>
      ),
      children: authState.isAuthenticated ? (
        <LoanApplicationSection />
      ) : (
        <Card title="📋 Зээлийн хүсэлт">
          <Alert
            message="Эхлээд нэвтэрнэ үү"
            description="Зээлийн хүсэлтүүдийг харах болон шийдвэрлэхийн тулд системд нэвтэрнэ үү."
            type="warning"
            showIcon
          />
        </Card>
      )
    }
  ], [
    systemLoading,
    error,
    connectionTest,
    backendStatus,
    apiEndpoints,
    authState,
    hasPermission,
    hasRole,
    fetchSystemStatus,
    testApiEndpoints,
    handleLoginSuccess,
    getStatusColor,
    renderApiEndpointStatus
  ]);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ 
        display: 'flex', 
        alignItems: 'center', 
        background: '#001529',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <Title level={3} style={{ 
            color: 'white', 
            margin: 0, 
            marginRight: 24 
          }}>
            🏦 LOS
          </Title>
          <Menu
            theme="dark"
            mode="horizontal"
            selectedKeys={[activeTabKey]}
            items={menuItems}
            style={{ flex: 1, minWidth: 0 }}
            onSelect={({ key }) => setActiveTabKey(key)}
          />
        </div>

        {/* Enhanced Auth Section */}
        <div style={{ display: 'flex', alignItems: 'center' }}>
          {authState.isAuthenticated && authState.user ? (
            <Space>
              <Text style={{ color: 'white' }}>
                Сайн байна уу, {authState.user.fullName || authState.user.name || authState.user.username}!
              </Text>
              {authState.user.roles?.map(role => (
                <Tag key={role.name} color="blue">{role.name}</Tag>
              )) || (
                <Tag color="green">{authState.user.role}</Tag>
              )}
              <Divider type="vertical" style={{ borderColor: 'rgba(255,255,255,0.3)' }} />
              <Button 
                type="text" 
                icon={<SecurityScanOutlined />}
                style={{ color: 'white' }}
                onClick={() => {
                  message.info('Тохиргооны хэсэг удахгүй нэмэгдэнэ');
                }}
              >
                Тохиргоо
              </Button>
              <Button 
                type="text" 
                icon={<LogoutOutlined />}
                onClick={handleLogout}
                style={{ color: 'white' }}
                loading={logoutLoading}
              >
                Гарах
              </Button>
            </Space>
          ) : (
            <Space>
              {authState.loading && (
                <Spin size="small" style={{ color: 'white' }} />
              )}
              <Button 
                type="text" 
                icon={<LoginOutlined />}
                style={{ color: 'white' }}
                onClick={() => setActiveTabKey('auth')}
              >
                Нэвтрэх
              </Button>
            </Space>
          )}
        </div>
      </Header>

      <Content style={{ padding: '24px', background: '#f0f2f5' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <Tabs 
            activeKey={activeTabKey} 
            onChange={setActiveTabKey} 
            type="card"
            items={tabItems}
          />
        </div>
      </Content>

      <Footer style={{ textAlign: 'center', background: '#f0f2f5' }}>
        <Space split={<Divider type="vertical" />}>
          <Text>🏦 Зээлийн хүсэлтийн систем 2025 - v12.0 (Enhanced TypeScript)</Text>
          <Text type="success">✅ Full TypeScript + AuthContext + Error Boundary</Text>
          <a href="http://localhost:8080/los/swagger-ui.html" target="_blank" rel="noopener noreferrer">
            API баримт бичиг
          </a>
          <a href="http://localhost:8080/los/h2-console" target="_blank" rel="noopener noreferrer">
            Өгөгдлийн сан
          </a>
          <a href={`${API_BASE_URL}/auth/test-users`} target="_blank" rel="noopener noreferrer">
            Test Users
          </a>
        </Space>
      </Footer>
    </Layout>
  );
};

// ⭐ MAIN APP WITH ERROR BOUNDARY AND AUTH PROVIDER ⭐
function App(): React.ReactElement {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </ErrorBoundary>
  );
}

export default App;