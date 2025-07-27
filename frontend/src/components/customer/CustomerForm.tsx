// frontend/src/components/customer/CustomerForm.tsx

import React, { useState, useEffect } from 'react';
import {
  Form,
  Input,
  Select,
  DatePicker,
  InputNumber,
  Radio,
  Card,
  Row,
  Col,
  Button,
  Space,
  Divider,
  Typography,
  message,
  Spin,
  Switch,
  Alert
} from 'antd';
import {
  UserOutlined,
  BankOutlined,
  PhoneOutlined,
  MailOutlined,
  HomeOutlined,
  DollarOutlined,
  SafetyOutlined,
  SaveOutlined,
  CloseOutlined,
  InfoCircleOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';

import {
  Customer,
  CustomerFormData,
  CustomerFormErrors,
  CustomerFormProps,
  CreateCustomerRequest,
  UpdateCustomerRequest,
  CustomerType,
  Gender,
  MaritalStatus,
  KycStatus,
  RiskRating,
  CUSTOMER_TYPE_OPTIONS,
  GENDER_OPTIONS,
  MARITAL_STATUS_OPTIONS,
  KYC_STATUS_OPTIONS,
  RISK_RATING_OPTIONS,
  PROVINCE_OPTIONS,
  DEFAULT_CUSTOMER_FORM
} from '../../types/customer';

const { Option } = Select;
const { TextArea } = Input;
const { Title, Text } = Typography;

const CustomerForm: React.FC<CustomerFormProps> = ({
  customer,
  onSubmit,
  onCancel,
  loading = false,
  readonly = false
}) => {
  const [form] = Form.useForm<CustomerFormData>();
  const [customerType, setCustomerType] = useState<CustomerType>(CustomerType.INDIVIDUAL);
  const [formErrors, setFormErrors] = useState<CustomerFormErrors>({});
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  const isEditing = !!customer;

  // Initialize form
  useEffect(() => {
    if (customer) {
      const formData: Partial<CustomerFormData> = {
        customerType: customer.customerType,
        registerNumber: customer.registerNumber,
        firstName: customer.firstName || '',
        lastName: customer.lastName || '',
        middleName: customer.middleName || '',
        dateOfBirth: customer.dateOfBirth || '',
        gender: customer.gender || Gender.MALE,
        maritalStatus: customer.maritalStatus || MaritalStatus.SINGLE,
        companyName: customer.companyName || '',
        businessType: customer.businessType || '',
        establishmentDate: customer.establishmentDate || '',
        taxNumber: customer.taxNumber || '',
        businessRegistrationNumber: customer.businessRegistrationNumber || '',
        annualRevenue: customer.annualRevenue || 0,
        phone: customer.phone,
        email: customer.email || '',
        address: customer.address || '',
        city: customer.city || '',
        province: customer.province || '',
        postalCode: customer.postalCode || '',
        employerName: customer.employerName || '',
        jobTitle: customer.jobTitle || '',
        monthlyIncome: customer.monthlyIncome || 0,
        workExperienceYears: customer.workExperienceYears || 0,
        bankName: customer.bankName || '',
        accountNumber: customer.accountNumber || '',
        notes: customer.notes || ''
      };
      
      form.setFieldsValue(formData);
      setCustomerType(customer.customerType);
    } else {
      form.setFieldsValue(DEFAULT_CUSTOMER_FORM);
      setCustomerType(CustomerType.INDIVIDUAL);
    }
  }, [customer, form]);

  // Handle customer type change
  const handleCustomerTypeChange = (type: CustomerType) => {
    setCustomerType(type);
    setHasUnsavedChanges(true);
    
    // Clear type-specific fields when switching
    if (type === CustomerType.INDIVIDUAL) {
      form.setFieldsValue({
        companyName: '',
        businessType: '',
        establishmentDate: '',
        taxNumber: '',
        businessRegistrationNumber: '',
        annualRevenue: 0
      });
    } else {
      form.setFieldsValue({
        firstName: '',
        lastName: '',
        middleName: '',
        dateOfBirth: '',
        gender: undefined,
        maritalStatus: undefined
      });
    }
  };

  // Handle form submission
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      const submitData: CreateCustomerRequest | UpdateCustomerRequest = {
        ...(isEditing && { id: customer!.id }),
        customerType: values.customerType,
        registerNumber: values.registerNumber,
        ...(values.customerType === CustomerType.INDIVIDUAL ? {
          firstName: values.firstName,
          lastName: values.lastName,
          middleName: values.middleName,
          dateOfBirth: values.dateOfBirth,
          gender: values.gender,
          maritalStatus: values.maritalStatus
        } : {
          companyName: values.companyName,
          businessType: values.businessType,
          establishmentDate: values.establishmentDate,
          taxNumber: values.taxNumber,
          businessRegistrationNumber: values.businessRegistrationNumber,
          annualRevenue: values.annualRevenue
        }),
        phone: values.phone,
        email: values.email,
        address: values.address,
        city: values.city,
        province: values.province,
        postalCode: values.postalCode,
        employerName: values.employerName,
        jobTitle: values.jobTitle,
        monthlyIncome: values.monthlyIncome,
        workExperienceYears: values.workExperienceYears,
        bankName: values.bankName,
        accountNumber: values.accountNumber,
        notes: values.notes
      };

      onSubmit(submitData);
      setHasUnsavedChanges(false);
    } catch (error) {
      console.error('Form validation failed:', error);
      message.error('Форм бөглөхөд алдаа гарлаа. Бүх шаардлагатай талбарыг зөв бөгөлнө үү.');
    }
  };

  // Handle form values change
  const handleValuesChange = () => {
    setHasUnsavedChanges(true);
  };

  // Validation rules
  const validationRules = {
    registerNumber: [
      { required: true, message: 'Регистрийн дугаар шаардлагатай' },
      { 
        pattern: /^[А-ЯЁ]{2}\d{8}$/, 
        message: 'Регистрийн дугаар буруу байна (жнь: УБ12345678)' 
      }
    ],
    firstName: customerType === CustomerType.INDIVIDUAL ? [
      { required: true, message: 'Нэр шаардлагатай' },
      { min: 2, message: 'Нэр хамгийн багадаа 2 тэмдэгт байх ёстой' }
    ] : [],
    lastName: customerType === CustomerType.INDIVIDUAL ? [
      { required: true, message: 'Овог шаардлагатай' },
      { min: 2, message: 'Овог хамгийн багадаа 2 тэмдэгт байх ёстой' }
    ] : [],
    companyName: customerType === CustomerType.BUSINESS ? [
      { required: true, message: 'Байгууллагын нэр шаардлагатай' },
      { min: 3, message: 'Байгууллагын нэр хамгийн багадаа 3 тэмдэгт байх ёстой' }
    ] : [],
    phone: [
      { required: true, message: 'Утасны дугаар шаардлагатай' },
      { 
        pattern: /^\+976\d{8}$/, 
        message: 'Утасны дугаар буруу байна (жнь: +97612345678)' 
      }
    ],
    email: [
      { type: 'email' as const, message: 'И-мэйл хаяг буруу байна' }
    ],
    dateOfBirth: customerType === CustomerType.INDIVIDUAL ? [
      { required: true, message: 'Төрсөн огноо шаардлагатай' }
    ] : [],
    establishmentDate: customerType === CustomerType.BUSINESS ? [
      { required: true, message: 'Байгуулагдсан огноо шаардлагатай' }
    ] : [],
    taxNumber: customerType === CustomerType.BUSINESS ? [
      { 
        pattern: /^[А-ЯЁ]{2}\d{8}$/, 
        message: 'Татварын дугаар буруу байна' 
      }
    ] : []
  };

  return (
    <div className="customer-form">
      <Spin spinning={loading}>
        <Form
          form={form}
          layout="vertical"
          onValuesChange={handleValuesChange}
          disabled={readonly}
          size="large"
        >
          {/* Header */}
          <Card>
            <Row justify="space-between" align="middle">
              <Col>
                <Title level={4} style={{ margin: 0 }}>
                  {isEditing ? 'Харилцагчийн мэдээлэл засах' : 'Шинэ харилцагч бүртгэх'}
                </Title>
                {isEditing && customer && (
                  <Text type="secondary">
                    ID: {customer.id} | Үүсгэсэн: {dayjs(customer.createdAt).format('YYYY-MM-DD HH:mm')}
                  </Text>
                )}
              </Col>
              <Col>
                <Space>
                  <Button
                    icon={<CloseOutlined />}
                    onClick={onCancel}
                  >
                    Болих
                  </Button>
                  {!readonly && (
                    <Button
                      type="primary"
                      icon={<SaveOutlined />}
                      loading={loading}
                      onClick={handleSubmit}
                    >
                      {isEditing ? 'Хадгалах' : 'Бүртгэх'}
                    </Button>
                  )}
                </Space>
              </Col>
            </Row>

            {hasUnsavedChanges && !readonly && (
              <Alert
                message="Хадгалагдаагүй өөрчлөлт байна"
                type="warning"
                style={{ marginTop: 16 }}
                showIcon
              />
            )}
          </Card>

          {/* Basic Information */}
          <Card title={<><UserOutlined /> Үндсэн мэдээлэл</>} style={{ marginTop: 16 }}>
            <Row gutter={24}>
              <Col span={8}>
                <Form.Item
                  name="customerType"
                  label="Харилцагчийн төрөл"
                  rules={[{ required: true, message: 'Төрөл сонгох шаардлагатай' }]}
                >
                  <Radio.Group
                    options={CUSTOMER_TYPE_OPTIONS}
                    onChange={(e) => handleCustomerTypeChange(e.target.value)}
                    optionType="button"
                    buttonStyle="solid"
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="registerNumber"
                  label="Регистрийн дугаар"
                  rules={validationRules.registerNumber}
                >
                  <Input 
                    placeholder="УБ12345678"
                    maxLength={10}
                    style={{ textTransform: 'uppercase' }}
                  />
                </Form.Item>
              </Col>
            </Row>

            {/* Individual Customer Fields */}
            {customerType === CustomerType.INDIVIDUAL && (
              <>
                <Divider>Хувь хүний мэдээлэл</Divider>
                <Row gutter={24}>
                  <Col span={8}>
                    <Form.Item
                      name="lastName"
                      label="Овог"
                      rules={validationRules.lastName}
                    >
                      <Input placeholder="Болд" />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item
                      name="firstName"
                      label="Нэр"
                      rules={validationRules.firstName}
                    >
                      <Input placeholder="Бат" />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item
                      name="middleName"
                      label="Эцгийн нэр"
                    >
                      <Input placeholder="Дашийн" />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={24}>
                  <Col span={8}>
                    <Form.Item
                      name="dateOfBirth"
                      label="Төрсөн огноо"
                      rules={validationRules.dateOfBirth}
                    >
                      <DatePicker
                        style={{ width: '100%' }}
                        placeholder="Огноо сонгох"
                        disabledDate={(current) => current && current > dayjs().subtract(18, 'year')}
                      />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item
                      name="gender"
                      label="Хүйс"
                      rules={[{ required: true, message: 'Хүйс сонгох шаардлагатай' }]}
                    >
                      <Select placeholder="Хүйс сонгох">
                        {GENDER_OPTIONS.map(option => (
                          <Option key={option.value} value={option.value}>
                            {option.label}
                          </Option>
                        ))}
                      </Select>
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item
                      name="maritalStatus"
                      label="Гэрлэлтийн байдал"
                    >
                      <Select placeholder="Байдал сонгох">
                        {MARITAL_STATUS_OPTIONS.map(option => (
                          <Option key={option.value} value={option.value}>
                            {option.label}
                          </Option>
                        ))}
                      </Select>
                    </Form.Item>
                  </Col>
                </Row>
              </>
            )}

            {/* Business Customer Fields */}
            {customerType === CustomerType.BUSINESS && (
              <>
                <Divider>Байгууллагын мэдээлэл</Divider>
                <Row gutter={24}>
                  <Col span={12}>
                    <Form.Item
                      name="companyName"
                      label="Байгууллагын нэр"
                      rules={validationRules.companyName}
                    >
                      <Input placeholder="Монгол банк ХХК" />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      name="businessType"
                      label="Үйл ажиллагааны төрөл"
                    >
                      <Input placeholder="Банк санхүү" />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={24}>
                  <Col span={8}>
                    <Form.Item
                      name="establishmentDate"
                      label="Байгуулагдсан огноо"
                      rules={validationRules.establishmentDate}
                    >
                      <DatePicker
                        style={{ width: '100%' }}
                        placeholder="Огноо сонгох"
                        disabledDate={(current) => current && current > dayjs()}
                      />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item
                      name="taxNumber"
                      label="Татварын дугаар"
                      rules={validationRules.taxNumber}
                    >
                      <Input 
                        placeholder="TT12345678" 
                        maxLength={10}
                        style={{ textTransform: 'uppercase' }}
                      />
                    </Form.Item>
                  </Col>
                  <Col span={8}>
                    <Form.Item
                      name="businessRegistrationNumber"
                      label="Бизнес регистрийн дугаар"
                    >
                      <Input 
                        placeholder="BR12345678" 
                        maxLength={10}
                        style={{ textTransform: 'uppercase' }}
                      />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={24}>
                  <Col span={12}>
                    <Form.Item
                      name="annualRevenue"
                      label="Жилийн орлого (₮)"
                    >
                      <InputNumber
                        style={{ width: '100%' }}
                        placeholder="0"
                        min={0}
                        formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                        parser={(value) => value!.replace(/\$\s?|(,*)/g, '')}
                      />
                    </Form.Item>
                  </Col>
                </Row>
              </>
            )}
          </Card>

          {/* Contact Information */}
          <Card title={<><PhoneOutlined /> Холбоо барих мэдээлэл</>} style={{ marginTop: 16 }}>
            <Row gutter={24}>
              <Col span={8}>
                <Form.Item
                  name="phone"
                  label="Утасны дугаар"
                  rules={validationRules.phone}
                >
                  <Input 
                    prefix={<PhoneOutlined />}
                    placeholder="+97612345678" 
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="email"
                  label="И-мэйл хаяг"
                  rules={validationRules.email}
                >
                  <Input 
                    prefix={<MailOutlined />}
                    placeholder="example@email.com" 
                  />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={24}>
              <Col span={12}>
                <Form.Item
                  name="address"
                  label="Хаяг"
                >
                  <TextArea 
                    placeholder="Дэлгэрэнгүй хаяг бичнэ үү"
                    rows={2}
                  />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  name="province"
                  label="Аймаг/Хот"
                >
                  <Select placeholder="Байршил сонгох">
                    {PROVINCE_OPTIONS.map(province => (
                      <Option key={province} value={province}>
                        {province}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item
                  name="city"
                  label="Дүүрэг/Сум"
                >
                  <Input placeholder="Дүүрэг/Сум" />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={24}>
              <Col span={8}>
                <Form.Item
                  name="postalCode"
                  label="Шуудангийн код"
                >
                  <Input placeholder="14200" />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* Employment/Business Information */}
          <Card title={<><DollarOutlined /> Ажил мэргэжил/Орлогын мэдээлэл</>} style={{ marginTop: 16 }}>
            <Row gutter={24}>
              <Col span={8}>
                <Form.Item
                  name="employerName"
                  label="Ажил олгогчийн нэр"
                >
                  <Input placeholder="Монгол банк ХХК" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="jobTitle"
                  label="Албан тушаал"
                >
                  <Input placeholder="Ахлах мэргэжилтэн" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="workExperienceYears"
                  label="Ажлын туршлага (жил)"
                >
                  <InputNumber
                    style={{ width: '100%' }}
                    placeholder="0"
                    min={0}
                    max={50}
                  />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={24}>
              <Col span={12}>
                <Form.Item
                  name="monthlyIncome"
                  label="Сарын орлого (₮)"
                >
                  <InputNumber
                    style={{ width: '100%' }}
                    placeholder="0"
                    min={0}
                    formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                    parser={(value) => value!.replace(/\$\s?|(,*)/g, '')}
                  />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* Banking Information */}
          <Card title={<><BankOutlined /> Банкны мэдээлэл</>} style={{ marginTop: 16 }}>
            <Row gutter={24}>
              <Col span={12}>
                <Form.Item
                  name="bankName"
                  label="Банкны нэр"
                >
                  <Input placeholder="Хаан банк" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="accountNumber"
                  label="Дансны дугаар"
                >
                  <Input placeholder="1234567890" />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* Additional Information */}
          <Card title={<><InfoCircleOutlined /> Нэмэлт мэдээлэл</>} style={{ marginTop: 16 }}>
            <Row gutter={24}>
              <Col span={24}>
                <Form.Item
                  name="notes"
                  label="Тэмдэглэл"
                >
                  <TextArea 
                    placeholder="Нэмэлт тэмдэглэл..."
                    rows={3}
                    maxLength={1000}
                    showCount
                  />
                </Form.Item>
              </Col>
            </Row>
          </Card>
        </Form>
      </Spin>
    </div>
  );
};

export default CustomerForm;