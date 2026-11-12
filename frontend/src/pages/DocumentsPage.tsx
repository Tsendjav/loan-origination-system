import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Button,
  Card,
  Descriptions,
  Drawer,
  Form,
  Input,
  Modal,
  Popconfirm,
  Result,
  Segmented,
  Select,
  Space,
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
} from '@ant-design/icons';
import { PERMISSIONS, useSession } from '../contexts/SessionContext';
import {
  losActions,
  useLosStore,
  type DocumentRecord,
  type DocumentStatus,
} from '../services/demoStore';
import { DOC_STATUS_META, downloadCsv, formatDateTime } from '../utils/format';

const { Title, Text } = Typography;

type DocumentFilter = 'ALL' | DocumentStatus;

interface DocumentFormValues {
  loanId: string;
  name: string;
  category: string;
}

const CATEGORIES = [
  'Хувь хүний бичиг баримт',
  'Орлогын баримт',
  'Байгууллагын баримт',
  'Санхүүгийн тайлан',
  'Барьцаа хөрөнгө',
  'Бусад',
];

const DocumentsPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, hasPermission } = useSession();
  const { documents, loans } = useLosStore();
  const [form] = Form.useForm<DocumentFormValues>();

  const [filter, setFilter] = useState<DocumentFilter>('ALL');
  const [query, setQuery] = useState('');
  const [createOpen, setCreateOpen] = useState(false);
  const [viewing, setViewing] = useState<DocumentRecord | null>(null);
  const [saving, setSaving] = useState(false);

  const actor = user?.name ?? 'Систем';

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return documents.filter((document) => {
      const matchesStatus = filter === 'ALL' || document.status === filter;
      const matchesQuery =
        !needle ||
        [document.name, document.category, document.loanNumber, document.customerName]
          .join(' ')
          .toLowerCase()
          .includes(needle);
      return matchesStatus && matchesQuery;
    });
  }, [documents, filter, query]);

  if (!hasPermission(PERMISSIONS.DOCUMENT_VIEW)) {
    return (
      <Result
        status="403"
        title="Хандах эрхгүй"
        subTitle="Баримт бичгийн хэсгийг үзэх эрх танд байхгүй байна."
        extra={<Button onClick={() => navigate('/dashboard')}>Хяналтын самбар руу буцах</Button>}
      />
    );
  }

  const updateStatus = (document: DocumentRecord, status: DocumentStatus): void => {
    losActions.setDocumentStatus(document.id, status, actor);
    message.success(
      status === 'VERIFIED'
        ? `${document.name} баталгаажлаа`
        : `${document.name} буцаагдлаа`,
    );
  };

  const handleCreate = async (): Promise<void> => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      losActions.addDocument(values, actor);
      message.success('Баримт бичиг хавсаргагдлаа');
      form.resetFields();
      setCreateOpen(false);
    } catch {
      // Inline validation.
    } finally {
      setSaving(false);
    }
  };

  const exportRows = (): void => {
    downloadCsv('documents.csv', [
      ['Зээлийн дугаар', 'Харилцагч', 'Баримтын нэр', 'Ангилал', 'Төлөв', 'Огноо'],
      ...filtered.map((document) => [
        document.loanNumber,
        document.customerName,
        document.name,
        document.category,
        DOC_STATUS_META[document.status].label,
        formatDateTime(document.uploadedAt),
      ]),
    ]);
    message.success('CSV файл татагдлаа');
  };

  const columns: ColumnsType<DocumentRecord> = [
    {
      title: 'Хүсэлт',
      dataIndex: 'loanNumber',
      key: 'loanNumber',
      render: (value: string) => <Text strong>{value}</Text>,
    },
    { title: 'Харилцагч', dataIndex: 'customerName', key: 'customerName' },
    {
      title: 'Баримт',
      key: 'name',
      render: (_, document) => (
        <div>
          <Text>{document.name}</Text>
          <div className="muted">{document.category}</div>
        </div>
      ),
    },
    {
      title: 'Огноо',
      dataIndex: 'uploadedAt',
      key: 'uploadedAt',
      responsive: ['lg'],
      render: (value: string) => formatDateTime(value),
    },
    {
      title: 'Төлөв',
      dataIndex: 'status',
      key: 'status',
      render: (status: DocumentStatus) => (
        <Tag color={DOC_STATUS_META[status].color}>{DOC_STATUS_META[status].label}</Tag>
      ),
    },
    {
      title: 'Үйлдэл',
      key: 'actions',
      width: 300,
      render: (_, document) => (
        <Space size={0} wrap>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => setViewing(document)}>
            Харах
          </Button>
          {hasPermission(PERMISSIONS.DOCUMENT_VERIFY) && document.status !== 'VERIFIED' && (
            <Popconfirm
              title="Баримтыг баталгаажуулах уу?"
              okText="Баталгаажуулах"
              cancelText="Болих"
              onConfirm={() => updateStatus(document, 'VERIFIED')}
            >
              <Button type="link" size="small" style={{ color: '#389e0d' }} icon={<CheckCircleOutlined />}>
                Баталгаажуулах
              </Button>
            </Popconfirm>
          )}
          {hasPermission(PERMISSIONS.DOCUMENT_VERIFY) && document.status !== 'REJECTED' && (
            <Popconfirm
              title="Баримтыг буцаах уу?"
              okText="Буцаах"
              cancelText="Болих"
              okButtonProps={{ danger: true }}
              onConfirm={() => updateStatus(document, 'REJECTED')}
            >
              <Button type="link" size="small" danger icon={<CloseCircleOutlined />}>
                Буцаах
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const detailItems: DescriptionsProps['items'] = viewing
    ? [
        { key: 'loan', label: 'Зээлийн дугаар', children: viewing.loanNumber },
        { key: 'customer', label: 'Харилцагч', children: viewing.customerName },
        { key: 'name', label: 'Баримтын нэр', children: viewing.name },
        { key: 'category', label: 'Ангилал', children: viewing.category },
        {
          key: 'status',
          label: 'Төлөв',
          children: (
            <Tag color={DOC_STATUS_META[viewing.status].color}>
              {DOC_STATUS_META[viewing.status].label}
            </Tag>
          ),
        },
        { key: 'date', label: 'Хавсаргасан', children: formatDateTime(viewing.uploadedAt) },
      ]
    : [];

  return (
    <div>
      <div className="page-head">
        <div>
          <Title level={3} className="page-head__title">Баримт бичиг</Title>
          <Text className="page-head__sub">
            Нийт {documents.length} баримт · Хяналт хүлээж байна{' '}
            {documents.filter((document) => document.status === 'PENDING').length}
          </Text>
        </div>
        <div className="page-head__actions">
          <Button icon={<DownloadOutlined />} onClick={exportRows}>
            CSV татах
          </Button>
          {hasPermission(PERMISSIONS.DOCUMENT_UPLOAD) && (
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                form.resetFields();
                setCreateOpen(true);
              }}
            >
              Баримт нэмэх
            </Button>
          )}
        </div>
      </div>

      <Card>
        <Alert
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
          message="Баримтын бүрэн бүтэн байдал"
          description="Зээлийн шийдвэр гаргахаас өмнө орлого, барьцаа болон байгууллагын баримтыг баталгаажуулна уу."
        />

        <div className="toolbar">
          <div className="toolbar__group">
            <Input.Search
              allowClear
              placeholder="Баримт, хүсэлт, харилцагчаар хайх"
              style={{ width: 300 }}
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            <Segmented
              value={filter}
              onChange={(value) => setFilter(value as DocumentFilter)}
              options={[
                { label: 'Бүгд', value: 'ALL' },
                { label: 'Хүлээж буй', value: 'PENDING' },
                { label: 'Баталгаажсан', value: 'VERIFIED' },
                { label: 'Татгалзсан', value: 'REJECTED' },
              ]}
            />
          </div>
          <Text className="muted">{filtered.length} баримт</Text>
        </div>

        <Table<DocumentRecord>
          rowKey="id"
          columns={columns}
          dataSource={filtered}
          scroll={{ x: 900 }}
          pagination={{ pageSize: 8, showSizeChanger: false }}
          locale={{ emptyText: 'Баримт олдсонгүй' }}
        />
      </Card>

      <Drawer
        open={Boolean(viewing)}
        onClose={() => setViewing(null)}
        width={480}
        title="Баримтын дэлгэрэнгүй"
        footer={<Button onClick={() => setViewing(null)}>Хаах</Button>}
      >
        <Descriptions column={1} size="small" bordered items={detailItems} />
        <Alert
          style={{ marginTop: 16 }}
          type="warning"
          showIcon
          message="Файлын урьдчилан харах"
          description="Демо горимд файлын агуулга хадгалагдахгүй. Сервер холбогдсон үед файлыг эндээс үзэх боломжтой."
        />
      </Drawer>

      <Modal
        open={createOpen}
        title="Баримт бичиг нэмэх"
        okText="Хавсаргах"
        cancelText="Болих"
        confirmLoading={saving}
        onOk={() => void handleCreate()}
        onCancel={() => setCreateOpen(false)}
      >
        <Form<DocumentFormValues> form={form} layout="vertical" requiredMark={false}>
          <Form.Item
            name="loanId"
            label="Зээлийн хүсэлт"
            rules={[{ required: true, message: 'Хүсэлт сонгоно уу' }]}
          >
            <Select
              showSearch
              optionFilterProp="label"
              placeholder="Хүсэлт сонгох"
              options={loans.map((loan) => ({
                value: loan.id,
                label: `${loan.number} · ${loan.customerName}`,
              }))}
            />
          </Form.Item>
          <Form.Item
            name="name"
            label="Баримтын нэр"
            rules={[{ required: true, message: 'Баримтын нэр оруулна уу' }]}
          >
            <Input placeholder="Жишээ: Цалингийн тодорхойлолт" />
          </Form.Item>
          <Form.Item name="category" label="Ангилал" rules={[{ required: true }]}>
            <Select options={CATEGORIES.map((category) => ({ value: category, label: category }))} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DocumentsPage;
