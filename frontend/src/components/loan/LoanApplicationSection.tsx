import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Col,
  InputNumber,
  Row,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import { CheckOutlined, CloseOutlined, FileTextOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';

const { Text } = Typography;

const API_BASE = '/los/api/v1';

interface LoanRow {
  id: string;
  applicationNumber?: string;
  customerId?: string;
  customerName?: string;
  loanType?: string;
  requestedAmount?: number;
  requestedTermMonths?: number;
  status?: string;
  approvedRate?: number;
  monthlyPayment?: number;
  decisionReason?: string;
  createdAt?: string;
}

interface CustomerOption {
  id: string;
  label: string;
}

const STATUS_COLORS: Record<string, string> = {
  DRAFT: 'default',
  SUBMITTED: 'blue',
  PENDING: 'gold',
  PENDING_DOCUMENTS: 'gold',
  UNDER_REVIEW: 'orange',
  APPROVED: 'green',
  REJECTED: 'red',
  CANCELLED: 'default',
  DISBURSED: 'purple',
};

function authHeaders(): Record<string, string> {
  const token = localStorage.getItem('los_token') || localStorage.getItem('los_auth_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

function currentRoles(): string[] {
  try {
    const raw = localStorage.getItem('los_user');
    if (!raw) return [];
    const user = JSON.parse(raw);
    const roles: Array<{ name?: string }> = user.roles || [];
    return roles.map(r => r.name || '').filter(Boolean).concat(user.role ? [user.role] : []);
  } catch {
    return [];
  }
}

/** Зээлийн хүсэлтийн жагсаалт + шийдвэрийн үйлдэл (эрхээр хязгаарлана). */
const LoanApplicationSection: React.FC = () => {
  const [rows, setRows] = useState<LoanRow[]>([]);
  const [customers, setCustomers] = useState<CustomerOption[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [actionId, setActionId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [customerId, setCustomerId] = useState<string | undefined>(undefined);
  const [amount, setAmount] = useState<number>(5000000);
  const [term, setTerm] = useState<number>(24);

  const roles = useMemo(() => currentRoles(), []);
  const canDecide = roles.some(r => ['SUPER_ADMIN', 'ADMIN', 'MANAGER'].includes(r));
  const canCreate = roles.some(r => ['SUPER_ADMIN', 'ADMIN', 'MANAGER', 'LOAN_OFFICER', 'CUSTOMER_SERVICE'].includes(r));

  const load = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const [appsRes, custRes] = await Promise.all([
        fetch(`${API_BASE}/loan-applications?page=0&size=50`, { headers: authHeaders() }),
        fetch(`${API_BASE}/customers?page=0&size=100`, { headers: authHeaders() }),
      ]);

      if (!appsRes.ok) {
        throw new Error(appsRes.status === 401 || appsRes.status === 403
          ? 'Хандах эрхгүй байна. Дахин нэвтэрнэ үү.'
          : `Серверийн алдаа: HTTP ${appsRes.status}`);
      }

      const appsJson = await appsRes.json();
      setRows(appsJson?.data?.content ?? appsJson?.content ?? []);

      if (custRes.ok) {
        const custJson = await custRes.json();
        const list: any[] = custJson?.data?.content ?? custJson?.content ?? [];
        setCustomers(list.map(c => ({
          id: c.id,
          label: [c.lastName, c.firstName].filter(Boolean).join(' ') || c.companyName || c.customerNumber || c.id,
        })));
      }
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Хүсэлтүүдийг татахад алдаа гарлаа');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const createApplication = useCallback(async (): Promise<void> => {
    if (!customerId) {
      message.warning('Харилцагч сонгоно уу');
      return;
    }
    setActionId('create');
    try {
      const res = await fetch(`${API_BASE}/loan-applications`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify({
          customerId,
          loanType: 'PERSONAL',
          requestedAmount: amount,
          requestedTermMonths: term,
          purpose: 'UI-с илгээсэн хүсэлт',
          saveAsDraft: false,
        }),
      });
      const json = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(json?.error || json?.message || `HTTP ${res.status}`);
      }
      const status = json?.data?.status;
      message.success(
        status === 'APPROVED'
          ? 'Хүсэлт хүлээн авч автоматаар ЗӨВШӨӨРӨГДЛӨӨ'
          : `Хүсэлт хүлээн авлаа (${status || '-'}) — гараар хянана`,
      );
      await load();
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Хүсэлт үүсгэхэд алдаа гарлаа');
    } finally {
      setActionId(null);
    }
  }, [customerId, amount, term, load]);

  const decide = useCallback(async (row: LoanRow, decision: 'approve' | 'reject'): Promise<void> => {
    setActionId(row.id);
    try {
      const params = decision === 'approve'
        ? `approvedAmount=${row.requestedAmount ?? 0}&approvedTermMonths=${row.requestedTermMonths ?? 12}&approvedRate=0.185&comment=UI`
        : 'reason=Хүсэлтийг гараар татгалзлаа';
      const res = await fetch(
        `${API_BASE}/loan-applications/${row.id}/${decision}?${params}`,
        { method: 'PUT', headers: authHeaders() },
      );
      const json = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(json?.error || json?.message || `HTTP ${res.status}`);
      }
      message.success(decision === 'approve' ? 'Зээл зөвшөөрөгдлөө' : 'Зээл татгалзагдлаа');
      await load();
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Шийдвэр гүйцэтгэхэд алдаа гарлаа');
    } finally {
      setActionId(null);
    }
  }, [load]);

  const columns = [
    { title: 'Дугаар', dataIndex: 'applicationNumber', key: 'applicationNumber' },
    { title: 'Харилцагч', dataIndex: 'customerName', key: 'customerName', render: (v?: string) => v || '-' },
    { title: 'Төрөл', dataIndex: 'loanType', key: 'loanType' },
    {
      title: 'Дүн',
      dataIndex: 'requestedAmount',
      key: 'requestedAmount',
      render: (v?: number) => (v != null ? `${v.toLocaleString()}₮` : '-'),
    },
    { title: 'Хугацаа', dataIndex: 'requestedTermMonths', key: 'requestedTermMonths', render: (v?: number) => (v ? `${v} сар` : '-') },
    {
      title: 'Төлөв',
      dataIndex: 'status',
      key: 'status',
      render: (v?: string) => <Tag color={STATUS_COLORS[v || ''] || 'default'}>{v || '-'}</Tag>,
    },
    {
      title: 'Сарын төлбөр',
      dataIndex: 'monthlyPayment',
      key: 'monthlyPayment',
      render: (v?: number) => (v != null ? `${Number(v).toLocaleString()}₮` : '-'),
    },
    {
      title: 'Шийдвэр',
      key: 'actions',
      render: (_: unknown, row: LoanRow) => {
        const decidable = canDecide && ['SUBMITTED', 'UNDER_REVIEW', 'PENDING'].includes(row.status || '');
        if (!decidable) return <Text type="secondary">{canDecide ? '-' : 'Эрх хүрэхгүй'}</Text>;
        return (
          <Space>
            <Button
              size="small"
              type="primary"
              icon={<CheckOutlined />}
              loading={actionId === row.id}
              onClick={() => void decide(row, 'approve')}
            >
              Зөвшөөрөх
            </Button>
            <Button
              size="small"
              danger
              icon={<CloseOutlined />}
              loading={actionId === row.id}
              onClick={() => void decide(row, 'reject')}
            >
              Татгалзах
            </Button>
          </Space>
        );
      },
    },
  ];

  return (
    <Card
      title={<span><FileTextOutlined /> Зээлийн хүсэлтүүд</span>}
      extra={
        <Button icon={<ReloadOutlined />} onClick={() => void load()} loading={loading}>
          Шинэчлэх
        </Button>
      }
    >
      {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 16 }} />}

      {canCreate && (
        <Card size="small" title="Шинэ хүсэлт (хүлээн авах)" style={{ marginBottom: 16 }}>
          <Row gutter={[12, 12]} align="middle">
            <Col xs={24} md={8}>
              <Select
                showSearch
                style={{ width: '100%' }}
                placeholder="Харилцагч сонгох"
                value={customerId}
                onChange={setCustomerId}
                optionFilterProp="label"
                options={customers.map(c => ({ value: c.id, label: c.label }))}
              />
            </Col>
            <Col xs={24} md={6}>
              <InputNumber
                style={{ width: '100%' }}
                min={100000}
                step={500000}
                value={amount}
                onChange={(v) => setAmount(Number(v || 0))}
                addonAfter="₮"
              />
            </Col>
            <Col xs={24} md={4}>
              <InputNumber
                style={{ width: '100%' }}
                min={3}
                max={120}
                value={term}
                onChange={(v) => setTerm(Number(v || 0))}
                addonAfter="сар"
              />
            </Col>
            <Col xs={24} md={6}>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                loading={actionId === 'create'}
                onClick={() => void createApplication()}
                block
              >
                Хүсэлт илгээх
              </Button>
            </Col>
          </Row>
          <Text type="secondary" style={{ display: 'block', marginTop: 8 }}>
            Автомат шийдвэр: ≤ 10,000,000₮, ≤ 60 сар, кредит оноо ≥ 650 бол шууд зөвшөөрөгдөнө.
          </Text>
        </Card>
      )}

      {loading && rows.length === 0 ? (
        <div style={{ textAlign: 'center', padding: 32 }}><Spin /></div>
      ) : (
        <Table<LoanRow>
          rowKey="id"
          size="middle"
          columns={columns as any}
          dataSource={rows}
          pagination={{ pageSize: 10, showSizeChanger: false }}
          scroll={{ x: 900 }}
        />
      )}
    </Card>
  );
};

export default LoanApplicationSection;
