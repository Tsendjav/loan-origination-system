import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Button,
  Card,
  Col,
  Empty,
  Popconfirm,
  Progress,
  Row,
  Space,
  Table,
  Tag,
  Timeline,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  ArrowRightOutlined,
  BarChartOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  FileTextOutlined,
  FolderOpenOutlined,
  PlusOutlined,
  RiseOutlined,
  TeamOutlined,
  WalletOutlined,
} from '@ant-design/icons';
import { useInstitution } from '../contexts/InstitutionContext';
import { useSession } from '../contexts/SessionContext';
import {
  losActions,
  useLosStore,
  type ActivityType,
  type LoanRecord,
} from '../services/demoStore';
import {
  LOAN_STATUS_META,
  formatCompactMnt,
  formatDate,
  formatMnt,
  type LoanStatus,
} from '../utils/format';

const { Title, Text } = Typography;

const ACTIVITY_COLOR: Record<ActivityType, string> = {
  customer: 'blue',
  loan: 'green',
  document: 'gold',
  system: 'gray',
};

const STATUS_ORDER: LoanStatus[] = ['SUBMITTED', 'IN_REVIEW', 'APPROVED', 'DISBURSED', 'REJECTED'];

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, hasPermission } = useSession();
  const { profile } = useInstitution();
  const { customers, loans, documents, activities } = useLosStore();

  const stats = useMemo(() => {
    const pending = loans.filter((loan) => loan.status === 'SUBMITTED' || loan.status === 'IN_REVIEW');
    const activePortfolio = loans
      .filter((loan) => loan.status === 'APPROVED' || loan.status === 'DISBURSED')
      .reduce((sum, loan) => sum + loan.amount, 0);
    const disbursed = loans
      .filter((loan) => loan.status === 'DISBURSED')
      .reduce((sum, loan) => sum + loan.amount, 0);

    return {
      customers: customers.length,
      kycPending: customers.filter((customer) => customer.kyc === 'PENDING').length,
      loans: loans.length,
      pending,
      pendingDocs: documents.filter((document) => document.status === 'PENDING').length,
      activePortfolio,
      disbursed,
      approvalRate: loans.length === 0 ? 0 : Math.round((loans.filter((loan) => loan.status === 'APPROVED' || loan.status === 'DISBURSED').length / loans.length) * 100),
    };
  }, [customers, loans, documents]);

  const approve = (loan: LoanRecord): void => {
    losActions.setLoanStatus(loan.id, 'APPROVED', user?.name ?? 'Систем', 'Хяналтын самбараас зөвшөөрөгдлөө.');
    message.success(`${loan.number} зээлийн хүсэлт зөвшөөрөгдлөө`);
  };

  const queueColumns: ColumnsType<LoanRecord> = [
    {
      title: 'Дугаар',
      dataIndex: 'number',
      key: 'number',
      render: (value: string) => <Text strong>{value}</Text>,
    },
    { title: 'Харилцагч', dataIndex: 'customerName', key: 'customerName' },
    {
      title: 'Бүтээгдэхүүн',
      dataIndex: 'product',
      key: 'product',
      responsive: ['lg'],
    },
    {
      title: 'Дүн',
      dataIndex: 'amount',
      key: 'amount',
      align: 'right',
      render: (value: number) => formatMnt(value),
    },
    {
      title: 'Төлөв',
      dataIndex: 'status',
      key: 'status',
      render: (status: LoanStatus) => (
        <Tag color={LOAN_STATUS_META[status].color}>{LOAN_STATUS_META[status].label}</Tag>
      ),
    },
    {
      title: 'Үйлдэл',
      key: 'actions',
      width: 250,
      render: (_, loan) => (
        <Space size={4} wrap>
          <Button type="link" size="small" onClick={() => navigate(`/loans?focus=${loan.id}`)}>
            Харах
          </Button>
          {hasPermission('loan:approve') && (
            <Popconfirm
              title="Зээлийг зөвшөөрөх үү?"
              description={`${loan.number} · ${formatMnt(loan.amount)}`}
              okText="Зөвшөөрөх"
              cancelText="Болих"
              onConfirm={() => approve(loan)}
            >
              <Button type="link" size="small" style={{ color: '#389e0d' }} icon={<CheckCircleOutlined />}>
                Зөвшөөрөх
              </Button>
            </Popconfirm>
          )}
          {hasPermission('loan:reject') && (
            <Button
              type="link"
              size="small"
              danger
              icon={<CloseCircleOutlined />}
              onClick={() => navigate(`/loans?focus=${loan.id}&action=reject`)}
            >
              Татгалзах
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const quickActions = [
    {
      key: 'customer',
      title: 'Шинэ харилцагч бүртгэх',
      description: 'Хувь хүн болон байгууллагын мэдээлэл оруулах',
      icon: <TeamOutlined />,
      tint: '#e8efff',
      color: '#1d4ed8',
      to: '/customers?new=1',
      permission: 'customer:create',
    },
    {
      key: 'loan',
      title: 'Зээлийн хүсэлт үүсгэх',
      description: 'Бүтээгдэхүүн, дүн, хугацааг тодорхойлох',
      icon: <FileTextOutlined />,
      tint: '#e7f8ef',
      color: '#15803d',
      to: '/loans?new=1',
      permission: 'loan:create',
    },
    {
      key: 'documents',
      title: 'Баримт бичиг хянах',
      description: 'Хүлээгдэж буй баримтыг баталгаажуулах',
      icon: <FolderOpenOutlined />,
      tint: '#fff7e6',
      color: '#b45309',
      to: '/documents',
      permission: 'document:view',
    },
    {
      key: 'reports',
      title: 'Портфелийн тайлан',
      description: 'Санхүүгийн үзүүлэлт, экспорт',
      icon: <BarChartOutlined />,
      tint: '#f3e8ff',
      color: '#7e22ce',
      to: '/reports',
      permission: 'report:view',
    },
  ].filter((action) => hasPermission(action.permission));

  const today = new Date().toLocaleDateString('mn-MN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  });

  return (
    <div>
      <div className="hero">
        <div>
          <span className="hero__eyebrow">{profile.name} · {today}</span>
          <Title level={2} className="hero__title">
            Сайн байна уу, {user?.name ?? 'хэрэглэгч'}!
          </Title>
          <span className="hero__sub">
            {stats.pending.length} хүсэлт шийдвэр хүлээж, {stats.pendingDocs} баримт хяналт хүлээж байна.
          </span>
        </div>
        <Space className="hero__actions" wrap>
          {hasPermission('loan:create') && (
            <Button type="primary" size="large" icon={<PlusOutlined />} onClick={() => navigate('/loans?new=1')}>
              Шинэ зээлийн хүсэлт
            </Button>
          )}
          {hasPermission('customer:create') && (
            <Button size="large" icon={<TeamOutlined />} onClick={() => navigate('/customers?new=1')}>
              Шинэ харилцагч
            </Button>
          )}
        </Space>
      </div>

      <Row gutter={[16, 16]} style={{ marginTop: 20 }}>
        <Col xs={24} sm={12} xl={6}>
          <Card className="stat-card">
            <div className="stat-card__top">
              <div>
                <span className="stat-card__label">Харилцагч</span>
                <span className="stat-card__value">{stats.customers}</span>
              </div>
              <span className="stat-card__icon" style={{ background: '#e8efff', color: '#1d4ed8' }}>
                <TeamOutlined />
              </span>
            </div>
            <Text className="muted">KYC хүлээж байна: {stats.kycPending}</Text>
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card className="stat-card">
            <div className="stat-card__top">
              <div>
                <span className="stat-card__label">Идэвхтэй зээл</span>
                <span className="stat-card__value">{stats.loans}</span>
              </div>
              <span className="stat-card__icon" style={{ background: '#e7f8ef', color: '#15803d' }}>
                <FileTextOutlined />
              </span>
            </div>
            <Text className="muted">Портфель: {formatCompactMnt(stats.activePortfolio)}</Text>
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card className="stat-card">
            <div className="stat-card__top">
              <div>
                <span className="stat-card__label">Шийдвэр хүлээж байна</span>
                <span className="stat-card__value">{stats.pending.length}</span>
              </div>
              <span className="stat-card__icon" style={{ background: '#fff7e6', color: '#b45309' }}>
                <ClockCircleOutlined />
              </span>
            </div>
            <Text className="muted">Хяналт + илгээсэн хүсэлт</Text>
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card className="stat-card">
            <div className="stat-card__top">
              <div>
                <span className="stat-card__label">Олгосон дүн</span>
                <span className="stat-card__value">{formatCompactMnt(stats.disbursed)}</span>
              </div>
              <span className="stat-card__icon" style={{ background: '#e0f2fe', color: '#0369a1' }}>
                <WalletOutlined />
              </span>
            </div>
            <Text className="muted">
              <RiseOutlined /> Зөвшөөрлийн хувь: {stats.approvalRate}%
            </Text>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} xl={16}>
          <Card
            className="section-card"
            title="Шийдвэр хүлээж буй хүсэлтүүд"
            extra={
              <Button type="link" className="section-card__link" onClick={() => navigate('/loans')}>
                Бүх хүсэлт <ArrowRightOutlined />
              </Button>
            }
          >
            <Table<LoanRecord>
              rowKey="id"
              size="middle"
              columns={queueColumns}
              dataSource={stats.pending}
              pagination={false}
              scroll={{ x: 720 }}
              locale={{ emptyText: <Empty description="Хүлээгдэж буй хүсэлт байхгүй" /> }}
            />
          </Card>
        </Col>

        <Col xs={24} xl={8}>
          <Card className="section-card" title="Портфелийн бүтэц">
            <div className="stack-16">
              {STATUS_ORDER.map((status) => {
                const count = loans.filter((loan) => loan.status === status).length;
                const percent = loans.length === 0 ? 0 : Math.round((count / loans.length) * 100);
                return (
                  <div key={status}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                      <Tag color={LOAN_STATUS_META[status].color}>{LOAN_STATUS_META[status].label}</Tag>
                      <Text type="secondary">{count} · {percent}%</Text>
                    </div>
                    <Progress
                      percent={percent}
                      showInfo={false}
                      strokeColor={LOAN_STATUS_META[status].stroke}
                      size="small"
                    />
                  </div>
                );
              })}
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} xl={12}>
          <Card className="section-card" title="Хурдан үйлдэл">
            <Row gutter={[12, 12]}>
              {quickActions.map((action) => (
                <Col xs={24} md={12} key={action.key}>
                  <button type="button" className="action-tile" onClick={() => navigate(action.to)}>
                    <span
                      className="action-tile__icon"
                      style={{ background: action.tint, color: action.color }}
                    >
                      {action.icon}
                    </span>
                    <span>
                      <span className="action-tile__title">{action.title}</span>
                      <span className="action-tile__desc">{action.description}</span>
                    </span>
                  </button>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>

        <Col xs={24} xl={12}>
          <Card
            className="section-card"
            title="Сүүлийн үйл ажиллагаа"
            extra={
              hasPermission('audit:view') ? (
                <Button type="link" className="section-card__link" onClick={() => navigate('/audit')}>
                  Аудит лог <ArrowRightOutlined />
                </Button>
              ) : null
            }
          >
            {activities.length === 0 ? (
              <Empty description="Үйл ажиллагаа бүртгэгдээгүй" />
            ) : (
              <Timeline
                items={activities.slice(0, 5).map((activity) => ({
                  color: ACTIVITY_COLOR[activity.type],
                  children: (
                    <div>
                      <Text strong>{activity.action}</Text>
                      <div className="muted">{activity.target}</div>
                      <div className="muted">
                        {activity.actor} · {formatDate(activity.at)}
                      </div>
                    </div>
                  ),
                }))}
              />
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default DashboardPage;
