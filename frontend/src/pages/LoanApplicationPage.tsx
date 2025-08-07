import React, { useState, useEffect } from 'react';
import {
  Card,
  Steps,
  Form,
  Input,
  Select,
  InputNumber,
  Upload,
  Button,
  Row,
  Col,
  message,
  Modal,
  Table,
  Tag,
  Space,
  Divider,
  Typography,
  Alert,
  Drawer
} from 'antd';
import {
  PlusOutlined,
  UploadOutlined,
  EyeOutlined,
  EditOutlined,
  FileTextOutlined,
  DollarOutlined,
  UserOutlined,
  BankOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';

// Import types from our type definitions
import { 
  LoanApplication, 
  Customer, 
  LoanApplicationStatus,
  LoanType,
  RiskLevel // Changed from RiskRating to RiskLevel
} from '../types';

// Mock services to avoid import errors
const loanService = {
  getLoanApplications: async () => {
    return [
      {
        id: '1',
        applicationNumber: 'LN-2024-0001',
        customerName: 'Бат Болд',
        loanType: LoanType.PERSONAL,
        requestedAmount: 5000000,
        requestedTermMonths: 24,
        status: LoanApplicationStatus.SUBMITTED,
        applicationDate: new Date().toISOString(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        customerId: '1',
        purpose: 'Гэрийн өрөө засвар',
        interestRate: 12.5
      } as LoanApplication
    ];
  },
  createLoanApplication: async (data: any) => {
    return {
      ...data,
      id: Date.now().toString(),
      applicationNumber: `LN-${new Date().getFullYear()}-${String(Date.now()).slice(-4)}`,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
  }
};

const customerService = {
  getCustomers: async () => {
    return {
      data: {
        content: [
          {
            id: '1',
            firstName: 'Бат',
            lastName: 'Болд',
            email: 'bat@email.com',
            phone: '99112233',
            customerType: 'INDIVIDUAL' as const,
            registerNumber: 'УБ12345678',
            status: 'ACTIVE' as const,
            kycStatus: 'APPROVED' as const,
            riskLevel: 'LOW' as RiskLevel, // Changed from RiskRating to RiskLevel
            isActive: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          } as Customer
        ]
      }
    };
  }
};

// LoadingSpinner component
const LoadingSpinner: React.FC<{ tip?: string }> = ({ tip }) => (
  <div className="flex flex-col items-center justify-center py-8">
    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
    {tip && <p className="mt-2 text-sm text-gray-600">{tip}</p>}
  </div>
);

const { Step } = Steps;
const { Option } = Select;
const { TextArea } = Input;
const { Title, Text } = Typography;

interface LoanApplicationPageProps {}

const LoanApplicationPage: React.FC<LoanApplicationPageProps> = () => {
  const [form] = Form.useForm();
  const [currentStep, setCurrentStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [applications, setApplications] = useState<LoanApplication[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [selectedApplication, setSelectedApplication] = useState<LoanApplication | null>(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [viewDrawerVisible, setViewDrawerVisible] = useState(false);
  
  useEffect(() => {
    loadApplications();
    loadCustomers();
  }, []);

  const loadApplications = async () => {
    try {
      setLoading(true);
      const data = await loanService.getLoanApplications();
      setApplications(data);
    } catch (error) {
      message.error('Зээлийн хүсэлтүүдийг ачаалахад алдаа гарлаа');
    } finally {
      setLoading(false);
    }
  };

  const loadCustomers = async () => {
    try {
      const data = await customerService.getCustomers();
      setCustomers(data.data.content);
    } catch (error) {
      message.error('Харилцагчдын жагсаалтыг ачаалахад алдаа гарлаа');
    }
  };

  const steps = [
    {
      title: 'Харилцагч',
      content: 'customer-info',
      icon: <UserOutlined />
    },
    {
      title: 'Зээлийн мэдээлэл',
      content: 'loan-info',
      icon: <DollarOutlined />
    },
    {
      title: 'Баримт бичиг',
      content: 'documents',
      icon: <FileTextOutlined />
    },
    {
      title: 'Баталгаажуулалт',
      content: 'confirmation',
      icon: <CheckCircleOutlined />
    }
  ];

  const loanTypes = [
    { value: LoanType.PERSONAL, label: 'Хувийн зээл' },
    { value: LoanType.MORTGAGE, label: 'Ипотекийн зээл' },
    { value: LoanType.AUTO, label: 'Автомашины зээл' },
    { value: LoanType.BUSINESS, label: 'Бизнес зээл' },
    { value: LoanType.EDUCATION, label: 'Боловсролын зээл' }
  ];

  const handleSubmit = async (values: any) => {
    try {
      setLoading(true);
      const applicationData = {
        ...values,
        applicationDate: new Date().toISOString(),
        status: LoanApplicationStatus.SUBMITTED
      };

      await loanService.createLoanApplication(applicationData);
      message.success('Зээлийн хүсэлт амжилттай илгээгдлээ');
      setIsModalVisible(false);
      form.resetFields();
      setCurrentStep(0);
      loadApplications();
    } catch (error) {
      message.error('Зээлийн хүсэлт илгээхэд алдаа гарлаа');
    } finally {
      setLoading(false);
    }
  };

  const handleNext = () => {
    form.validateFields().then(() => {
      setCurrentStep(currentStep + 1);
    }).catch(() => {
      message.error('Талбаруудыг зөв бөглөнө үү');
    });
  };

  const handlePrev = () => {
    setCurrentStep(currentStep - 1);
  };

  const getStatusColor = (status: LoanApplicationStatus) => {
    const colors: Record<LoanApplicationStatus, string> = {
      [LoanApplicationStatus.DRAFT]: 'default',
      [LoanApplicationStatus.SUBMITTED]: 'orange',
      [LoanApplicationStatus.UNDER_REVIEW]: 'blue',
      [LoanApplicationStatus.APPROVED]: 'green',
      [LoanApplicationStatus.REJECTED]: 'red',
      [LoanApplicationStatus.DISBURSED]: 'purple',
      [LoanApplicationStatus.ADDITIONAL_INFO_REQUIRED]: 'yellow',
      [LoanApplicationStatus.CANCELLED]: 'gray',
      [LoanApplicationStatus.COMPLETED]: 'green'
    };
    return colors[status] || 'default';
  };

  const getStatusText = (status: LoanApplicationStatus) => {
    const texts: Record<LoanApplicationStatus, string> = {
      [LoanApplicationStatus.DRAFT]: 'Ноорог',
      [LoanApplicationStatus.SUBMITTED]: 'Илгээсэн',
      [LoanApplicationStatus.UNDER_REVIEW]: 'Шалгагдаж байна',
      [LoanApplicationStatus.APPROVED]: 'Зөвшөөрөгдсөн',
      [LoanApplicationStatus.REJECTED]: 'Татгалзсан',
      [LoanApplicationStatus.DISBURSED]: 'Олгогдсон',
      [LoanApplicationStatus.ADDITIONAL_INFO_REQUIRED]: 'Нэмэлт мэдээлэл шаардлагатай',
      [LoanApplicationStatus.CANCELLED]: 'Цуцалсан',
      [LoanApplicationStatus.COMPLETED]: 'Дууссан'
    };
    return texts[status] || status;
  };

  const columns = [
    {
      title: 'Хүсэлтийн дугаар',
      dataIndex: 'applicationNumber',
      key: 'applicationNumber',
      render: (text: string) => <Text strong>{text}</Text>
    },
    {
      title: 'Харилцагч',
      dataIndex: 'customerName',
      key: 'customerName'
    },
    {
      title: 'Зээлийн төрөл',
      dataIndex: 'loanType',
      key: 'loanType',
      render: (type: LoanType) => {
        const loanType = loanTypes.find(t => t.value === type);
        return loanType ? loanType.label : type;
      }
    },
    {
      title: 'Дүн',
      dataIndex: 'requestedAmount',
      key: 'requestedAmount',
      render: (amount: number) => `₮${amount?.toLocaleString()}`
    },
    {
      title: 'Хугацаа',
      dataIndex: 'requestedTermMonths',
      key: 'requestedTermMonths',
      render: (months: number) => `${months} сар`
    },
    {
      title: 'Статус',
      dataIndex: 'status',
      key: 'status',
      render: (status: LoanApplicationStatus) => (
        <Tag color={getStatusColor(status)}>
          {getStatusText(status)}
        </Tag>
      )
    },
    {
      title: 'Огноо',
      dataIndex: 'applicationDate',
      key: 'applicationDate',
      render: (date: string) => new Date(date).toLocaleDateString('mn-MN')
    },
    {
      title: 'Үйлдэл',
      key: 'actions',
      render: (_: any, record: LoanApplication) => (
        <Space>
          <Button
            type="text"
            icon={<EyeOutlined />}
            onClick={() => {
              setSelectedApplication(record);
              setViewDrawerVisible(true);
            }}
          >
            Харах
          </Button>
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => {
              setSelectedApplication(record);
              form.setFieldsValue(record);
              setIsModalVisible(true);
            }}
          >
            Засах
          </Button>
        </Space>
      )
    }
  ];

  const renderStepContent = () => {
    switch (currentStep) {
      case 0:
        return (
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="customerId"
                label="Харилцагч сонгох"
                rules={[{ required: true, message: 'Харилцагч сонгоно уу' }]}
              >
                <Select
                  placeholder="Харилцагч сонгоно уу"
                  showSearch
                  filterOption={(input, option) => {
                    const customer = customers.find(c => c.id === option?.value);
                    if (!customer) return false;
                    const searchText = `${customer.firstName} ${customer.lastName} ${customer.phone}`.toLowerCase();
                    return searchText.indexOf(input.toLowerCase()) >= 0;
                  }}
                >
                  {customers.map(customer => (
                    <Option key={customer.id} value={customer.id}>
                      {customer.firstName} {customer.lastName} - {customer.phone}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="applicationNumber"
                label="Хүсэлтийн дугаар"
                rules={[{ required: true, message: 'Хүсэлтийн дугаар оруулна уу' }]}
              >
                <Input placeholder="LN-2025-0001" />
              </Form.Item>
            </Col>
          </Row>
        );

      case 1:
        return (
          <>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="loanType"
                  label="Зээлийн төрөл"
                  rules={[{ required: true, message: 'Зээлийн төрөл сонгоно уу' }]}
                >
                  <Select placeholder="Зээлийн төрөл сонгоно уу">
                    {loanTypes.map(type => (
                      <Option key={type.value} value={type.value}>
                        {type.label}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="requestedAmount"
                  label="Хүссэн дүн (₮)"
                  rules={[{ required: true, message: 'Зээлийн дүн оруулна уу' }]}
                >
                  <InputNumber
                    style={{ width: '100%' }}
                    min={100000}
                    max={1000000000}
                    formatter={value => `₮ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                    parser={(value) => {
                      if (!value) return 100000;
                      const num = parseInt(value.replace(/₮\s?|(,*)/g, ''));
                      return num === 1000000000 ? 1000000000 : 100000;
                    }}
                    placeholder="₮ 1,000,000"
                  />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="requestedTermMonths"
                  label="Хугацаа (сар)"
                  rules={[{ required: true, message: 'Зээлийн хугацаа оруулна уу' }]}
                >
                  <InputNumber
                    style={{ width: '100%' }}
                    min={1}
                    max={360}
                    placeholder="12"
                  />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="interestRate"
                  label="Хүү (%)"
                >
                  <InputNumber
                    style={{ width: '100%' }}
                    min={0}
                    max={50}
                    step={0.1}
                    placeholder="12.5"
                  />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={24}>
                <Form.Item
                  name="purpose"
                  label="Зээлийн зориулалт"
                  rules={[{ required: true, message: 'Зээлийн зориулалтыг бичнэ үү' }]}
                >
                  <TextArea
                    rows={3}
                    placeholder="Зээлийн зориулалтыг тайлбарлана уу..."
                  />
                </Form.Item>
              </Col>
            </Row>
          </>
        );

      case 2:
        return (
          <div>
            <Alert
              message="Шаардлагатай баримт бичгүүд"
              description="Зээлийн хүсэлтийг боловсруулахад шаардлагатай баримт бичгүүдийг хавсаргана уу."
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
            />
            <Form.Item
              name="documents"
              label="Баримт бичиг"
            >
              <Upload.Dragger
                multiple
                action="/api/v1/documents/upload"
                onChange={(info) => {
                  if (info.file.status === 'done') {
                    message.success(`${info.file.name} файл амжилттай хуулагдлаа`);
                  } else if (info.file.status === 'error') {
                    message.error(`${info.file.name} файл хуулахад алдаа гарлаа`);
                  }
                }}
              >
                <p className="ant-upload-drag-icon">
                  <UploadOutlined />
                </p>
                <p className="ant-upload-text">Файл сонгохын тулд энд дарна уу эсвэл чирж оруулна уу</p>
                <p className="ant-upload-hint">
                  PDF, Word, Excel, Зураг файлууд дэмжигддэг. Файлийн хэмжээ 10MB-аас бага байх ёстой.
                </p>
              </Upload.Dragger>
            </Form.Item>
          </div>
        );

      case 3:
        return (
          <div>
            <Alert
              message="Хүсэлтийн мэдээллийг шалгана уу"
              description="Зээлийн хүсэлтийг илгээхийн өмнө бүх мэдээллийг шалгана уу."
              type="warning"
              showIcon
              style={{ marginBottom: 24 }}
            />
            
            <Card title="Хүсэлтийн хураангуй" style={{ marginBottom: 16 }}>
              <Row gutter={16}>
                <Col span={12}>
                  <p><strong>Зээлийн төрөл:</strong> {form.getFieldValue('loanType')}</p>
                  <p><strong>Хүссэн дүн:</strong> ₮{form.getFieldValue('requestedAmount')?.toLocaleString()}</p>
                </Col>
                <Col span={12}>
                  <p><strong>Хугацаа:</strong> {form.getFieldValue('requestedTermMonths')} сар</p>
                  <p><strong>Хүү:</strong> {form.getFieldValue('interestRate')}%</p>
                </Col>
              </Row>
              <Divider />
              <p><strong>Зориулалт:</strong> {form.getFieldValue('purpose')}</p>
            </Card>
          </div>
        );

      default:
        return null;
    }
  };

  if (loading && applications.length === 0) {
    return <LoadingSpinner tip="Зээлийн хүсэлтүүд ачааллаж байна..." />;
  }

  return (
    <div style={{ padding: '24px' }}>
      <Row justify="space-between" align="middle" style={{ marginBottom: 24 }}>
        <Col>
          <Title level={2}>
            <BankOutlined style={{ marginRight: 8, color: '#1890ff' }} />
            Зээлийн хүсэлт
          </Title>
        </Col>
        <Col>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setIsModalVisible(true)}
            size="large"
          >
            Шинэ хүсэлт
          </Button>
        </Col>
      </Row>

      <Card>
        <Table
          columns={columns}
          dataSource={applications}
          loading={loading}
          rowKey="id"
          pagination={{
            total: applications.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              `${range[0]}-${range[1]} / ${total} хүсэлт`
          }}
        />
      </Card>

      {/* Шинэ хүсэлт модал */}
      <Modal
        title="Шинэ зээлийн хүсэлт"
        open={isModalVisible}
        onCancel={() => {
          setIsModalVisible(false);
          setCurrentStep(0);
          form.resetFields();
        }}
        footer={null}
        width={800}
        destroyOnClose
      >
        <Steps current={currentStep} style={{ marginBottom: 24 }}>
          {steps.map(item => (
            <Step key={item.title} title={item.title} icon={item.icon} />
          ))}
        </Steps>

        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          {renderStepContent()}

          <div style={{ marginTop: 24, textAlign: 'right' }}>
            {currentStep > 0 && (
              <Button style={{ marginRight: 8 }} onClick={handlePrev}>
                Өмнөх
              </Button>
            )}
            {currentStep < steps.length - 1 && (
              <Button type="primary" onClick={handleNext}>
                Дараах
              </Button>
            )}
            {currentStep === steps.length - 1 && (
              <Button type="primary" htmlType="submit" loading={loading}>
                Хүсэлт илгээх
              </Button>
            )}
          </div>
        </Form>
      </Modal>

      {/* Хүсэлт харах Drawer */}
      <Drawer
        title="Зээлийн хүсэлтийн дэлгэрэнгүй"
        placement="right"
        width={600}
        open={viewDrawerVisible}
        onClose={() => setViewDrawerVisible(false)}
      >
        {selectedApplication && (
          <div>
            <Card title="Үндсэн мэдээлэл" style={{ marginBottom: 16 }}>
              <Row gutter={16}>
                <Col span={12}>
                  <p><strong>Хүсэлтийн дугаар:</strong></p>
                  <p>{selectedApplication.applicationNumber}</p>
                </Col>
                <Col span={12}>
                  <p><strong>Статус:</strong></p>
                  <Tag color={getStatusColor(selectedApplication?.status || LoanApplicationStatus.DRAFT)}>
                    {getStatusText(selectedApplication?.status || LoanApplicationStatus.DRAFT)}
                  </Tag>
                </Col>
              </Row>
            </Card>

            <Card title="Зээлийн мэдээлэл" style={{ marginBottom: 16 }}>
              <Row gutter={16}>
                <Col span={12}>
                  <p><strong>Төрөл:</strong> {selectedApplication.loanType}</p>
                  <p><strong>Дүн:</strong> ₮{selectedApplication.requestedAmount?.toLocaleString()}</p>
                </Col>
                <Col span={12}>
                  <p><strong>Хугацаа:</strong> {selectedApplication.requestedTermMonths} сар</p>
                  <p><strong>Хүү:</strong> {selectedApplication.interestRate}%</p>
                </Col>
              </Row>
              <Divider />
              <p><strong>Зориулалт:</strong></p>
              <p>{selectedApplication.purpose}</p>
            </Card>
          </div>
        )}
      </Drawer>
    </div>
  );
};

export default LoanApplicationPage;