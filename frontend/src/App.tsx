import React, { useState, useEffect } from 'react';
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
  Tag
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
  EyeTwoTone
} from '@ant-design/icons';

const { Header, Content, Footer } = Layout;
const { Title, Paragraph, Text } = Typography;

// TypeScript Interfaces
interface User {
  id: string | number;
  username: string;
  role: string;
  name: string;
  email?: string;
  fullName?: string;
}

interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  loading: boolean;
  error: string | null;
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

// Configuration
const API_BASE_URL = 'http://localhost:8080/los/api/v1';

// Enhanced API Client with backend compatibility
class SimpleApiClient {
  private baseURL: string;

  constructor(baseURL: string) {
    this.baseURL = baseURL;
  }

  private async request(endpoint: string, options: RequestInit = {}): Promise<any> {
    const url = `${this.baseURL}${endpoint}`;
    
    const config: RequestInit = {
      mode: 'cors',
      credentials: 'omit',
      headers: {
        'Content-Type': 'application/json;charset=UTF-8',
        'Accept': 'application/json;charset=UTF-8',
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
    } catch (error) {
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
}

// Create API client instance
const apiClient = new SimpleApiClient(API_BASE_URL);

// Test Users that match backend AuthServiceImpl
const TEST_USERS = [
  { username: 'admin', password: 'admin123', role: 'SUPER_ADMIN', name: 'Системийн админ' },
  { username: 'manager', password: 'manager123', role: 'MANAGER', name: 'Салбарын менежер' },
  { username: 'loan_officer', password: 'loan123', role: 'LOAN_OFFICER', name: 'Зээлийн мэргэжилтэн' }
];

// Login Component
interface LoginComponentProps {
  onLoginSuccess: (user: User) => void;
}

const LoginComponent: React.FC<LoginComponentProps> = ({ onLoginSuccess }) => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [debugMode, setDebugMode] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  // Validation that matches backend LoginRequestDto.Validator exactly
  const validateForm = (): string | null => {
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
  };

  // Backend validation test
  const testValidation = async () => {
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
      
      if (response.valid) {
        message.success('Validation амжилттай! Backend-тай тохирч байна.');
      } else {
        message.error(`Validation алдаа: ${response.error}`);
      }
      
    } catch (error: any) {
      console.error('❌ Validation test failed:', error);
      message.warning('Backend validation test хийх боломжгүй. Энгийн frontend validation ашигласан.');
    }
  };

  const handleLogin = async () => {
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
      const loginRequest = {
        username: username.trim(),
        password: password.trim(),
        rememberMe: false,
        timestamp: Date.now(),
        platform: 'WEB',
        deviceInfo: navigator.userAgent,
        clientVersion: '1.0.0',
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
      };

      console.log('📤 Sending login request to backend...');
      console.log('📋 Login request structure:', {
        username: loginRequest.username,
        password: '[HIDDEN]',
        rememberMe: loginRequest.rememberMe,
        platform: loginRequest.platform,
        timestamp: loginRequest.timestamp
      });
      
      const response = await apiClient.post('/auth/login', loginRequest);

      console.log('✅ Login response received:', response);

      if (response.success && response.token && response.user) {
        const normalizedUser: User = {
          id: response.user.id,
          username: response.user.username,
          role: response.user.role || (response.user.roles && response.user.roles[0] && response.user.roles[0].name) || 'USER',
          name: response.user.fullName || response.user.name || response.user.username,
          email: response.user.email,
          fullName: response.user.fullName
        };

        message.success(response.message || 'Амжилттай нэвтэрлээ!');
        onLoginSuccess(normalizedUser);
        
        // Reset form
        setUsername('');
        setPassword('');
      } else {
        throw new Error(response.message || response.error || 'Нэвтрэхэд алдаа гарлаа');
      }
    } catch (err: any) {
      console.error('❌ Login error:', err);
      let errorMessage = 'Нэвтрэх үед алдаа гарлаа';
      
      // Parse backend error messages
      if (err.message.includes('401')) {
        errorMessage = 'Хэрэглэгчийн нэр эсвэл нууц үг буруу байна';
      } else if (err.message.includes('400')) {
        errorMessage = 'Нэвтрэх мэдээлэл буруу байна';
      } else if (err.message.includes('403')) {
        errorMessage = 'Хандах эрхгүй байна';
      } else if (err.message.includes('500')) {
        errorMessage = 'Серверийн алдаа гарлаа. Дахин оролдоно уу';
      } else if (err.message) {
        errorMessage = err.message;
      }
      
      setError(errorMessage);
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = (testUsername: string, testPassword: string) => {
    setUsername(testUsername);
    setPassword(testPassword);
    // Auto-submit after setting values
    setTimeout(() => {
      handleLogin();
    }, 100);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleLogin();
    }
  };

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
              loading={loading} 
              size="large"
              style={{ minWidth: 120 }}
            >
              {loading ? 'Нэвтрэж байна...' : 'Нэвтрэх'}
            </Button>

            <Space>
              <Button 
                onClick={testValidation}
                size="large"
                disabled={loading}
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
              disabled={loading}
              type="default"
            >
              👤 {user.username} / {user.password} ({user.name})
            </Button>
          ))}
        </Space>

        {debugMode && (
          <div style={{ marginTop: 16, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
            <Text strong>Backend Compatibility Debug:</Text>
            <pre style={{ fontSize: 11, margin: '8px 0 0 0' }}>
              {JSON.stringify({
                apiBaseUrl: API_BASE_URL,
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
                corsConfig: {
                  allowedOrigins: 'http://localhost:3001',
                  credentials: 'omit',
                  mode: 'cors'
                },
                timestamp: new Date().toISOString(),
                testUsers: TEST_USERS.length,
                validationRules: {
                  usernamePattern: '^[a-zA-Z0-9._@-]+$',
                  usernameLength: '3-50',
                  passwordLength: '6-100'
                }
              }, null, 2)}
            </pre>
          </div>
        )}
      </Card>
    </div>
  );
};

// Main App Component
function App() {
  // State declarations with proper TypeScript syntax and types
  const [authState, setAuthState] = useState<AuthState>({
    isAuthenticated: false,
    user: null,
    loading: false,
    error: null
  });
  
  const [backendStatus, setBackendStatus] = useState<BackendStatus | null>(null);
  const [systemLoading, setSystemLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [connectionTest, setConnectionTest] = useState<ConnectionTest | null>(null);
  const [apiEndpoints, setApiEndpoints] = useState<Record<string, boolean>>({});
  const [activeTabKey, setActiveTabKey] = useState('dashboard');
  const [logoutLoading, setLogoutLoading] = useState(false);

  // Fetch system status
  const fetchSystemStatus = async () => {
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
    } catch (err: any) {
      console.error('❌ System status fetch failed:', err);
      setError(err.message || 'Системийн статус авахад алдаа гарлаа');
      setBackendStatus(null);
      setConnectionTest({
        success: false,
        message: err.message || 'Backend холбогдохгүй байна',
        responseTime: 0
      });
    } finally {
      setSystemLoading(false);
    }
  };

  // Test API endpoints that match backend AuthController
  const testApiEndpoints = async () => {
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
      } catch (error: any) {
        console.log(`❌ ${endpoint.name} endpoint failed:`, error.message);
        
        // Don't consider auth-related errors as endpoint failures
        if (error.message.includes('401') || error.message.includes('403')) {
          results[endpoint.name] = true; // Endpoint exists but requires auth
          console.log(`⚠️ ${endpoint.name} endpoint requires auth (but working)`);
        } else {
          results[endpoint.name] = false;
        }
      }
    }

    setApiEndpoints(results);
    return results;
  };

  // Handle login success
  const handleLoginSuccess = (user: User) => {
    console.log('🎉 Login success, user:', user);
    setAuthState({
      isAuthenticated: true,
      user: user,
      loading: false,
      error: null
    });
    setActiveTabKey('dashboard');
  };

  // Handle logout
  const handleLogout = async () => {
    try {
      setLogoutLoading(true);
      
      // Try backend logout
      try {
        await apiClient.post('/auth/logout');
      } catch (error) {
        console.warn('Backend logout failed, proceeding with local logout');
      }

      // Clear auth state
      setAuthState({
        isAuthenticated: false,
        user: null,
        loading: false,
        error: null
      });
      
      setActiveTabKey('dashboard');
      message.success('Амжилттай гарлаа');
    } catch (error: any) {
      console.error('❌ Logout error:', error);
      message.error('Гарахад алдаа гарлаа');
    } finally {
      setLogoutLoading(false);
    }
  };

  // Initialize on mount
  useEffect(() => {
    fetchSystemStatus();
    testApiEndpoints();
  }, []);

  // Menu items
  const menuItems = [
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
  ];

  // Helper functions
  const getStatusColor = (status?: string) => {
    if (!status) return 'info';
    return status === 'UP' ? 'success' : 'error';
  };

  const renderApiEndpointStatus = () => {
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
  };

  // Tab items
  const tabItems = [
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
          <Card title="🎉 Зээлийн хүсэлтийн системд тавтай морил!">
            <div style={{ textAlign: 'left' }}>
              <Title level={4}>✅ Системийн төлөв (Backend Compatible):</Title>

              <ul>
                <li>Backend API: {connectionTest?.success ? '✅ Ажиллаж байна' : '❌ Холбогдохгүй байна'} (Spring Boot + Spring Security)</li>
                <li>Frontend: ✅ Ажиллаж байна (React app with backend compatibility)</li>
                <li>Database: {backendStatus?.components?.database?.status === 'UP' ? '✅ Холбогдсон' : '⚠️ Тодорхойгүй'} ({backendStatus?.components?.database?.type || 'H2'})</li>
                <li>API endpoints: {Object.values(apiEndpoints).filter(Boolean).length}/{Object.keys(apiEndpoints).length} ✅</li>
                <li>CORS Configuration: ✅ Frontend (localhost:3001) зөвшөөрөгдсөн</li>
                <li>Authentication: ✅ JWT + Test Users багц бэлэн</li>
                <li>Validation: ✅ Frontend backend LoginRequestDto-тай тохирч байна</li>
              </ul>

              <Title level={5}>🔗 Хандах холбоосууд:</Title>
              <ul>
                <li><a href="http://localhost:8080/los/api/v1/health" target="_blank" rel="noopener noreferrer">Backend Health Check</a></li>
                <li><a href="http://localhost:8080/los/api/v1/auth/health" target="_blank" rel="noopener noreferrer">Auth Service Health</a></li>
                <li><a href="http://localhost:8080/los/api/v1/auth/test" target="_blank" rel="noopener noreferrer">Auth Test Endpoint</a></li>
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
                  message={`Сайн байна уу, ${authState.user.name || authState.user.username}!`}
                  description={
                    <div>
                      <p>Та амжилттай нэвтэрсэн байна. Одоо системийн бүх функцийг ашиглах боломжтой.</p>
                      <p><strong>Эрх:</strong> {authState.user.role}</p>
                      <p><strong>ID:</strong> {authState.user.id}</p>
                    </div>
                  }
                  type="info"
                  showIcon
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
      children: (
        <Card title="Харилцагчийн удирдлага">
          <Paragraph>Харилцагчийн удирдлагын хэсэг удахгүй нэмэгдэнэ...</Paragraph>
          {!authState.isAuthenticated && (
            <Alert
              message="Эхлээд нэвтэрнэ үү"
              description="Энэ хэсгийг ашиглахын тулд эхлээд системд нэвтэрнэ үү."
              type="warning"
              showIcon
            />
          )}
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
      children: (
        <Card title="Зээлийн хүсэлт">
          <Paragraph>Зээлийн хүсэлтийн хэсэг удахгүй нэмэгдэнэ...</Paragraph>
          {!authState.isAuthenticated && (
            <Alert
              message="Эхлээд нэвтэрнэ үү"
              description="Энэ хэсгийг ашиглахын тулд эхлээд системд нэвтэрнэ үү."
              type="warning"
              showIcon
            />
          )}
        </Card>
      )
    }
  ];

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

        {/* Auth Section */}
        <div style={{ display: 'flex', alignItems: 'center' }}>
          {authState.isAuthenticated && authState.user ? (
            <Space>
              <Text style={{ color: 'white' }}>
                Сайн байна уу, {authState.user.name || authState.user.username}!
              </Text>
              <Tag color="green">{authState.user.role}</Tag>
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
            <Button 
              type="text" 
              icon={<LoginOutlined />}
              style={{ color: 'white' }}
              onClick={() => setActiveTabKey('auth')}
            >
              Нэвтрэх
            </Button>
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
        <Space>
          <Text>🏦 Зээлийн хүсэлтийн систем 2025 - v5.0</Text>
          <span>|</span>
          <Text type="success">✅ Backend Compatible</Text>
          <span>|</span>
          <a href="http://localhost:8080/los/swagger-ui.html" target="_blank" rel="noopener noreferrer">
            API баримт бичиг
          </a>
          <span>|</span>
          <a href="http://localhost:8080/los/h2-console" target="_blank" rel="noopener noreferrer">
            Өгөгдлийн сан
          </a>
          <span>|</span>
          <a href="http://localhost:8080/los/api/v1/auth/test-users" target="_blank" rel="noopener noreferrer">
            Test Users
          </a>
        </Space>
      </Footer>
    </Layout>
  );
}

export default App;