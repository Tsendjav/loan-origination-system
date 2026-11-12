import React, { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Alert,
  Button,
  Card,
  Col,
  Descriptions,
  Drawer,
  Empty,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Result,
  Row,
  Segmented,
  Select,
  Space,
  Steps,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { DescriptionsProps } from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  DownloadOutlined,
  EyeOutlined,
  PlusOutlined,
  WalletOutlined,
} from '@ant-design/icons';
import { useInstitution } from '../contexts/InstitutionContext';
import { PERMISSIONS, useSession } from '../contexts/SessionContext';
import {
  losActions,
  useLosStore,
  type LoanRecord,
  type RiskLevel,
} from '../services/demoStore';
import {
  LOAN_STATUS_META,
  RISK_META,
  downloadCsv,
  formatDate,
  formatMnt,
  type LoanStatus,
} from '../utils/format';

const { Title, Text } = Typography;

type LoanFilter = 'ALL' | LoanStatus;

interface LoanFormValues {
  customerId: string;
  product: string;
  amount: number;
  termMonths: number;
  rate: number;
  purpose: string;
  risk: RiskLevel;
}

const FILTERS: Array<{ label: string; value: LoanFilter }> = [
  { label: 'Бүгд', value: 'ALL' },
  { label: 'Илгээсэн', value: 'SUBMITTED' },
  { label: 'Хянаж байгаа', value: 'IN_REVIEW' },
  { label: 'Зөвшөөрсөн', value: 'APPROVED' },
  { label: 'Олгосон', value: 'DISBURSED' },
  { label: 'Татгалзсан', value: 'REJECTED' },
];

const LoansPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, hasPermission } = useSession();
  const { profile, products } = useInstitution();
  const { customers, loans } = useLosStore();
  const [searchParams] = useSearchParams();
  const [form] = Form.useForm<LoanFormValues>();

  const initialFocus = searchParams.get('focus');
  const [filter, setFilter] = useState<LoanFilter>('ALL');
  const [query, setQuery] = useState('');
  const [createOpen, setCreateOpen] = useState(searchParams.get('new') === '1');
  const [selectedId, setSelectedId] = useState<string | null>(initialFocus);
  const [rejectId, setRejectId] = useState<string | null>(
    searchParams.get('action') === 'reject' ? initialFocus : null,
  );
  const [rejectReason, setRejectReason] = useState('');
  const [saving, setSaving] = useState(false);

  const actor = user?.name ?? 'Систем';

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return loans.filter((loan) => {
      const matchesStatus = filter === 'ALL' || loan.status === filter;
      const matchesQuery =
        !needle ||
        [loan.number, loan.customerName, loan.product, loan.officer]
          .join(' ')
          .toLowerCase()
          .includes(needle);
      return matchesStatus && matchesQuery;
    });
  }, [loans, filter, query]);

  const selected = loans.find((loan) => loan.id === selectedId) ?? null;
  const rejecting = loans.find((loan) => loan.id === rejectId) ?? null;

  if (!hasPermission(PERMISSIONS.LOAN_VIEW)) {
    return (
      <Result
        status="403"
        title="Хандах эрхгүй"
        subTitle="Зээлийн хүсэлтийг үзэх эрх танд байхгүй байна."
        extra={<Button onClick={() => navigate('/dashboard')}>Хяналтын самбар руу буцах</Button>}
      />
    );
  }

  const changeStatus = (loan: LoanRecord, status: LoanStatus, note: string): void => {
    losActions.setLoanStatus(loan.id, status, actor, note);
    message.success(`${loan.number} · ${LOAN_STATUS_META[status].label}`);
  };

  const handleCreate = async (): Promise<void> => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      const loan = losActions.createLoan(values, actor);
      if (!loan) {
        message.error('Харилцагч олдсонгүй');
        return;
      }
      message.success(`Зээлийн хүсэлт ${loan.number} үүслээ`);
      form.resetFields();
      setCreateOpen(false);
    } catch {
      // Inline validation.
    } finally {
      setSaving(false);
    }
  };

  const handleReject = (): void => {
    if (!rejecting) return;
    if (rejectReason.trim().length < 3) {
      message.warning('Татгалзах шалтгааныг оруулна уу');
      return;
    }
    changeStatus(rejecting, 'REJECTED', rejectReason.trim());
    setRejectId(null);
    setRejectReason('');
  };

  const exportRows = (): void => {
    downloadCsv('loan-applications.csv', [
      ['Дугаар', 'Харилцагч', 'Бүтээгдэхүүн', 'Дүн', 'Хугацаа', 'Хүү', 'Төлөв', 'Огноо'],
      ...filtered.map((loan) => [
        loan.number,
        loan.customerName,
        loan.product,
        loan.amount,
        loan.termMonths,
        loan.rate,
        LOAN_STATUS_META[loan.status].label,
        formatDate(loan.createdAt),
      ]),
    ]);
    message.success('CSV файл татагдлаа');
  };

  const columns: ColumnsType<LoanRecord> = [
    {
      title: 'Дугаар',
      dataIndex: 'number',
      key: 'number',
      render: (value: string) => <Text strong>{value}</Text>,
    },
    { title: 'Харилцагч', dataIndex: 'customerName', key: 'customerName' },
    { title: 'Бүтээгдэхүүн', dataIndex: 'product', key: 'product', responsive: ['lg'] },
    {
      title: 'Дүн',
      dataIndex: 'amount',
      key: 'amount',
      align: 'right',
      render: (value: number) => formatMnt(value),
    },
    {
      title: 'Хугацаа',
      dataIndex: 'termMonths',
      key: 'termMonths',
      align: 'right',
      responsive: ['lg'],
      render: (value: number) => `${value} сар`,
    },
    {
      title: 'Хүү',
      dataIndex: 'rate',
      key: 'rate',
      align: 'right',
      responsive: ['xl'],
      render: (value: number) => `${value}%`,
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
      width: 280,
      render: (_, loan) => (
        <Space size={0} wrap>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => setSelectedId(loan.id)}
          >
            Харах
          </Button>
          {hasPermission(PERMISSIONS.LOAN_APPROVE) &&
            (loan.status === 'SUBMITTED' || loan.status === 'IN_REVIEW') && (
              <Popconfirm
                title="Зээлийг зөвшөөрөх үү?"
                description={`${loan.number} · ${formatMnt(loan.amount)}`}
                okText="Зөвшөөрөх"
                cancelText="Болих"
                onConfirm={() => changeStatus(loan, 'APPROVED', 'Зээлийн хорооны шийдвэрээр зөвшөөрөгдлөө.')}
              >
                <Button type="link" size="small" style={{ color: '#389e0d' }} icon={<CheckCircleOutlined />}>
                  Зөвшөөрөх
                </Button>
              </Popconfirm>
            )}
          {hasPermission(PERMISSIONS.LOAN_REJECT) &&
            (loan.status === 'SUBMITTED' || loan.status === 'IN_REVIEW') && (
              <Button
                type="link"
                size="small"
                danger
                icon={<CloseCircleOutlined />}
                onClick={() => setRejectId(loan.id)}
              >
                Татгалзах
              </Button>
            )}
          {hasPermission(PERMISSIONS.LOAN_DISBURSE) && loan.status === 'APPROVED' && (
            <Popconfirm
              title="Зээлийг олгох уу?"
              description={`${loan.number} · ${formatMnt(loan.amount)}`}
              okText="Олгох"
              cancelText="Болих"
              onConfirm={() => changeStatus(loan, 'DISBURSED', 'Зээлийн гэрээ байгуулж, олголт хийсэн.')}
            >
              <Button type="link" size="small" icon={<WalletOutlined />}>
                Олгох
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const detailItems: DescriptionsProps['items'] = selected
    ? [
        { key: 'number', label: 'Хүсэлтийн дугаар', children: selected.number },
        { key: 'customer', label: 'Харилцагч', children: selected.customerName },
        { key: 'product', label: 'Бүтээгдэхүүн', children: selected.product },
        { key: 'amount', label: 'Зээлийн дүн', children: formatMnt(selected.amount) },
        { key: 'term', label: 'Хугацаа', children: `${selected.termMonths} сар` },
        { key: 'rate', label: 'Жилийн хүү', children: `${selected.rate}%` },
        {
          key: 'risk',
          label: 'Эрсдэлийн түвшин',
          children: <Tag color={RISK_META[selected.risk].color}>{RISK_META[selected.risk].label}</Tag>,
        },
        {
          key: 'status',
          label: 'Төлөв',
          children: (
            <Tag color={LOAN_STATUS_META[selected.status].color}>
              {LOAN_STATUS_META[selected.status].label}
            </Tag>
          ),
        },
        { key: 'purpose', label: 'Зорилго', children: selected.purpose },
        { key: 'officer', label: 'Хариуцсан ажилтан', children: selected.officer },
        { key: 'created', label: 'Илгээсэн огноо', children: formatDate(selected.createdAt) },
        ...(selected.decisionNote
          ? [{ key: 'note', label: 'Шийдвэрийн тэмдэглэл', children: selected.decisionNote }]
          : []),
      ]
    : [];

  const stepIndex = selected
    ? selected.status === 'SUBMITTED'
      ? 0
      : selected.status === 'IN_REVIEW' || selected.status === 'REJECTED'
        ? 1
        : selected.status === 'APPROVED'
          ? 2
          : 3
    : 0;

  return (
    <div>
      <div className="page-head">
        <div>
          <Title level={3} className="page-head__title">Зээлийн хүсэлтүүд</Title>
          <Text className="page-head__sub">
            Нийт {loans.length} хүсэлт · Шийдвэр хүлээж байна{' '}
            {loans.filter((loan) => loan.status === 'SUBMITTED' || loan.status === 'IN_REVIEW').length}
          </Text>
        </div>
        <div className="page-head__actions">
          <Button icon={<DownloadOutlined />} onClick={exportRows}>
            CSV татах
          </Button>
          {hasPermission(PERMISSIONS.LOAN_CREATE) && (
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                form.resetFields();
                form.setFieldsValue({ product: products[0], termMonths: 24, rate: 14.4, risk: 'MEDIUM' });
                setCreateOpen(true);
              }}
            >
              Шинэ хүсэлт
            </Button>
          )}
        </div>
      </div>

      <Card>
        <div className="toolbar">
          <div className="toolbar__group">
            <Input.Search
              allowClear
              placeholder="Дугаар, харилцагч, бүтээгдэхүүнээр хайх"
              style={{ width: 300 }}
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            <Segmented
              value={filter}
              onChange={(value) => setFilter(value as LoanFilter)}
              options={FILTERS.map((item) => ({
                ...item,
                label:
                  item.value === 'ALL'
                    ? item.label
                    : `${item.label} (${loans.filter((loan) => loan.status === item.value).length})`,
              }))}
            />
          </div>
          <Text className="muted">{filtered.length} хүсэлт</Text>
        </div>

        <Table<LoanRecord>
          rowKey="id"
          columns={columns}
          dataSource={filtered}
          scroll={{ x: 1050 }}
          pagination={{ pageSize: 8, showSizeChanger: false }}
          locale={{ emptyText: <Empty description="Хүсэлт олдсонгүй" /> }}
        />
      </Card>

      {/* Detail drawer */}
      <Drawer
        open={Boolean(selected)}
        onClose={() => setSelectedId(null)}
        width={560}
        title={selected ? `Хүсэлт ${selected.number}` : 'Хүсэлтийн дэлгэрэнгүй'}
        footer={
          selected ? (
            <Space wrap>
              <Button onClick={() => setSelectedId(null)}>Хаах</Button>
              {hasPermission(PERMISSIONS.LOAN_APPROVE) &&
                (selected.status === 'SUBMITTED' || selected.status === 'IN_REVIEW') && (
                  <Button
                    type="primary"
                    icon={<CheckCircleOutlined />}
                    onClick={() =>
                      changeStatus(selected, 'APPROVED', 'Зээлийн хорооны шийдвэрээр зөвшөөрөгдлөө.')
                    }
                  >
                    Зөвшөөрөх
                  </Button>
                )}
              {hasPermission(PERMISSIONS.LOAN_REJECT) &&
                (selected.status === 'SUBMITTED' || selected.status === 'IN_REVIEW') && (
                  <Button danger icon={<CloseCircleOutlined />} onClick={() => setRejectId(selected.id)}>
                    Татгалзах
                  </Button>
                )}
              {hasPermission(PERMISSIONS.LOAN_DISBURSE) && selected.status === 'APPROVED' && (
                <Button
                  icon={<WalletOutlined />}
                  onClick={() => changeStatus(selected, 'DISBURSED', 'Зээлийн гэрээ байгуулж, олголт хийсэн.')}
                >
                  Олгох
                </Button>
              )}
            </Space>
          ) : null
        }
      >
        {selected && (
          <div className="stack-16">
            <Steps
              size="small"
              current={stepIndex}
              status={selected.status === 'REJECTED' ? 'error' : 'process'}
              items={[
                { title: 'Илгээсэн' },
                { title: 'Хяналт' },
                { title: 'Шийдвэр' },
                { title: 'Олголт' },
              ]}
            />
            {selected.status === 'REJECTED' && (
              <Alert
                type="error"
                showIcon
                message="Хүсэлт татгалзагдсан"
                description={selected.decisionNote}
              />
            )}
            <Descriptions column={1} size="small" bordered items={detailItems} />
          </div>
        )}
      </Drawer>

      {/* Create drawer */}
      <Drawer
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        width={520}
        forceRender
        title="Шинэ зээлийн хүсэлт"
        footer={
          <Space>
            <Button onClick={() => setCreateOpen(false)}>Болих</Button>
            <Button type="primary" loading={saving} onClick={() => void handleCreate()}>
              Хүсэлт үүсгэх
            </Button>
          </Space>
        }
      >
        <Alert
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
          message={`${profile.name} · ${profile.branch}`}
          description="Хүсэлт үүсгэсний дараа баримт бичиг хавсаргаж, шийдвэрийн урсгал руу шилжүүлнэ."
        />
        <Form<LoanFormValues> form={form} layout="vertical" requiredMark={false}>
          <Form.Item
            name="customerId"
            label="Харилцагч"
            rules={[{ required: true, message: 'Харилцагч сонгоно уу' }]}
          >
            <Select
              showSearch
              optionFilterProp="label"
              placeholder="Харилцагч сонгох"
              options={customers.map((customer) => ({
                value: customer.id,
                label: `${customer.name} (${customer.code})`,
              }))}
            />
          </Form.Item>

          <Row gutter={12}>
            <Col span={24}>
              <Form.Item name="product" label="Бүтээгдэхүүн" rules={[{ required: true }]}>
                <Select options={products.map((product) => ({ value: product, label: product }))} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="amount"
                label="Зээлийн дүн (₮)"
                rules={[{ required: true, message: 'Дүн оруулна уу' }]}
              >
                <InputNumber
                  min={100000}
                  step={100000}
                  style={{ width: '100%' }}
                  placeholder="12,000,000"
                  formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="termMonths" label="Хугацаа (сар)" rules={[{ required: true }]}>
                <InputNumber min={1} max={120} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="rate" label="Жилийн хүү (%)" rules={[{ required: true }]}>
                <InputNumber min={0} max={60} step={0.1} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="risk" label="Эрсдэлийн түвшин" rules={[{ required: true }]}>
                <Select
                  options={Object.entries(RISK_META).map(([value, meta]) => ({
                    value,
                    label: meta.label,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={24}>
              <Form.Item
                name="purpose"
                label="Зээлийн зорилго"
                rules={[{ required: true, message: 'Зорилгоо оруулна уу' }]}
              >
                <Input.TextArea rows={3} placeholder="Жишээ: Орон сууцны урьдчилгаа" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Drawer>

      {/* Reject modal */}
      <Modal
        open={Boolean(rejecting)}
        title={rejecting ? `${rejecting.number} хүсэлтийг татгалзах` : 'Хүсэлт татгалзах'}
        okText="Татгалзах"
        cancelText="Болих"
        okButtonProps={{ danger: true }}
        onOk={handleReject}
        onCancel={() => {
          setRejectId(null);
          setRejectReason('');
        }}
      >
        <p className="muted" style={{ marginTop: 0 }}>
          Татгалзах шалтгааныг тодорхой бичнэ үү. Энэ мэдээлэл аудит логт хадгалагдана.
        </p>
        <Input.TextArea
          rows={4}
          value={rejectReason}
          onChange={(event) => setRejectReason(event.target.value)}
          placeholder="Жишээ: Орлогын баримт хүрэлцэхгүй, барьцаа хөрөнгийн үнэлгээ хангалтгүй"
        />
      </Modal>
    </div>
  );
};

export default LoansPage;
