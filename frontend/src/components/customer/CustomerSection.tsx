import React, { useCallback, useEffect, useState } from 'react';
import { Alert, Button, Card, Space, Spin, Table, Tag, Typography } from 'antd';
import { ReloadOutlined, UserOutlined } from '@ant-design/icons';

const { Text } = Typography;

const API_BASE = '/los/api/v1';

interface CustomerRow {
  id: string;
  customerNumber?: string;
  firstName?: string;
  lastName?: string;
  companyName?: string;
  email?: string;
  phone?: string;
  status?: string;
  kycStatus?: string;
  customerType?: string;
}

function authHeaders(): Record<string, string> {
  const token = localStorage.getItem('los_token') || localStorage.getItem('los_auth_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

const STATUS_COLORS: Record<string, string> = {
  ACTIVE: 'green',
  INACTIVE: 'default',
  BLOCKED: 'red',
  SUSPENDED: 'orange',
  COMPLETED: 'green',
  PENDING: 'gold',
};

const KYС_COLORS: Record<string, string> = {
  COMPLETED: 'green',
  PENDING: 'gold',
  REJECTED: 'red',
  IN_PROGRESS: 'blue',
};

/** Харилцагчийн жагсаалтыг backend-ээс татаж харуулна. */
const CustomerSection: React.FC = () => {
  const [rows, setRows] = useState<CustomerRow[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/customers?page=0&size=50`, { headers: authHeaders() });
      if (!res.ok) {
        throw new Error(res.status === 401 || res.status === 403
          ? 'Хандах эрхгүй байна. Дахин нэвтэрнэ үү.'
          : `Серверийн алдаа: HTTP ${res.status}`);
      }
      const json = await res.json();
      const content: CustomerRow[] = json?.data?.content ?? json?.content ?? [];
      setRows(content);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Харилцагчдыг татахад алдаа гарлаа');
      setRows([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const columns = [
    {
      title: 'Дугаар',
      dataIndex: 'customerNumber',
      key: 'customerNumber',
      render: (v?: string) => v || '-',
    },
    {
      title: 'Нэр',
      key: 'name',
      render: (_: unknown, r: CustomerRow) =>
        [r.lastName, r.firstName].filter(Boolean).join(' ') || r.companyName || '-',
    },
    { title: 'Имэйл', dataIndex: 'email', key: 'email', render: (v?: string) => v || '-' },
    { title: 'Утас', dataIndex: 'phone', key: 'phone', render: (v?: string) => v || '-' },
    {
      title: 'Төлөв',
      dataIndex: 'status',
      key: 'status',
      render: (v?: string) => <Tag color={STATUS_COLORS[v || ''] || 'default'}>{v || '-'}</Tag>,
    },
    {
      title: 'KYC',
      dataIndex: 'kycStatus',
      key: 'kycStatus',
      render: (v?: string) => <Tag color={KYС_COLORS[v || ''] || 'default'}>{v || '-'}</Tag>,
    },
  ];

  return (
    <Card
      title={<span><UserOutlined /> Харилцагчийн бүртгэл</span>}
      extra={
        <Space>
          <Text type="secondary">Нийт: {rows.length}</Text>
          <Button icon={<ReloadOutlined />} onClick={() => void load()} loading={loading}>
            Шинэчлэх
          </Button>
        </Space>
      }
    >
      {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 16 }} />}
      {loading && rows.length === 0 ? (
        <div style={{ textAlign: 'center', padding: 32 }}><Spin /></div>
      ) : (
        <Table<CustomerRow>
          rowKey="id"
          size="middle"
          columns={columns as any}
          dataSource={rows}
          pagination={{ pageSize: 10, showSizeChanger: false }}
        />
      )}
    </Card>
  );
};

export default CustomerSection;
