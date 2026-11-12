import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Button,
  Card,
  Input,
  Result,
  Segmented,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { DownloadOutlined } from '@ant-design/icons';
import { PERMISSIONS, useSession } from '../contexts/SessionContext';
import { useLosStore, type ActivityRecord, type ActivityType } from '../services/demoStore';
import { downloadCsv, formatDateTime } from '../utils/format';

const { Title, Text } = Typography;

type ActivityFilter = 'ALL' | ActivityType;

const TYPE_META: Record<ActivityType, { label: string; color: string }> = {
  customer: { label: 'Харилцагч', color: 'blue' },
  loan: { label: 'Зээл', color: 'green' },
  document: { label: 'Баримт', color: 'gold' },
  system: { label: 'Систем', color: 'default' },
};

const AuditPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasPermission } = useSession();
  const { activities } = useLosStore();
  const [filter, setFilter] = useState<ActivityFilter>('ALL');
  const [query, setQuery] = useState('');

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return activities.filter((activity) => {
      const matchesType = filter === 'ALL' || activity.type === filter;
      const matchesQuery =
        !needle ||
        [activity.action, activity.actor, activity.target].join(' ').toLowerCase().includes(needle);
      return matchesType && matchesQuery;
    });
  }, [activities, filter, query]);

  if (!hasPermission(PERMISSIONS.AUDIT_VIEW)) {
    return (
      <Result
        status="403"
        title="Хандах эрхгүй"
        subTitle="Аудит лог үзэх эрх танд байхгүй байна."
        extra={<Button onClick={() => navigate('/dashboard')}>Хяналтын самбар руу буцах</Button>}
      />
    );
  }

  const exportRows = (): void => {
    downloadCsv('audit-log.csv', [
      ['Огноо', 'Хэрэглэгч', 'Үйлдэл', 'Объект', 'Төрөл'],
      ...filtered.map((activity) => [
        formatDateTime(activity.at),
        activity.actor,
        activity.action,
        activity.target,
        TYPE_META[activity.type].label,
      ]),
    ]);
    message.success('Аудит лог татагдлаа');
  };

  const columns: ColumnsType<ActivityRecord> = [
    {
      title: 'Огноо',
      dataIndex: 'at',
      key: 'at',
      width: 180,
      render: (value: string) => formatDateTime(value),
    },
    {
      title: 'Төрөл',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (value: ActivityType) => (
        <Tag color={TYPE_META[value].color}>{TYPE_META[value].label}</Tag>
      ),
    },
    { title: 'Үйлдэл', dataIndex: 'action', key: 'action' },
    { title: 'Объект', dataIndex: 'target', key: 'target' },
    { title: 'Хэрэглэгч', dataIndex: 'actor', key: 'actor', responsive: ['lg'] },
  ];

  return (
    <div>
      <div className="page-head">
        <div>
          <Title level={3} className="page-head__title">Аудит лог</Title>
          <Text className="page-head__sub">
            Системийн бүх үйлдлийн бүртгэл · {activities.length} бичлэг
          </Text>
        </div>
        <div className="page-head__actions">
          <Button icon={<DownloadOutlined />} onClick={exportRows}>
            CSV татах
          </Button>
        </div>
      </div>

      <Card>
        <div className="toolbar">
          <div className="toolbar__group">
            <Input.Search
              allowClear
              placeholder="Үйлдэл, хэрэглэгч, объектоор хайх"
              style={{ width: 300 }}
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            <Segmented
              value={filter}
              onChange={(value) => setFilter(value as ActivityFilter)}
              options={[
                { label: 'Бүгд', value: 'ALL' },
                { label: 'Харилцагч', value: 'customer' },
                { label: 'Зээл', value: 'loan' },
                { label: 'Баримт', value: 'document' },
              ]}
            />
          </div>
          <Text className="muted">{filtered.length} бичлэг</Text>
        </div>

        <Table<ActivityRecord>
          rowKey="id"
          columns={columns}
          dataSource={filtered}
          scroll={{ x: 800 }}
          pagination={{ pageSize: 10, showSizeChanger: false }}
          locale={{ emptyText: 'Бүртгэл олдсонгүй' }}
        />
      </Card>
    </div>
  );
};

export default AuditPage;
