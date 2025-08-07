// frontend/src/pages/LoginPage.tsx - ЗАСВАРЛАСАН
import React, { useState, useEffect } from 'react';
import authService, { TEST_USERS } from '../services/authService';
import './LoginPage.css';

interface LoginFormData {
  username: string;
  password: string;
}

interface ConnectionStatus {
  success: boolean;
  message: string;
  endpoint?: string;
}

const LoginPage: React.FC = () => {
  const [formData, setFormData] = useState<LoginFormData>({
    username: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus | null>(null);

  // Backend холболт шалгах
  useEffect(() => {
    const checkConnection = async () => {
      console.log('🔍 Checking backend connection...');
      const isConnected = await authService.testConnection();
      console.log('🔗 Connection status:', isConnected);
      setConnectionStatus(isConnected);
    };

    checkConnection();
  }, []);

  // Form input өөрчлөх
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    // Алдаа цэвэрлэх
    if (error) {
      setError(null);
    }
  };

  // Нэвтрэх үйлдэл
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.username || !formData.password) {
      setError('Хэрэглэгчийн нэр болон нууц үгээ оруулна уу');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      console.log('🚀 Starting login process...');
      const response = await authService.login(formData);

      if (response.success) {
        console.log('✅ Login successful, redirecting...');
        // Амжилттай нэвтэрсэн - dashboard луу шилжих
        window.location.href = '/dashboard';
      } else {
        setError(response.message || 'Нэвтрэх амжилтгүй');
      }
    } catch (error: any) {
      console.error('❌ Login error:', error);
      setError(error.message || 'Серверт холбогдох алдаа гарлаа');
    } finally {
      setLoading(false);
    }
  };

  // Тест хэрэглэгчээр нэвтрэх
  const handleTestLogin = async (testUser: typeof TEST_USERS[0]) => {
    console.log('🧪 Test login with:', testUser.username);

    setFormData({
      username: testUser.username,
      password: testUser.password
    });

    setLoading(true);
    setError(null);

    try {
      const response = await authService.login({
        username: testUser.username,
        password: testUser.password
      });

      if (response.success) {
        console.log('✅ Test login successful');
        window.location.href = '/dashboard';
      } else {
        setError(response.message || 'Тест нэвтрэлт амжилтгүй');
      }
    } catch (error: any) {
      console.error('❌ Test login failed:', error);
      setError(error.message || 'Тест нэвтрэлтэд алдаа гарлаа');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-header">
          <h1>🏦 LOS Систем</h1>
          <h2>Нэвтрэх</h2>
        </div>

        {/* Backend холболтын статус */}
        <div className={`connection-status ${connectionStatus?.success ? 'connected' : 'disconnected'}`}>
          {connectionStatus === null ? (
            <span>🔄 Холболт шалгаж байна...</span>
          ) : connectionStatus.success ? (
            <span>✅ Backend холбогдсон</span>
          ) : (
            <span>❌ Backend холбогдоогүй (http://localhost:8080)</span>
          )}
        </div>

        {/* Login форм */}
        <form onSubmit={handleSubmit} className="login-form">
          {error && (
            <div className="error-message">
              <span className="error-icon">❌</span>
              {error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="username">Хэрэглэгчийн нэр:</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              placeholder="admin"
              disabled={loading}
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Нууц үг:</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              placeholder="••••"
              disabled={loading}
              autoComplete="current-password"
            />
          </div>

          <button 
            type="submit" 
            className="login-button"
            disabled={loading || !connectionStatus?.success}
          >
            {loading ? '🔄 Нэвтрэж байна...' : '🔐 Нэвтрэх'}
          </button>
        </form>

        {/* Тест хэрэглэгчдийн жагсаалт */}
        <div className="test-users">
          <h3>Тест хэрэглэгчид:</h3>
          <div className="test-user-buttons">
            {TEST_USERS.map((user: any) => (
              <button
                key={user.username}
                onClick={() => handleTestLogin(user)}
                className="test-user-button"
                disabled={loading || !connectionStatus?.success}
                title={`${user.username} / ${user.password}`}
              >
                👤 {user.username} ({user.role})
              </button>
            ))}
          </div>
          <p className="test-info">
            💡 Дээрх товчнуудыг дарж шууд нэвтрэх боломжтой
          </p>
        </div>

        {/* Системийн мэдээлэл */}
        <div className="system-info">
          <h4>📋 Системийн мэдээлэл:</h4>
          <ul>
            <li>🌐 Backend: http://localhost:8080/los</li>
            <li>🎨 Frontend: http://localhost:3001</li>
            <li>📊 API Docs: http://localhost:8080/los/swagger-ui.html</li>
            <li>🗄️ H2 Database: http://localhost:8080/los/h2-console</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;