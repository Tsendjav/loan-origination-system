import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Alert,
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  List,
  Popconfirm,
  Result,
  Row,
  Col,
  Segmented,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { CheckCircleFilled, MinusCircleOutlined, ReloadOutlined, SaveOutlined } from '@ant-design/icons';
import {
  INSTITUTION_LABEL,
  PRODUCT_CATALOG,
  useInstitution,
  type InstitutionProfile,
  type InstitutionType,
} from '../contexts/InstitutionContext';
import {
  PERMISSIONS,
  ROLE_LABELS,
  ROLE_PERMISSION_MATRIX,
  useSession,
  type UserRole,
} from '../contexts/SessionContext';
import { losActions } from '../services/demoStore';

const { Title, Text } = Typography;

const CAPABILITIES = [
  { key: 'customer', label: 'Харилцагч', permission: PERMISSIONS.CUSTOMER_VIEW },
  { key: 'loan', label: 'Зээлийн хүсэлт', permission: PERMISSIONS.LOAN_VIEW },
  { key: 'decision', label: 'Шийдвэр', permission: PERMISSIONS.LOAN_APPROVE },
  { key: 'document', label: 'Баримт бичиг', permission: PERMISSIONS.DOCUMENT_VIEW },
  { key: 'report', label: 'Тайлан', permission: PERMISSIONS.REPORT_VIEW },
  { key: 'settings', label: 'Тохиргоо', permission: PERMISSIONS.SETTINGS_MANAGE },
];

interface RoleRow {
  key: UserRole;
  role: string;
  permissions: string[];
}

const SettingsPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasPermission } = useSession();
  const { profile, updateProfile, switchInstitutionType } = useInstitution();
  const [form] = Form.useForm<InstitutionProfile>();

  useEffect(() => {
    form.setFieldsValue(profile);
  }, [form, profile]);

  if (!hasPermission(PERMISSIONS.SETTINGS_MANAGE)) {
    return (
      <Result
        status="403"
        title="Хандах эрхгүй"
        subTitle="Тохиргоо өөрчлөх эрх танд байхгүй байна."
        extra={<Button onClick={() => navigate('/dashboard')}>Хяналтын самбар руу буцах</Button>}
      />
    );
  }

  const handleSave = async (): Promise<void> => {
    try {
      const values = await form.validateFields();
      updateProfile(values);
      message.success('Байгууллагын тохиргоо хадгалагдлаа');
    } catch {
      // Inline validation errors.
    }
  };

  const roleRows: RoleRow[] = (Object.keys(ROLE_LABELS) as UserRole[]).map((role) => ({
    key: role,
    role: ROLE_LABELS[role],
    permissions: ROLE_PERMISSION_MATRIX[role],
  }));

  const roleColumns: ColumnsType<RoleRow> = [
    { title: 'Үүрэг', dataIndex: 'role', key: 'role', fixed: 'left', width: 170 },
    ...CAPABILITIES.map((capability) => ({
      title: capability.label,
      key: capability.key,
      align: 'center' as const,
      render: (_: unknown, row: RoleRow) =>
        row.permissions.includes(capability.permission) ? (
          <CheckCircleFilled style={{ color: '#389e0d' }} />
        ) : (
          <MinusCircleOutlined style={{ color: '#cbd5e1' }} />
        ),
    })),
  ];

  return (
    <div>
      <div className="page-head">
        <div>
          <Title level={3} className="page-head__title">Байгууллагын тохиргоо</Title>
          <Text className="page-head__sub">
            Банк болон банк бус санхүүгийн байгууллагын горим, бүтээгдэхүүн, эрхийн матриц
          </Text>
        </div>
      </div>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Банк ба банк бус санхүүгийн байгууллагад нэг систем"
        description="Байгууллагын төрөл солиход бүтээгдэхүүний жагсаалт, нэршил болон лицензийн мэдээлэл автоматаар тохируулагдана. Үйл ажиллагааны урсгал ижил хэвээр ажиллана."
      />

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card title="Байгууллагын мэдээлэл">
            <div className="toolbar" style={{ marginBottom: 20 }}>
              <div>
                <span className="stat-card__label">Байгууллагын төрөл</span>
                <div style={{ marginTop: 6 }}>
                  <Segmented
                    value={profile.type}
                    onChange={(value) => {
                      switchInstitutionType(value as InstitutionType);
                      message.info(`${INSTITUTION_LABEL[value as InstitutionType]} горимд шилжлээ`);
                    }}
                    options={[
                      { label: 'Банк', value: 'BANK' },
                      { label: 'Банк бус СФБ', value: 'NBFI' },
                    ]}
                  />
                </div>
              </div>
              <Tag color={profile.type === 'BANK' ? 'blue' : 'purple'}>
                {INSTITUTION_LABEL[profile.type]}
              </Tag>
            </div>

            <Form<InstitutionProfile> form={form} layout="vertical" requiredMark={false}>
              <Row gutter={12}>
                <Col span={24}>
                  <Form.Item
                    name="name"
                    label="Байгууллагын нэр"
                    rules={[{ required: true, message: 'Нэр оруулна уу' }]}
                  >
                    <Input />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    name="shortName"
                    label="Товч нэр"
                    rules={[{ required: true, message: 'Товч нэр оруулна уу' }]}
                  >
                    <Input maxLength={12} />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item
                    name="licenseNo"
                    label="Лицензийн дугаар"
                    rules={[{ required: true, message: 'Лицензийн дугаар оруулна уу' }]}
                  >
                    <Input />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item name="branch" label="Салбар / Нэгж">
                    <Input />
                  </Form.Item>
                </Col>
                <Col xs={24} md={12}>
                  <Form.Item name="phone" label="Утас">
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={24}>
                  <Form.Item
                    name="email"
                    label="И-мэйл"
                    rules={[{ type: 'email', message: 'И-мэйл хаяг буруу байна' }]}
                  >
                    <Input />
                  </Form.Item>
                </Col>
              </Row>
              <Space>
                <Button type="primary" icon={<SaveOutlined />} onClick={() => void handleSave()}>
                  Хадгалах
                </Button>
                <Button onClick={() => form.setFieldsValue(profile)}>Өөрчлөлтийг буцаах</Button>
              </Space>
            </Form>
          </Card>
        </Col>

        <Col xs={24} xl={10}>
          <Card title="Бүтээгдэхүүний каталог" className="section-card">
            <List
              dataSource={PRODUCT_CATALOG[profile.type]}
              renderItem={(product) => (
                <List.Item>
                  <List.Item.Meta
                    title={product.name}
                    description={<span className="muted">{product.description}</span>}
                  />
                  <Tag color="blue">Идэвхтэй</Tag>
                </List.Item>
              )}
            />
            <Descriptions
              size="small"
              column={1}
              style={{ marginTop: 16 }}
              items={[
                { key: 'currency', label: 'Валют', children: 'MNT (₮)' },
                { key: 'mode', label: 'Үйл ажиллагааны горим', children: INSTITUTION_LABEL[profile.type] },
                { key: 'branch', label: 'Салбар', children: profile.branch },
              ]}
            />
          </Card>
        </Col>
      </Row>

      <Card title="Эрхийн матриц" style={{ marginTop: 16 }}>
        <Table<RoleRow>
          rowKey="key"
          columns={roleColumns}
          dataSource={roleRows}
          pagination={false}
          size="middle"
          scroll={{ x: 760 }}
        />
        <Text className="muted" style={{ display: 'block', marginTop: 12 }}>
          Эрхүүд нь хэрэглэгчийн үүргээр тодорхойлогдоно. Өөрчлөлтийг системийн админ удирдана.
        </Text>
      </Card>

      <Card title="Өгөгдлийн сан" style={{ marginTop: 16 }}>
        <div className="toolbar" style={{ marginBottom: 0 }}>
          <Text className="muted">
            Демо горимд өгөгдөл таны хөтөч дээр хадгалагдана. Сэргээх үед анхны жишээ өгөгдөл буцаж ирнэ.
          </Text>
          <Popconfirm
            title="Демо өгөгдлийг сэргээх үү?"
            description="Оруулсан бүх өөрчлөлт устах болно."
            okText="Сэргээх"
            cancelText="Болих"
            okButtonProps={{ danger: true }}
            onConfirm={() => {
              losActions.reset();
              message.success('Демо өгөгдөл сэргээгдлээ');
            }}
          >
            <Button danger icon={<ReloadOutlined />}>
              Демо өгөгдөл сэргээх
            </Button>
          </Popconfirm>
        </div>
      </Card>
    </div>
  );
};

export default SettingsPage;
