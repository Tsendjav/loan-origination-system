// frontend/src/pages/LoginPage.tsx
import React, { useState, useEffect } from 'react';
import authService, { TEST_USERS } from '../services/authService';
import './LoginPage.css';

interface LoginFormData {
  username: string;
  password: string;
}

const LoginPage: React.FC = () => {
  const [formData, setFormData] = useState<LoginFormData>({
    username: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [connectionStatus, setConnectionStatus] = useState<boolean | null>(null);

  // Backend холболт шалгах
  useEffect(() => {
    const checkConnection = async () => {
      const isConnected = await authService.testConnection();
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
      const response = await authService.login(formData);
      
      if (response.success) {
        // Амжилттай нэвтэрсэн - dashboard луу шилжих
        window.location.href = '/dashboard';
      } else {
        setError(response.message || 'Нэвтрэх амжилтгүй');
      }
    } catch (error: any) {
      console.error('Нэвтрэх алдаа:', error);
      setError(error.message || 'Серверт холбогдох алдаа гарлаа');
    } finally {
      setLoading(false);
    }
  };

  // Тест хэрэглэгчээр нэвтрэх
  const handleTestLogin = async (testUser: typeof TEST_USERS[0]) => {
    setFormData({
      username: testUser.username,
      password: testUser.password
    });
    
    // Автомат нэвтрэх
    setTimeout(() => {
      handleSubmit(new Event('submit') as any);
    }, 100);
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <div className="login-header">
          <h1>🏦 LOS Систем</h1>
          <h2>Нэвтрэх</h2>
        </div>

        {/* Backend холболтын статус */}
        <div className={`connection-status ${connectionStatus ? 'connected' : 'disconnected'}`}>
          {connectionStatus === null ? (
            <span>🔄 Холболт шалгаж байна...</span>
          ) : connectionStatus ? (
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
              placeholder="••••••"
              disabled={loading}
              autoComplete="current-password"
            />
          </div>

          <button 
            type="submit" 
            className="login-button"
            disabled={loading || !connectionStatus}
          >
            {loading ? '🔄 Нэвтрэж байна...' : '🔐 Нэвтрэх'}
          </button>
        </form>

        {/* Тест хэрэглэгчдийн жагсаалт */}
        <div className="test-users">
          <h3>Тест хэрэглэгчид:</h3>
          <div className="test-user-buttons">
            {TEST_USERS.map((user) => (
              <button
                key={user.username}
                onClick={() => handleTestLogin(user)}
                className="test-user-button"
                disabled={loading || !connectionStatus}
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