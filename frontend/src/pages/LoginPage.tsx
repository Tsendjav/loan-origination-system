import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Checkbox, Divider, Form, Input, Segmented, Tag, message } from 'antd';
import {
  ApiOutlined,
  ArrowRightOutlined,
  BankOutlined,
  CloudServerOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { DEMO_ACCOUNTS, ROLE_LABELS, useSession } from '../contexts/SessionContext';
import { INSTITUTION_LABEL, useInstitution } from '../contexts/InstitutionContext';
import { apiRequest } from '../services/http';
import './LoginPage.css';

interface LoginValues {
  username: string;
  password: string;
  remember?: boolean;
}

type ServerState = 'checking' | 'online' | 'offline';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useSession();
  const { profile, switchInstitutionType } = useInstitution();
  const [form] = Form.useForm<LoginValues>();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [serverState, setServerState] = useState<ServerState>('checking');

  useEffect(() => {
    let active = true;
    apiRequest('/health', { timeoutMs: 2500 })
      .then(() => {
        if (active) setServerState('online');
      })
      .catch(() => {
        if (active) setServerState('offline');
      });
    return () => {
      active = false;
    };
  }, []);

  const doLogin = async (username: string, password: string): Promise<void> => {
    setSubmitting(true);
    setError(null);
    try {
      const session = await login(username, password);
      message.success(`Тавтай морил, ${session.name}`);
      navigate('/dashboard', { replace: true });
    } catch (loginError) {
      setError(loginError instanceof Error ? loginError.message : 'Нэвтрэх үед алдаа гарлаа');
    } finally {
      setSubmitting(false);
    }
  };

  const handleFinish = async (values: LoginValues): Promise<void> => {
    if (values.remember) {
      localStorage.setItem('los_remember_username', values.username.trim());
    } else {
      localStorage.removeItem('los_remember_username');
    }
    await doLogin(values.username, values.password);
  };

  const handleDemo = (username: string, password: string): void => {
    form.setFieldsValue({ username, password });
    void doLogin(username, password);
  };

  const serverBadge =
    serverState === 'online' ? (
      <Tag color="green" icon={<CloudServerOutlined />}>Сервер холбогдсон</Tag>
    ) : serverState === 'offline' ? (
      <Tag color="orange" icon={<ApiOutlined />}>Демо горим</Tag>
    ) : (
      <Tag icon={<CloudServerOutlined />}>Холболт шалгаж байна...</Tag>
    );

  return (
    <div className="login-page">
      <aside className="login-brand">
        <div className="login-brand__top">
          <span className="login-brand__logo">
            <BankOutlined />
          </span>
          <div>
            <strong>LOS · Зээлийн хүсэлтийн систем</strong>
            <small>Loan Origination System</small>
          </div>
        </div>

        <div className="login-brand__body">
          <p className="login-brand__eyebrow">Банк · Банк бус санхүүгийн байгууллага</p>
          <h1>Зээлийн шийдвэрийг нэг дороос удирдаарай</h1>
          <p className="login-brand__lead">
            Харилцагчийн бүртгэл, KYC, зээлийн хүсэлт, баримт бичиг, шийдвэр болон
            олголтын урсгалыг нэг дор нэгтгэсэн платформ.
          </p>

          <ul className="login-brand__features">
            <li>
              <SafetyCertificateOutlined />
              <span><strong>Эрхэд суурилсан хандалт</strong> — үүрэг тус бүрээр тусгаарлагдсан үйлдэл</span>
            </li>
            <li>
              <ThunderboltOutlined />
              <span><strong>Хурдан шийдвэр</strong> — зээлийн хороо, менежер, мэргэжилтний нэг урсгал</span>
            </li>
            <li>
              <LockOutlined />
              <span><strong>Аудит бүртгэл</strong> — үйлдэл бүр түүхэнд хадгалагдана</span>
            </li>
          </ul>
        </div>

        <div className="login-brand__footer">
          <span>Мэдээллийн нууцлал</span>
          <span>·</span>
          <span>Хандалтын хяналт</span>
          <span>·</span>
          <span>Аудитын бүртгэл</span>
        </div>
      </aside>

      <main className="login-panel">
        <div className="login-card">
          <div className="login-card__head">
            <div>
              <span className="login-card__eyebrow">Байгууллагын төрөл</span>
              <Segmented
                block
                value={profile.type}
                onChange={(value) => switchInstitutionType(value as 'BANK' | 'NBFI')}
                options={[
                  { label: 'Банк', value: 'BANK' },
                  { label: 'Банк бус СФБ', value: 'NBFI' },
                ]}
              />
              <p className="login-card__org">
                {profile.name} · {INSTITUTION_LABEL[profile.type]}
              </p>
            </div>
            {serverBadge}
          </div>

          <h2 className="login-card__title">Системд нэвтрэх</h2>
          <p className="login-card__sub">
            Ажлын үргэлжлэлд хэрэглэгчийн нэр, нууц үгээ оруулна уу.
          </p>

          {error && (
            <Alert
              type="error"
              showIcon
              message={error}
              closable
              onClose={() => setError(null)}
              className="login-card__alert"
            />
          )}

          <Form<LoginValues>
            form={form}
            layout="vertical"
            requiredMark={false}
            initialValues={{
              username: localStorage.getItem('los_remember_username') ?? '',
              password: '',
              remember: Boolean(localStorage.getItem('los_remember_username')),
            }}
            onFinish={handleFinish}
            className="login-form"
          >
            <Form.Item
              name="username"
              label="Хэрэглэгчийн нэр"
              rules={[
                { required: true, message: 'Хэрэглэгчийн нэрээ оруулна уу' },
                { min: 3, message: 'Хамгийн багадаа 3 тэмдэгт' },
              ]}
            >
              <Input
                size="large"
                placeholder="admin"
                autoComplete="username"
                disabled={submitting}
                prefix={<BankOutlined className="login-input-icon" />}
              />
            </Form.Item>

            <Form.Item
              name="password"
              label="Нууц үг"
              rules={[
                { required: true, message: 'Нууц үгээ оруулна уу' },
                { min: 6, message: 'Хамгийн багадаа 6 тэмдэгт' },
              ]}
            >
              <Input.Password
                size="large"
                placeholder="••••••••"
                autoComplete="current-password"
                disabled={submitting}
                prefix={<LockOutlined className="login-input-icon" />}
              />
            </Form.Item>

            <div className="login-form__row">
              <Form.Item name="remember" valuePropName="checked" noStyle>
                <Checkbox disabled={submitting}>Намайг сана</Checkbox>
              </Form.Item>
              <Button
                type="link"
                size="small"
                onClick={() =>
                  message.info('Нууц үг сэргээх хүсэлтийг байгууллагын админд хандана уу')
                }
              >
                Нууц үгээ мартсан уу?
              </Button>
            </div>

            <Button
              type="primary"
              htmlType="submit"
              size="large"
              block
              loading={submitting}
              className="login-submit"
            >
              {submitting ? 'Нэвтэрч байна...' : 'Нэвтрэх'}
            </Button>
          </Form>

          <Divider plain className="login-divider">Демо хэрэглэгчээр турших</Divider>

          <div className="login-demo-grid">
            {DEMO_ACCOUNTS.map((account) => (
              <button
                key={account.username}
                type="button"
                className="login-demo"
                disabled={submitting}
                onClick={() => handleDemo(account.username, account.password)}
              >
                <span className="login-demo__avatar">{account.name.charAt(0)}</span>
                <span className="login-demo__body">
                  <strong>{ROLE_LABELS[account.role]}</strong>
                  <small>{account.username} / {account.password}</small>
                </span>
                <ArrowRightOutlined className="login-demo__arrow" />
              </button>
            ))}
          </div>

          <p className="login-card__note">
            Демо горимд бүх өгөгдөл таны хөтөч дээр хадгалагдах бөгөөд сервер шаардлагагүй.
          </p>
        </div>

        <p className="login-copyright">
          © {new Date().getFullYear()} {profile.name} · Лиценз {profile.licenseNo}
        </p>
      </main>
    </div>
  );
};

export default LoginPage;
