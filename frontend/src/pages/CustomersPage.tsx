import React, { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Button,
  Card,
  Col,
  Descriptions,
  Drawer,
  Form,
  Input,
  Popconfirm,
  Result,
  Row,
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
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  EyeOutlined,
  PlusOutlined,
} from '@ant-design/icons';
import { PERMISSIONS, useSession } from '../contexts/SessionContext';
import {
  losActions,
  useLosStore,
  type CustomerRecord,
  type CustomerType,
  type KycStatus,
  type RiskLevel,
} from '../services/demoStore';
import { KYC_META, RISK_META, downloadCsv, formatDate } from '../utils/format';

const { Title, Text } = Typography;

type CustomerFilter = 'ALL' | 'ACTIVE' | 'KYC';

interface CustomerFormValues {
  name: string;
  type: CustomerType;
  registerNo: string;
  phone: string;
  email: string;
  segment: string;
  kyc: KycStatus;
  risk: RiskLevel;
  status: 'ACTIVE' | 'INACTIVE';
}

const CustomersPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, hasPermission } = useSession();
  const { customers } = useLosStore();
  const [searchParams] = useSearchParams();
  const [form] = Form.useForm<CustomerFormValues>();

  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState<CustomerFilter>('ALL');
  const [open, setOpen] = useState(searchParams.get('new') === '1');
  const [mode, setMode] = useState<'create' | 'edit' | 'view'>('create');
  const [selected, setSelected] = useState<CustomerRecord | null>(null);
  const [saving, setSaving] = useState(false);

  const actor = user?.name ?? 'Систем';

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    return customers.filter((customer) => {
      const matchesQuery =
        !needle ||
        [customer.name, customer.code, customer.registerNo, customer.phone, customer.email]
          .join(' ')
          .toLowerCase()
          .includes(needle);
      const matchesFilter =
        filter === 'ALL' ||
        (filter === 'ACTIVE' && customer.status === 'ACTIVE') ||
        (filter === 'KYC' && customer.kyc === 'PENDING');
      return matchesQuery && matchesFilter;
    });
  }, [customers, query, filter]);

  if (!hasPermission(PERMISSIONS.CUSTOMER_VIEW)) {
    return (
      <Result
        status="403"
        title="Хандах эрхгүй"
        subTitle="Харилцагчийн мэдээллийг үзэх эрх танд байхгүй байна."
        extra={<Button onClick={() => navigate('/dashboard')}>Хяналтын самбар руу буцах</Button>}
      />
    );
  }

  const openCreate = (): void => {
    setMode('create');
    setSelected(null);
    form.resetFields();
    form.setFieldsValue({
      type: 'INDIVIDUAL',
      kyc: 'PENDING',
      risk: 'LOW',
      status: 'ACTIVE',
      segment: 'Жижиглэн',
    });
    setOpen(true);
  };

  const openEdit = (customer: CustomerRecord): void => {
    setMode('edit');
    setSelected(customer);
    form.setFieldsValue({
      name: customer.name,
      type: customer.type,
      registerNo: customer.registerNo,
      phone: customer.phone,
      email: customer.email,
      segment: customer.segment,
      kyc: customer.kyc,
      risk: customer.risk,
      status: customer.status,
    });
    setOpen(true);
  };

  const openView = (customer: CustomerRecord): void => {
    setMode('view');
    setSelected(customer);
    setOpen(true);
  };

  const handleSubmit = async (): Promise<void> => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      if (mode === 'edit' && selected) {
        losActions.updateCustomer(selected.id, values, actor);
        message.success('Харилцагчийн мэдээлэл шинэчлэгдлээ');
      } else {
        losActions.createCustomer(values, actor);
        message.success('Шинэ харилцагч бүртгэгдлээ');
      }
      setOpen(false);
    } catch {
      // Validation errors are shown inline by the form.
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = (customer: CustomerRecord): void => {
    losActions.deleteCustomer(customer.id, actor);
    message.success(`${customer.name} устгагдлаа`);
  };

  const exportRows = (): void => {
    downloadCsv('customers.csv', [
      ['Код', 'Нэр', 'Төрөл', 'Регистр', 'Утас', 'И-мэйл', 'KYC', 'Эрсдэл', 'Төлөв'],
      ...filtered.map((customer) => [
        customer.code,
        customer.name,
        customer.type === 'INDIVIDUAL' ? 'Хувь хүн' : 'Байгууллага',
        customer.registerNo,
        customer.phone,
        customer.email,
        KYC_META[customer.kyc].label,
        RISK_META[customer.risk].label,
        customer.status,
      ]),
    ]);
    message.success('CSV файл татагдлаа');
  };

  const columns: ColumnsType<CustomerRecord> = [
    {
      title: 'Код',
      dataIndex: 'code',
      key: 'code',
      width: 90,
      render: (value: string) => <Text strong>{value}</Text>,
    },
    {
      title: 'Харилцагч',
      key: 'name',
      render: (_, customer) => (
        <div>
          <Text strong>{customer.name}</Text>
          <div className="muted">{customer.type === 'INDIVIDUAL' ? 'Хувь хүн' : 'Байгууллага'}</div>
        </div>
      ),
    },
    { title: 'Регистр', dataIndex: 'registerNo', key: 'registerNo', responsive: ['lg'] },
    { title: 'Утас', dataIndex: 'phone', key: 'phone', responsive: ['md'] },
    { title: 'Сегмент', dataIndex: 'segment', key: 'segment', responsive: ['xl'] },
    {
      title: 'KYC',
      dataIndex: 'kyc',
      key: 'kyc',
      render: (value: keyof typeof KYC_META) => (
        <Tag color={KYC_META[value].color}>{KYC_META[value].label}</Tag>
      ),
    },
    {
      title: 'Эрсдэл',
      dataIndex: 'risk',
      key: 'risk',
      render: (value: keyof typeof RISK_META) => (
        <Tag color={RISK_META[value].color}>{RISK_META[value].label}</Tag>
      ),
    },
    {
      title: 'Үйлдэл',
      key: 'actions',
      width: 260,
      render: (_, customer) => (
        <Space size={0} wrap>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openView(customer)}>
            Харах
          </Button>
          {hasPermission(PERMISSIONS.CUSTOMER_UPDATE) && (
            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(customer)}>
              Засах
            </Button>
          )}
          {hasPermission(PERMISSIONS.CUSTOMER_DELETE) && (
            <Popconfirm
              title="Харилцагчийг устгах уу?"
              description={`${customer.code} · ${customer.name}`}
              okText="Устгах"
              cancelText="Болих"
              okButtonProps={{ danger: true }}
              onConfirm={() => handleDelete(customer)}
            >
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                Устгах
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const detailItems: DescriptionsProps['items'] = selected
    ? [
        { key: 'code', label: 'Код', children: selected.code },
        { key: 'name', label: 'Нэр', children: selected.name },
        {
          key: 'type',
          label: 'Төрөл',
          children: selected.type === 'INDIVIDUAL' ? 'Хувь хүн' : 'Байгууллага',
        },
        { key: 'register', label: 'Регистр', children: selected.registerNo },
        { key: 'phone', label: 'Утас', children: selected.phone },
        { key: 'email', label: 'И-мэйл', children: selected.email || '—' },
        { key: 'segment', label: 'Сегмент', children: selected.segment },
        {
          key: 'kyc',
          label: 'KYC',
          children: <Tag color={KYC_META[selected.kyc].color}>{KYC_META[selected.kyc].label}</Tag>,
        },
        {
          key: 'risk',
          label: 'Эрсдэл',
          children: <Tag color={RISK_META[selected.risk].color}>{RISK_META[selected.risk].label}</Tag>,
        },
        {
          key: 'status',
          label: 'Төлөв',
          children: <Tag color={selected.status === 'ACTIVE' ? 'green' : 'default'}>{selected.status === 'ACTIVE' ? 'Идэвхтэй' : 'Идэвхгүй'}</Tag>,
        },
        { key: 'created', label: 'Бүртгэсэн', children: formatDate(selected.createdAt) },
      ]
    : [];

  return (
    <div>
      <div className="page-head">
        <div>
          <Title level={3} className="page-head__title">Харилцагчийн бүртгэл</Title>
          <Text className="page-head__sub">
            Нийт {customers.length} харилцагч · KYC хүлээж байна{' '}
            {customers.filter((customer) => customer.kyc === 'PENDING').length}
          </Text>
        </div>
        <div className="page-head__actions">
          <Button icon={<DownloadOutlined />} onClick={exportRows}>
            CSV татах
          </Button>
          {hasPermission(PERMISSIONS.CUSTOMER_CREATE) && (
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
              Шинэ харилцагч
            </Button>
          )}
        </div>
      </div>

      <Card>
        <div className="toolbar">
          <div className="toolbar__group">
            <Input.Search
              allowClear
              placeholder="Нэр, код, регистр, утсаар хайх"
              style={{ width: 300 }}
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
            <Segmented
              value={filter}
              onChange={(value) => setFilter(value as CustomerFilter)}
              options={[
                { label: 'Бүгд', value: 'ALL' },
                { label: 'Идэвхтэй', value: 'ACTIVE' },
                { label: 'KYC хүлээж буй', value: 'KYC' },
              ]}
            />
          </div>
          <Text className="muted">{filtered.length} бичлэг харагдаж байна</Text>
        </div>

        <Table<CustomerRecord>
          rowKey="id"
          columns={columns}
          dataSource={filtered}
          scroll={{ x: 900 }}
          pagination={{ pageSize: 8, showSizeChanger: false }}
          locale={{ emptyText: 'Харилцагч олдсонгүй' }}
        />
      </Card>

      <Drawer
        open={open}
        onClose={() => setOpen(false)}
        width={520}
        forceRender
        title={
          mode === 'create' ? 'Шинэ харилцагч' : mode === 'edit' ? 'Харилцагч засах' : 'Харилцагчийн дэлгэрэнгүй'
        }
        footer={
          mode === 'view' ? (
            <Button onClick={() => setOpen(false)}>Хаах</Button>
          ) : (
            <Space>
              <Button onClick={() => setOpen(false)}>Болих</Button>
              <Button type="primary" loading={saving} onClick={() => void handleSubmit()}>
                Хадгалах
              </Button>
            </Space>
          )
        }
      >
        {mode === 'view' ? (
          <Descriptions column={1} size="small" bordered items={detailItems} />
        ) : (
          <Form<CustomerFormValues> form={form} layout="vertical" requiredMark={false}>
            <Row gutter={12}>
              <Col span={24}>
                <Form.Item
                  name="name"
                  label="Нэр / Байгууллагын нэр"
                  rules={[{ required: true, message: 'Нэр оруулна уу' }]}
                >
                  <Input placeholder="Батбаяр Болд" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="type" label="Төрөл" rules={[{ required: true }]}>
                  <Select
                    options={[
                      { value: 'INDIVIDUAL', label: 'Хувь хүн' },
                      { value: 'CORPORATE', label: 'Байгууллага' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="segment" label="Сегмент">
                  <Select
                    options={[
                      { value: 'Жижиглэн', label: 'Жижиглэн' },
                      { value: 'Ипотек', label: 'Ипотек' },
                      { value: 'Цалингийн', label: 'Цалингийн' },
                      { value: 'Бизнес', label: 'Бизнес' },
                      { value: 'Жижиг бизнес', label: 'Жижиг бизнес' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="registerNo"
                  label="Регистр / Байгууллагын дугаар"
                  rules={[{ required: true, message: 'Регистрийн дугаар оруулна уу' }]}
                >
                  <Input placeholder="УБ98010112" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="phone"
                  label="Утас"
                  rules={[{ required: true, message: 'Утасны дугаар оруулна уу' }]}
                >
                  <Input placeholder="9911-2233" />
                </Form.Item>
              </Col>
              <Col span={24}>
                <Form.Item name="email" label="И-мэйл" rules={[{ type: 'email', message: 'И-мэйл буруу байна' }]}>
                  <Input placeholder="name@example.mn" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="kyc" label="KYC">
                  <Select
                    options={Object.entries(KYC_META).map(([value, meta]) => ({
                      value,
                      label: meta.label,
                    }))}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="risk" label="Эрсдэл">
                  <Select
                    options={Object.entries(RISK_META).map(([value, meta]) => ({
                      value,
                      label: meta.label,
                    }))}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="status" label="Төлөв">
                  <Select
                    options={[
                      { value: 'ACTIVE', label: 'Идэвхтэй' },
                      { value: 'INACTIVE', label: 'Идэвхгүй' },
                    ]}
                  />
                </Form.Item>
              </Col>
            </Row>
          </Form>
        )}
      </Drawer>
    </div>
  );
};

export default CustomersPage;
