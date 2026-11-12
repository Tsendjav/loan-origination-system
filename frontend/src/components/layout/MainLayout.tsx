import React, { useMemo, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  Avatar,
  Badge,
  Breadcrumb,
  Button,
  Drawer,
  Dropdown,
  Grid,
  Layout,
  List,
  Menu,
  Popover,
  Space,
  Tag,
  Tooltip,
  Typography,
  message,
} from 'antd';
import type { MenuProps } from 'antd';
import {
  AuditOutlined,
  BankOutlined,
  BarChartOutlined,
  BellOutlined,
  DashboardOutlined,
  DownOutlined,
  FileTextOutlined,
  FolderOpenOutlined,
  LogoutOutlined,
  MenuOutlined,
  SettingOutlined,
  SwapOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { INSTITUTION_SHORT_LABEL, useInstitution } from '../../contexts/InstitutionContext';
import { ROLE_LABELS, useSession } from '../../contexts/SessionContext';
import { useLosStore } from '../../services/demoStore';
import { initialsOf } from '../../utils/format';

const { Header, Sider, Content, Footer } = Layout;

interface RouteMeta {
  title: string;
  description: string;
}

const PAGE_META: Record<string, RouteMeta> = {
  '/dashboard': { title: 'Хяналтын самбар', description: 'Өдөр тутмын ажлын тойм' },
  '/customers': { title: 'Харилцагчид', description: 'Харилцагчийн бүртгэл ба KYC' },
  '/loans': { title: 'Зээлийн хүсэлтүүд', description: 'Хүсэлт бүртгэх, шийдвэрлэх, олгох' },
  '/documents': { title: 'Баримт бичиг', description: 'Баримтын бүрэн бүтэн байдал' },
  '/reports': { title: 'Тайлан', description: 'Портфелийн үзүүлэлтүүд' },
  '/audit': { title: 'Аудит лог', description: 'Системийн үйл ажиллагааны түүх' },
  '/settings': { title: 'Тохиргоо', description: 'Байгууллага ба эрхийн тохиргоо' },
};

interface NavEntry {
  key: string;
  label: string;
  icon: React.ReactNode;
  permission?: string;
}

const NAV_ENTRIES: NavEntry[] = [
  { key: '/dashboard', label: 'Хяналтын самбар', icon: <DashboardOutlined /> },
  { key: '/customers', label: 'Харилцагчид', icon: <TeamOutlined />, permission: 'customer:view' },
  { key: '/loans', label: 'Зээлийн хүсэлтүүд', icon: <FileTextOutlined />, permission: 'loan:view' },
  { key: '/documents', label: 'Баримт бичиг', icon: <FolderOpenOutlined />, permission: 'document:view' },
  { key: '/reports', label: 'Тайлан', icon: <BarChartOutlined />, permission: 'report:view' },
  { key: '/audit', label: 'Аудит лог', icon: <AuditOutlined />, permission: 'audit:view' },
  { key: '/settings', label: 'Тохиргоо', icon: <SettingOutlined />, permission: 'settings:manage' },
];

const TOAST_BY_TYPE = {
  success: message.success,
  error: message.error,
  warning: message.warning,
  info: message.info,
};

export const showToast = ({
  message: text,
  type,
}: {
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
}): void => {
  TOAST_BY_TYPE[type](text);
};

export const showGlobalLoading = (): void => {
  message.loading({ content: 'Боловсруулж байна...', key: 'global-loading' });
};

export const hideGlobalLoading = (): void => {
  message.destroy('global-loading');
};

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.lg;

  const { user, logout, hasPermission } = useSession();
  const { profile } = useInstitution();
  const { activities } = useLosStore();

  const [collapsed, setCollapsed] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const meta = PAGE_META[location.pathname] ?? { title: 'LOS', description: '' };

  const navEntries = useMemo(
    () => NAV_ENTRIES.filter((entry) => !entry.permission || hasPermission(entry.permission)),
    [hasPermission],
  );

  const menuItems: MenuProps['items'] = navEntries.map((entry) => ({
    key: entry.key,
    icon: entry.icon,
    label: entry.label,
  }));

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key);
    setDrawerOpen(false);
  };

  const handleLogout = async () => {
    await logout();
    message.success('Системээс амжилттай гарлаа');
    navigate('/login', { replace: true });
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Байгууллагын тохиргоо',
      disabled: !hasPermission('settings:manage'),
    },
    { type: 'divider' },
    { key: 'logout', icon: <LogoutOutlined />, label: 'Системээс гарах', danger: true },
  ];

  const brand = (
    <div className={`app-brand ${collapsed && !isMobile ? 'app-brand--collapsed' : ''}`}>
      <span className="app-brand__mark">
        <BankOutlined />
      </span>
      {(!collapsed || isMobile) && (
        <span className="app-brand__text">
          <strong>{profile.shortName} · LOS</strong>
          <small>{INSTITUTION_SHORT_LABEL[profile.type]} зээлийн систем</small>
        </span>
      )}
    </div>
  );

  const navigation = (
    <>
      {brand}
      <Menu
        mode="inline"
        selectedKeys={[location.pathname]}
        items={menuItems}
        onClick={handleMenuClick}
        className="app-nav"
      />
      <div className="app-nav__foot">
        <span className="app-nav__version">LOS v2.0</span>
        {(!collapsed || isMobile) && (
          <span className="app-nav__hint">Банк · Банк бус СФБ-д тохирсон</span>
        )}
      </div>
    </>
  );

  const notifications = activities.slice(0, 5);

  return (
    <Layout className="app-layout">
      {!isMobile && (
        <Sider
          width={252}
          collapsedWidth={80}
          collapsed={collapsed}
          onCollapse={setCollapsed}
          theme="light"
          className="app-sider"
        >
          {navigation}
        </Sider>
      )}

      {isMobile && (
        <Drawer
          placement="left"
          width={272}
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          closable={false}
          styles={{ body: { padding: 0, display: 'flex', flexDirection: 'column' } }}
        >
          {navigation}
        </Drawer>
      )}

      <Layout>
        <Header className="app-header">
          <div className="app-header__left">
            {isMobile ? (
              <Button
                type="text"
                icon={<MenuOutlined />}
                onClick={() => setDrawerOpen(true)}
                aria-label="Цэс нээх"
              />
            ) : (
              <Tooltip title={collapsed ? 'Цэс дэлгэх' : 'Цэс хураах'}>
                <Button
                  type="text"
                  icon={<MenuOutlined />}
                  onClick={() => setCollapsed((value) => !value)}
                  aria-label="Цэс"
                />
              </Tooltip>
            )}
            <div>
              <Breadcrumb
                items={[{ title: 'LOS' }, { title: meta.title }]}
                className="app-breadcrumb"
              />
              <Typography.Title level={4} className="app-header__title">
                {meta.title}
              </Typography.Title>
            </div>
          </div>

          <div className="app-header__right">
            {meta.description && !isMobile && (
              <Typography.Text type="secondary" className="app-header__desc">
                {meta.description}
              </Typography.Text>
            )}
            <Tag color="geekblue" icon={<BankOutlined />} className="app-tag">
              {INSTITUTION_SHORT_LABEL[profile.type]}
            </Tag>
            <Tag color={profile.type === 'BANK' ? 'blue' : 'purple'} className="app-tag app-tag--outline">
              <SwapOutlined /> {profile.branch}
            </Tag>
            <Popover
              trigger="click"
              placement="bottomRight"
              title="Сүүлийн үйл ажиллагаа"
              content={
                <List
                  size="small"
                  className="app-notifications"
                  dataSource={notifications}
                  locale={{ emptyText: 'Мэдээлэл байхгүй' }}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        title={item.action}
                        description={<span className="app-notifications__meta">{item.target}</span>}
                      />
                    </List.Item>
                  )}
                />
              }
            >
              <Badge count={notifications.filter((item) => item.type === 'loan').length} size="small">
                <Button type="text" icon={<BellOutlined />} aria-label="Мэдэгдэл" />
              </Badge>
            </Popover>

            <Dropdown
              trigger={['click']}
              placement="bottomRight"
              menu={{
                items: userMenuItems,
                onClick: ({ key }) => {
                  if (key === 'logout') void handleLogout();
                  if (key === 'profile') navigate('/settings');
                },
              }}
            >
              <button type="button" className="app-user">
                <Avatar style={{ background: '#1d4ed8' }} size={34}>
                  {initialsOf(user?.name ?? 'Хэрэглэгч')}
                </Avatar>
                <span className="app-user__meta">
                  <strong>{user?.name ?? 'Хэрэглэгч'}</strong>
                  <small>{user ? ROLE_LABELS[user.role] : ''}</small>
                </span>
                <DownOutlined className="app-user__caret" />
              </button>
            </Dropdown>
          </div>
        </Header>

        <Content className="app-content">
          <div key={location.pathname} className="page-transition">
            <Outlet />
          </div>
        </Content>

        <Footer className="app-footer">
          <Space split="·" wrap>
            <span>© {new Date().getFullYear()} {profile.name}</span>
            <span>Лиценз: {profile.licenseNo}</span>
            <span>Зээлийн хүсэлтийн систем v2.0</span>
          </Space>
        </Footer>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
