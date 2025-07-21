import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, Card, Row, Col, Statistic, Alert, Layout, Menu, Typography, Space, Button } from 'antd';
import { 
  CheckCircleOutlined, 
  ClockCircleOutlined, 
  FileTextOutlined, 
  UserOutlined,
  DashboardOutlined,
  ReloadOutlined 
} from '@ant-design/icons';
import './App.css';

const { Header, Content, Footer } = Layout;
const { Title } = Typography;

const antdTheme = {
  token: {
    colorPrimary: '#1890ff',
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
    borderRadius: 6,
  },
};

interface BackendStatus {
  status: string;
  service: string;
  version: string;
  timestamp: string;
}

function App() {
  const [backendStatus, setBackendStatus] = useState<BackendStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSystemStatus = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await fetch('/los/api/v1/health');
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      setBackendStatus(data);
    } catch (err) {
      console.error('Backend хандалт амжилтгүй:', err);
      setError(err instanceof Error ? err.message : 'Алдаа гарлаа');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSystemStatus();
    const interval = setInterval(fetchSystemStatus, 30000); // 30 секунд тутам шалгах
    return () => clearInterval(interval);
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

  return (
    <ConfigProvider theme={antdTheme}>
      <Router>
        <Layout style={{ minHeight: '100vh' }}>
          <Header style={{ 
            display: 'flex', 
            alignItems: 'center', 
            background: '#001529' 
          }}>
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
              defaultSelectedKeys={['dashboard']}
              items={menuItems}
              style={{ flex: 1, minWidth: 0 }}
            />
          </Header>
          
          <Content style={{ padding: '24px', background: '#f0f2f5' }}>
            <div style={{ maxWidth: 1200, margin: '0 auto' }}>
              {/* System Status */}
              <div style={{ marginBottom: 24 }}>
                <Space>
                  <Button 
                    icon={<ReloadOutlined />} 
                    onClick={fetchSystemStatus}
                    loading={loading}
                  >
                    Статус шинэчлэх
                  </Button>
                </Space>
              </div>

              {error && (
                <Alert
                  message="Системийн холболтын алдаа"
                  description={`Backend-тэй холбогдож чадсангүй: ${error}`}
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

              {backendStatus && (
                <Alert
                  message={`Системийн статус: ${backendStatus.status}`}
                  description={`${backendStatus.service} v${backendStatus.version} ажиллаж байна`}
                  type="success"
                  showIcon
                  style={{ marginBottom: 24 }}
                />
              )}

              {/* Dashboard Cards */}
              <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
                <Col xs={24} sm={12} md={6}>
                  <Card>
                    <Statistic
                      title="Нийт харилцагчид"
                      value={0}
                      prefix={<UserOutlined />}
                      valueStyle={{ color: '#3f8600' }}
                    />
                  </Card>
                </Col>
                
                <Col xs={24} sm={12} md={6}>
                  <Card>
                    <Statistic
                      title="Зээлийн хүсэлт"
                      value={0}
                      prefix={<FileTextOutlined />}
                      valueStyle={{ color: '#1890ff' }}
                    />
                  </Card>
                </Col>
                
                <Col xs={24} sm={12} md={6}>
                  <Card>
                    <Statistic
                      title="Зөвшөөрөгдсөн"
                      value={0}
                      prefix={<CheckCircleOutlined />}
                      valueStyle={{ color: '#52c41a' }}
                    />
                  </Card>
                </Col>
                
                <Col xs={24} sm={12} md={6}>
                  <Card>
                    <Statistic
                      title="Хүлээгдэж буй"
                      value={0}
                      prefix={<ClockCircleOutlined />}
                      valueStyle={{ color: '#faad14' }}
                    />
                  </Card>
                </Col>
              </Row>

              {/* Main Content */}
              <Routes>
                <Route path="/" element={
                  <Card title="Зээлийн хүсэлтийн системд тавтай морил! 🎉">
                    <div style={{ textAlign: 'left' }}>
                      <h3>✅ Төслийн тохиргоо амжилттай дууслаа!</h3>
                      
                      <h4>🚀 Дараагийн алхамууд:</h4>
                      <ul>
                        <li>Backend API ажиллаж байна (порт 8080)</li>
                        <li>Frontend ажиллаж байна (порт 3001)</li>
                        <li>Database бэлэн (H2 in-memory)</li>
                        <li>Үндсэн компонентууд үүсгэгдсэн</li>
                      </ul>
                      
                      <h4>🔗 Хандах холбоосууд:</h4>
                      <ul>
                        <li><a href="/los/api/v1/health" target="_blank">Backend Health Check</a></li>
                        <li><a href="/los/swagger-ui.html" target="_blank">API Documentation</a></li>
                        <li><a href="/los/h2-console" target="_blank">H2 Database Console</a></li>
                      </ul>

                      <h4>🛠️ Хөгжүүлэлтэд бэлэн:</h4>
                      <ul>
                        <li>Харилцагчийн удирдлага</li>
                        <li>Зээлийн хүсэлтийн системd</li>
                        <li>Баримт бичгийн удирдлага</li>
                        <li>Зээлийн үнэлгээний систем</li>
                      </ul>
                    </div>
                  </Card>
                } />
                <Route path="/customers" element={
                  <Card title="Харилцагчийн удирдлага">
                    <p>Харилцагчийн удирдлагын хэсэг удахгүй нэмэгдэнэ...</p>
                  </Card>
                } />
                <Route path="/applications" element={
                  <Card title="Зээлийн хүсэлт">
                    <p>Зээлийн хүсэлтийн хэсэг удахгүй нэмэгдэнэ...</p>
                  </Card>
                } />
              </Routes>
            </div>
          </Content>
          
          <Footer style={{ textAlign: 'center', background: '#f0f2f5' }}>
            <Space>
              Зээлийн хүсэлтийн систем 2024
              <span>|</span>
              <a href="/los/swagger-ui.html" target="_blank" rel="noopener noreferrer">
                API баримт бичиг
              </a>
              <span>|</span>
              <a href="/los/h2-console" target="_blank" rel="noopener noreferrer">
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
