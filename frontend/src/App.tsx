import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ConfigProvider, Card, Row, Col, Statistic, Alert, Layout, Menu, Typography, Space, Button, Spin, Collapse, Tag, Tabs } from 'antd';
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
  LogoutOutlined
} from '@ant-design/icons';
import { healthService, type HealthStatus, type ConnectionTestResult } from './services/healthService';
import { apiClient } from './services/apiClient';
import { DEV_URLS } from './services/apiConfig';
import { authService } from './services/authService';
import './App.css';

const { Header, Content, Footer } = Layout;
const { Title } = Typography;
const { Panel } = Collapse;
const { TabPane } = Tabs;

const antdTheme = {
  token: {
    colorPrimary: '#1890ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    borderRadius: 6,
  },
};

// LoginComponent-ийг энд тодорхойлсон, эсвэл тусдаа файл болгож болно.
// Хэрэв танд LoginComponent.tsx файл байгаа бол энэ кодыг хасаад,
// зөвхөн import хийх хэрэгтэй.
const LoginComponent = ({ onLoginSuccess }: { onLoginSuccess: (user: any) => void }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLogin = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await authService.login(username, password);
      if (response.success) {
        onLoginSuccess(response.user);
      } else {
        setError(response.message || 'Нэвтрэх нэр эсвэл нууц үг буруу байна.');
      }
    } catch (err) {
      setError('Нэвтрэх үед алдаа гарлаа. Сервертэй холбогдож чадсангүй.');
      console.error('Login error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card title="Нэвтрэх" style={{ maxWidth: 400, margin: '0 auto' }}>
      {error && (
        <Alert
          message="Нэвтрэх алдаа"
          description={error}
          type="error"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}
      <div style={{ marginBottom: 16 }}>
        <Typography.Text>Хэрэглэгчийн нэр:</Typography.Text>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
        />
      </div>
      <div style={{ marginBottom: 16 }}>
        <Typography.Text>Нууц үг:</Typography.Text>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
        />
      </div>
      <Button type="primary" onClick={handleLogin} loading={loading} block>
        Нэвтрэх
      </Button>
      <Typography.Paragraph style={{ marginTop: 16 }}>
        Тест хийх хэрэглэгчид:
        <ul>
          <li><strong>admin</strong> / admin123</li>
          <li><strong>loan_officer</strong> / loan123</li>
          <li><strong>manager</strong> / manager123</li>
        </ul>
      </Typography.Paragraph>
    </Card>
  );
};


function App() {
  const [backendStatus, setBackendStatus] = useState<HealthStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [connectionTest, setConnectionTest] = useState<ConnectionTestResult | null>(null);
  const [apiEndpoints, setApiEndpoints] = useState<Record<string, boolean>>({});
  const [diagnostics, setDiagnostics] = useState<any>(null);
  const [authState, setAuthState] = useState(authService.getAuthState());
  const [activeTabKey, setActiveTabKey] = useState('dashboard'); // Табын төлөвийг хадгалах

  const fetchSystemStatus = async () => {
    try {
      setLoading(true);
      setError(null);
      
      console.log('🔄 Fetching system status...');
      
      // First test basic connection
      const testResult = await healthService.testConnection();
      setConnectionTest(testResult);
      
      if (testResult.success) {
        // If basic connection works, get detailed health
        try {
          const healthData = await healthService.getHealthStatus();
          setBackendStatus(healthData);
          console.log('✅ System status fetched successfully');
        } catch (healthError) {
          console.warn('⚠️ Detailed health failed, but connection works');
          setBackendStatus({
            status: 'UP',
            service: 'LOS Backend',
            version: '1.0.0',
            timestamp: new Date().toISOString()
          });
        }
      } else {
        throw new Error(testResult.message);
      }
    } catch (err) {
      console.error('❌ System status fetch failed:', err);
      setError(err instanceof Error ? err.message : 'Системийн статус авахад алдаа гарлаа');
      setBackendStatus(null);
    } finally {
      setLoading(false);
    }
  };

  const testApiEndpoints = async () => {
    try {
      console.log('🧪 Testing API endpoints...');
      const endpoints = await healthService.checkApiEndpoints();
      setApiEndpoints(endpoints);
      console.log('📡 API endpoints status:', endpoints);
      return endpoints;
    } catch (error) {
      console.error('❌ API endpoint test failed:', error);
      return {};
    }
  };

  const runDiagnostics = async () => {
    try {
      console.log('🔍 Running system diagnostics...');
      const diag = await healthService.getSystemDiagnostics();
      setDiagnostics(diag);
      console.log('📊 Diagnostics complete:', diag);
    } catch (error) {
      console.error('❌ Diagnostics failed:', error);
    }
  };

  const testDirectUrls = async () => {
    const urls = [
      DEV_URLS.HEALTH_CHECK,
      `${DEV_URLS.BACKEND_BASE}/api/v1/health/simple`,
      `${DEV_URLS.BACKEND_BASE}/api/v1/`,
      DEV_URLS.BACKEND_BASE
    ];

    console.log('🎯 Testing direct URLs...');
    for (const url of urls) {
      try {
        const success = await apiClient.testDirectUrl(url);
        console.log(`${success ? '✅' : '❌'} ${url} - ${success ? 'OK' : 'Failed'}`);
      } catch (error) {
        console.log(`❌ ${url} - Error:`, error);
      }
    }
  };

  const handleLogout = async () => {
    try {
      await authService.logout();
      setAuthState(authService.getAuthState());
    } catch (error) {
      console.error('Logout error:', error);
    }
  };

  const handleLoginSuccess = (user: any) => {
    setAuthState(authService.getAuthState()); // Auth state-г шинэчлэх
    setActiveTabKey('dashboard'); // Амжилттай нэвтэрсний дараа хяналтын самбар руу шилжих
  };

  useEffect(() => {
    // Initialize auth service
    authService.initialize();
    
    // Subscribe to auth state changes
    const unsubscribeAuth = authService.subscribe((state) => {
      setAuthState(state);
    });

    // Initial status check
    fetchSystemStatus();
    
    // Test API endpoints
    testApiEndpoints();
    
    // Run diagnostics
    runDiagnostics();
    
    // Set up periodic health monitoring
    healthService.startHealthMonitoring(30000); // Every 30 seconds
    
    // Subscribe to health status changes
    const unsubscribeHealth = healthService.subscribe((status) => {
      setBackendStatus(status);
    });
    
    // Cleanup on unmount
    return () => {
      healthService.stopHealthMonitoring();
      unsubscribeHealth();
      unsubscribeAuth();
    };
  }, []);

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

  const getStatusColor = (status?: string) => {
    if (!status) return 'default';
    return status === 'UP' ? 'success' : 'error';
  };

  const renderApiEndpointStatus = () => {
    // "health-simple" endpoint-ийг харуулахгүйгээр шүүж байна
    const filteredEntries = Object.entries(apiEndpoints).filter(([name]) => name !== 'health-simple');
    if (filteredEntries.length === 0) return null;

    return (
      <div style={{ marginTop: 16 }}>
        <Typography.Text strong>API Endpoints:</Typography.Text>
        <div style={{ marginTop: 8 }}>
          {filteredEntries.map(([name, status]) => (
            <Tag key={name} color={status ? 'green' : 'red'} style={{ marginBottom: 4 }}>
              {name}: {status ? 'OK' : 'Failed'}
            </Tag>
          ))}
        </div>
      </div>
    );
  };

  return (
    <ConfigProvider theme={antdTheme}>
      <Router>
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
                selectedKeys={[activeTabKey]} // Сонгогдсон табыг удирдана
                items={menuItems}
                style={{ flex: 1, minWidth: 0 }}
                onSelect={({ key }) => setActiveTabKey(key)} // Таб солигдоход төлөвийг шинэчлэх
              />
            </div>
            
            {/* Auth section */}
            <div style={{ display: 'flex', alignItems: 'center' }}>
              {authState.isAuthenticated ? (
                <Space>
                  <Typography.Text style={{ color: 'white' }}>
                    {authState.user?.name || authState.user?.username}
                  </Typography.Text>
                  <Button 
                    type="text" 
                    icon={<LogoutOutlined />}
                    onClick={handleLogout}
                    style={{ color: 'white' }}
                  >
                    Гарах
                  </Button>
                </Space>
              ) : (
                <Button 
                  type="text" 
                  icon={<LoginOutlined />}
                  style={{ color: 'white' }}
                  onClick={() => setActiveTabKey('auth')} // Нэвтрэх товчийг дархад "Authentication тест" таб руу шилжих
                >
                  Нэвтрэх
                </Button>
              )}
            </div>
          </Header>
          
          <Content style={{ padding: '24px', background: '#f0f2f5' }}>
            <div style={{ maxWidth: 1200, margin: '0 auto' }}>
              
              <Tabs activeKey={activeTabKey} onChange={setActiveTabKey} type="card"> {/* activeKey болон onChange нэмсэн */}
                <TabPane tab={<span><DashboardOutlined />Хяналтын самбар</span>} key="dashboard">
                  {/* Control Panel */}
                  <div style={{ marginBottom: 24 }}>
                    <Space wrap>
                      <Button 
                        icon={<ReloadOutlined />} 
                        onClick={fetchSystemStatus}
                        loading={loading}
                      >
                        Статус шинэчлэх
                      </Button>
                      <Button 
                        icon={<ApiOutlined />}
                        onClick={testApiEndpoints}
                      >
                        API тестлэх
                      </Button>
                      <Button 
                        icon={<BugOutlined />}
                        onClick={runDiagnostics}
                      >
                        Диагностик
                      </Button>
                      <Button 
                        onClick={testDirectUrls}
                      >
                        Direct URL тест
                      </Button>
                    </Space>
                  </div>

                  {/* Connection Status */}
                  {loading && (
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
                              <Typography.Text type="secondary">
                                Дэлгэрэнгүй: {connectionTest.message}
                                {connectionTest.endpoint && ` (Endpoint: ${connectionTest.endpoint})`}
                              </Typography.Text>
                            </div>
                          )}
                        </div>
                      }
                      type="error"
                      showIcon
                      style={{ marginBottom: 24 }}
                      action={
                        <Space direction="vertical">
                          <Button size="small" onClick={fetchSystemStatus}>
                            Дахин оролдох
                          </Button>
                        </Space>
                      }
                    />
                  )}

                  {/* Connection Test Result */}
                  {connectionTest && (
                    <Alert
                      message={connectionTest.success ? "Холболт амжилттай" : "Холболтын алдаа"}
                      description={
                        <div>
                          <p>{connectionTest.message}</p>
                          {connectionTest.responseTime && (
                            <Typography.Text type="secondary">
                              Response time: {connectionTest.responseTime}ms
                            </Typography.Text>
                          )}
                          {connectionTest.endpoint && (
                            <div>
                              <Typography.Text type="secondary">
                                Working endpoint: {connectionTest.endpoint}
                              </Typography.Text>
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
                      description={`${backendStatus.service} v${backendStatus.version} ажиллаж байна`}
                      type={getStatusColor(backendStatus.status)}
                      showIcon
                      style={{ marginBottom: 24 }}
                    />
                  )}

                  {/* Diagnostics Panel */}
                  {diagnostics && (
                    <Collapse style={{ marginBottom: 24 }}>
                      <Panel header="🔍 Системийн диагностик" key="diagnostics">
                        <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 4, fontSize: 12 }}>
                          {JSON.stringify(diagnostics, null, 2)}
                        </pre>
                      </Panel>
                    </Collapse>
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
                  <Card title="Зээлийн хүсэлтийн системд тавтай морил! 🎉">
                    <div style={{ textAlign: 'left' }}>
                      <h3>✅ Төслийн тохиргоо амжилттай дууслаа!</h3>
                      
                      <h4>🚀 Системийн төлөв:</h4>
                      <ul>
                        <li>Backend API: {connectionTest?.success ? '✅ Ажиллаж байна' : '❌ Холбогдохгүй байна'}</li>
                        <li>Frontend: ✅ Ажиллаж байна (порт 3001)</li>
                        <li>Database: {backendStatus?.components?.database?.status === 'UP' ? '✅ Холбогдсон' : '⚠️ Тодорхойгүй'} (H2 in-memory)</li>
                        <li>API endpoints: {Object.values(apiEndpoints).filter(Boolean).length}/{Object.keys(apiEndpoints).length} ✅</li>
                      </ul>
                      
                      <h4>🔗 Хандах холбоосууд:</h4>
                      <ul>
                        <li><a href={DEV_URLS.HEALTH_CHECK} target="_blank" rel="noopener noreferrer">Backend Health Check</a></li>
                        <li><a href={DEV_URLS.SWAGGER_UI} target="_blank" rel="noopener noreferrer">API Documentation</a></li>
                        <li><a href={DEV_URLS.H2_CONSOLE} target="_blank" rel="noopener noreferrer">H2 Database Console</a></li>
                      </ul>

                      {authState.isAuthenticated && (
                        <Alert
                          message={`Сайн байна уу, ${authState.user?.name || authState.user?.username}!`}
                          description="Та амжилттай нэвтэрсэн байна. Одоо системийн бүх функцийг ашиглах боломжтой."
                          type="info"
                          showIcon
                          style={{ marginTop: 16 }}
                        />
                      )}
                    </div>
                  </Card>
                </TabPane>

                <TabPane tab={<span><LoginOutlined />Authentication тест</span>} key="auth">
                  <LoginComponent onLoginSuccess={handleLoginSuccess} />
                </TabPane>

                <TabPane tab={<span><UserOutlined />Харилцагчид</span>} key="customers">
                  <Card title="Харилцагчийн удирдлага">
                    <p>Харилцагчийн удирдлагын хэсэг удахгүй нэмэгдэнэ...</p>
                    {!authState.isAuthenticated && (
                      <Alert
                        message="Анхай нэвтэрнэ үү"
                        description="Энэ хэсгийг ашиглахын тулд эхлээд системд нэвтэрнэ үү."
                        type="warning"
                        showIcon
                      />
                    )}
                  </Card>
                </TabPane>

                <TabPane tab={<span><FileTextOutlined />Зээлийн хүсэлт</span>} key="applications">
                  <Card title="Зээлийн хүсэлт">
                    <p>Зээлийн хүсэлтийн хэсэг удахгүй нэмэгдэнэ...</p>
                    {!authState.isAuthenticated && (
                      <Alert
                        message="Анхай нэвтэрнэ үү"
                        description="Энэ хэсгийг ашиглахын тулд эхлээд системд нэвтэрнэ үү."
                        type="warning"
                        showIcon
                      />
                    )}
                  </Card>
                </TabPane>
              </Tabs>
            </div>
          </Content>
          
          <Footer style={{ textAlign: 'center', background: '#f0f2f5' }}>
            <Space>
              Зээлийн хүсэлтийн систем 2024
              <span>|</span>
              <a href={DEV_URLS.SWAGGER_UI} target="_blank" rel="noopener noreferrer">
                API баримт бичиг
              </a>
              <span>|</span>
              <a href={DEV_URLS.H2_CONSOLE} target="_blank" rel="noopener noreferrer">
                Өгөгдлийн сан
              </a>
            </Space>
          </Footer>
        </Layout>
      </Router>
    </ConfigProvider>
  );
}

export default App;