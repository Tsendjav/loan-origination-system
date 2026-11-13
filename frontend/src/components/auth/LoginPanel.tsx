import React, { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Button,
  Checkbox,
  Col,
  Divider,
  Input,
  Row,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import {
  ArrowRightOutlined,
  BankOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  UserOutlined,
} from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
}

interface AuthStateLike {
  isAuthenticated: boolean;
  user: { username?: string; fullName?: string; name?: string; role?: string } | null;
  loading: boolean;
  error: string | null;
}

interface LoginPanelProps {
  login: (data: LoginRequest) => Promise<void>;
  authState: AuthStateLike;
  onLoginSuccess: (user: any) => void;
}

const DEMO_USERS = [
  { username: 'admin', password: 'admin123', label: 'Системийн админ', role: 'SUPER_ADMIN' },
  { username: 'manager', password: 'manager123', label: 'Салбарын менежер', role: 'MANAGER' },
  { username: 'loan_officer', password: 'loan123', label: 'Зээлийн мэргэжилтэн', role: 'LOAN_OFFICER' },
];

const FONT_STACK =
  "'Inter', 'Segoe UI', system-ui, -apple-system, 'Helvetica Neue', Arial, sans-serif";

/** Хар цагаан (monochrome) нэвтрэлтийн дэлгэц. */
const LoginPanel: React.FC<LoginPanelProps> = ({ login, authState, onLoginSuccess }) => {
  const [username, setUsername] = useState<string>(() => localStorage.getItem('los_remember_username') || '');
  const [password, setPassword] = useState<string>('');
  const [remember, setRemember] = useState<boolean>(() => !!localStorage.getItem('los_remember_username'));
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (authState.isAuthenticated && authState.user) {
      onLoginSuccess(authState.user);
    }
  }, [authState.isAuthenticated, authState.user, onLoginSuccess]);

  const submit = useCallback(async () => {
    setError(null);
    const u = username.trim();
    const p = password.trim();

    if (u.length < 3) {
      setError('Хэрэглэгчийн нэр хамгийн багадаа 3 тэмдэгт байх ёстой');
      return;
    }
    if (p.length < 6) {
      setError('Нууц үг хамгийн багадаа 6 тэмдэгт байх ёстой');
      return;
    }

    setSubmitting(true);
    try {
      await login({ username: u, password: p, rememberMe: remember });
      if (remember) localStorage.setItem('los_remember_username', u);
      else localStorage.removeItem('los_remember_username');

      setPassword('');
      message.success('Амжилттай нэвтэрлээ');
    } catch (e) {
      const msg = e instanceof Error ? e.message : 'Нэвтрэх үед алдаа гарлаа';
      setError(
        msg.includes('401')
          ? 'Хэрэглэгчийн нэр эсвэл нууц үг буруу байна'
          : msg.includes('403')
            ? 'Хандах эрхгүй байна. Системийн админд хандана уу'
            : msg,
      );
    } finally {
      setSubmitting(false);
    }
  }, [username, password, remember, login]);

  const quickLogin = useCallback(
    async (u: string, p: string) => {
      setUsername(u);
      setPassword(p);
      setError(null);
      setSubmitting(true);
      try {
        await login({ username: u, password: p, rememberMe: false });
        message.success('Амжилттай нэвтэрлээ');
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Нэвтрэх үед алдаа гарлаа');
      } finally {
        setSubmitting(false);
      }
    },
    [login],
  );

  const busy = submitting || authState.loading;

  return (
    <div style={{ maxWidth: 1040, margin: '0 auto' }}>
      <Row
        style={{
          background: '#fff',
          border: '1px solid #111',
          borderRadius: 12,
          overflow: 'hidden',
          fontFamily: FONT_STACK,
        }}
      >
        {/* Зүүн тал — брэнд */}
        <Col xs={24} md={10} style={{ background: '#0a0a0a', color: '#fff', padding: '40px 36px' }}>
          <Space direction="vertical" size={20} style={{ width: '100%' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <BankOutlined style={{ fontSize: 26 }} />
              <span style={{ fontSize: 22, letterSpacing: 1, fontWeight: 600 }}>LOS</span>
            </div>

            <div>
              <Title level={3} style={{ color: '#fff', marginBottom: 8 }}>
                Зээлийн шийдвэрийн систем
              </Title>
              <Paragraph style={{ color: '#bfbfbf', marginBottom: 0 }}>
                Банк, банк бус санхүүгийн байгууллага болон зээлийн үйлчилгээ үзүүлэгчдэд
                зориулсан хүсэлт хүлээн авах, автоматаар шийдвэрлэх платформ.
              </Paragraph>
            </div>

            <Divider style={{ borderColor: '#303030', margin: '8px 0' }} />

            <Space direction="vertical" size={14} style={{ width: '100%' }}>
              <Feature icon={<ThunderboltOutlined />} title="Автомат шийдвэр" text="Дүн, хугацаа, кредит оноогоор шууд шийднэ" />
              <Feature icon={<SafetyCertificateOutlined />} title="Эрхийн удирдлага" text="Role/permission түвшинд хяналт" />
              <Feature icon={<UserOutlined />} title="Харилцагчийн бүртгэл" text="KYC болон баримтын урсгал" />
            </Space>

            <Text style={{ color: '#8c8c8c', fontSize: 12 }}>
              v1.0 · Зөвшөөрөлгүй нэвтрэх оролдлогыг бүртгэнэ
            </Text>
          </Space>
        </Col>

        {/* Баруун тал — форм */}
        <Col xs={24} md={14} style={{ padding: '40px 36px' }}>
          <Title level={4} style={{ marginBottom: 4 }}>
            Системд нэвтрэх
          </Title>
          <Text type="secondary">Хэрэглэгчийн нэр, нууц үгээ оруулна уу</Text>

          {(error || authState.error) && (
            <Alert
              type="error"
              showIcon
              closable
              onClose={() => setError(null)}
              message={error || authState.error}
              style={{ marginTop: 16, borderRadius: 8 }}
            />
          )}

          <Space direction="vertical" size={14} style={{ width: '100%', marginTop: 20 }}>
            <div>
              <Text strong>Хэрэглэгчийн нэр</Text>
              <Input
                size="large"
                autoFocus
                prefix={<UserOutlined style={{ color: '#8c8c8c' }} />}
                placeholder="ж: admin"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                onPressEnter={() => void submit()}
                disabled={busy}
                style={{ marginTop: 6 }}
              />
            </div>

            <div>
              <Text strong>Нууц үг</Text>
              <Input.Password
                size="large"
                prefix={<LockOutlined style={{ color: '#8c8c8c' }} />}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onPressEnter={() => void submit()}
                disabled={busy}
                style={{ marginTop: 6 }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Checkbox checked={remember} onChange={(e) => setRemember(e.target.checked)} disabled={busy}>
                Нэрийг санах
              </Checkbox>
              <Text type="secondary" style={{ fontSize: 12 }}>
                Нууц үгээ мартсан бол админд хандана уу
              </Text>
            </div>

            <Button
              type="primary"
              size="large"
              block
              loading={busy}
              onClick={() => void submit()}
              style={{ height: 46, fontWeight: 600 }}
              icon={!busy ? <ArrowRightOutlined /> : undefined}
              iconPosition="end"
            >
              {busy ? 'Нэвтэрч байна…' : 'Нэвтрэх'}
            </Button>
          </Space>

          <Divider style={{ margin: '24px 0 16px' }}>
            <Text type="secondary" style={{ fontSize: 12 }}>Демо хэрэглэгчид</Text>
          </Divider>

          <Space direction="vertical" size={8} style={{ width: '100%' }}>
            {DEMO_USERS.map((u) => (
              <button
                key={u.username}
                type="button"
                disabled={busy}
                onClick={() => void quickLogin(u.username, u.password)}
                style={{
                  width: '100%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: 12,
                  padding: '10px 12px',
                  background: '#fff',
                  border: '1px solid #d9d9d9',
                  borderRadius: 8,
                  cursor: busy ? 'not-allowed' : 'pointer',
                  fontFamily: FONT_STACK,
                  textAlign: 'left',
                }}
              >
                <span>
                  <span style={{ fontWeight: 600 }}>{u.username}</span>
                  <span style={{ color: '#8c8c8c' }}> / {u.password}</span>
                  <br />
                  <span style={{ fontSize: 12, color: '#595959' }}>{u.label}</span>
                </span>
                <Tag style={{ marginInlineEnd: 0, borderColor: '#111', color: '#111' }}>{u.role}</Tag>
              </button>
            ))}
          </Space>
        </Col>
      </Row>
    </div>
  );
};

const Feature: React.FC<{ icon: React.ReactNode; title: string; text: string }> = ({ icon, title, text }) => (
  <div style={{ display: 'flex', gap: 12 }}>
    <span style={{ fontSize: 18, marginTop: 2, color: '#fff' }}>{icon}</span>
    <div>
      <div style={{ fontWeight: 600, color: '#fff' }}>{title}</div>
      <div style={{ fontSize: 12, color: '#8c8c8c' }}>{text}</div>
    </div>
  </div>
);

export default LoginPanel;
