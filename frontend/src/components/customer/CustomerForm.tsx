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
  Alert,
  Switch, // Added Switch for boolean options
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
  InfoCircleOutlined,
  CheckCircleOutlined, // For KYC Status
  WarningOutlined, // For Risk Level
  CommentOutlined, // For Communication Preferences
} from '@ant-design/icons';
import dayjs from 'dayjs';

// Define all necessary types and options directly within the component or import from a types file
// For simplicity, I'm defining them here. In a real app, these would be in a separate `types/customer.ts`
export enum CustomerType {
  INDIVIDUAL = 'INDIVIDUAL',
  BUSINESS = 'BUSINESS',
  VIP = 'VIP',
}

export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  OTHER = 'OTHER',
}

export enum MaritalStatus {
  SINGLE = 'SINGLE',
  MARRIED = 'MARRIED',
  DIVORCED = 'DIVORCED',
  WIDOWED = 'WIDOWED',
}

export enum KycStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  REJECTED = 'REJECTED',
}

export enum RiskRating {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
}

export enum CustomerStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  PENDING_VERIFICATION = 'PENDING_VERIFICATION',
  SUSPENDED = 'SUSPENDED',
}

export enum EmploymentType {
  FULL_TIME = 'FULL_TIME',
  PART_TIME = 'PART_TIME',
  CONTRACT = 'CONTRACT',
  SELF_EMPLOYED = 'SELF_EMPLOYED',
  UNEMPLOYED = 'UNEMPLOYED',
}

export interface Address {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  addressType: string;
}

export interface EmploymentInfo {
  employerName: string;
  jobTitle: string;
  employmentType: EmploymentType;
  monthlyIncome: number;
  employmentStartDate: string;
  workPhone: string;
}

export interface CommunicationPreferences {
  emailNotifications: boolean;
  smsNotifications: boolean;
  phoneNotifications: boolean;
  marketingConsent: boolean;
  preferredContactTime: string;
}

export interface Customer {
  id?: string;
  firstName?: string;
  lastName?: string;
  middleName?: string;
  registerNumber: string;
  dateOfBirth?: string;
  gender?: Gender;
  maritalStatus?: MaritalStatus;
  customerType: CustomerType;
  companyName?: string;
  businessType?: string;
  establishmentDate?: string;
  taxNumber?: string;
  businessRegistrationNumber?: string;
  annualRevenue?: number;
  phone: string;
  email?: string;
  address?: Address; // Changed from string to Address object
  city?: string; // Kept for simplicity if address is string, but ideally part of Address object
  province?: string; // Kept for simplicity if address is string, but ideally part of Address object
  postalCode?: string; // Kept for simplicity if address is string, but ideally part of Address object
  employerName?: string; // Moved to EmploymentInfo
  jobTitle?: string; // Moved to EmploymentInfo
  monthlyIncome?: number; // Moved to EmploymentInfo
  workExperienceYears?: number; // Added this field
  bankName?: string;
  accountNumber?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
  kycStatus?: KycStatus; // Added from customer_form.tsx
  riskLevel?: RiskRating; // Added from customer_form.tsx
  status?: CustomerStatus; // Added from customer_form.tsx
  preferredLanguage?: string; // Added from customer_form.tsx
  socialSecurityNumber?: string; // Added from customer_form.tsx
  employmentInfo?: EmploymentInfo; // Added from customer_form.tsx
  communicationPreferences?: CommunicationPreferences; // Added from customer_form.tsx
}

export interface CustomerFormData {
  firstName?: string;
  lastName?: string;
  middleName?: string;
  registerNumber: string;
  dateOfBirth?: string;
  gender?: Gender;
  maritalStatus?: MaritalStatus;
  customerType: CustomerType;
  companyName?: string;
  businessType?: string;
  establishmentDate?: string;
  taxNumber?: string;
  businessRegistrationNumber?: string;
  annualRevenue?: number;
  phone: string;
  email?: string;
  address?: string; // Kept as string for simplicity with Ant Design's single input
  city?: string;
  province?: string;
  postalCode?: string;
  employerName?: string;
  jobTitle?: string;
  monthlyIncome?: number;
  workExperienceYears?: number;
  bankName?: string;
  accountNumber?: string;
  notes?: string;
  kycStatus?: KycStatus;
  riskLevel?: RiskRating;
  status?: CustomerStatus;
  preferredLanguage?: string;
  socialSecurityNumber?: string;
  employmentInfo_employerName?: string; // Flattened for Ant Design Form.Item
  employmentInfo_jobTitle?: string;
  employmentInfo_employmentType?: EmploymentType;
  employmentInfo_monthlyIncome?: number;
  employmentInfo_employmentStartDate?: string;
  employmentInfo_workPhone?: string;
  communicationPreferences_emailNotifications?: boolean;
  communicationPreferences_smsNotifications?: boolean;
  communicationPreferences_phoneNotifications?: boolean;
  communicationPreferences_marketingConsent?: boolean;
  communicationPreferences_preferredContactTime?: string;
}

export interface CustomerFormErrors {
  [key: string]: string;
}

export interface CustomerFormProps {
  customer?: Customer;
  onSubmit: (customer: Partial<Customer>) => Promise<void>;
  onCancel: () => void;
  loading?: boolean;
  readonly?: boolean;
  mode?: 'create' | 'edit' | 'view'; // Added mode prop
}

// Options for Select and Radio components
export const CUSTOMER_TYPE_OPTIONS = [
  { label: 'Хувь хүн', value: CustomerType.INDIVIDUAL },
  { label: 'Байгууллага', value: CustomerType.BUSINESS },
  { label: 'VIP Харилцагч', value: CustomerType.VIP },
];

export const GENDER_OPTIONS = [
  { label: 'Эрэгтэй', value: Gender.MALE },
  { label: 'Эмэгтэй', value: Gender.FEMALE },
  { label: 'Бусад', value: Gender.OTHER },
];

export const MARITAL_STATUS_OPTIONS = [
  { label: 'Ганц бие', value: MaritalStatus.SINGLE },
  { label: 'Гэрлэсэн', value: MaritalStatus.MARRIED },
  { label: 'Салсан', value: MaritalStatus.DIVORCED },
  { label: 'Бэлэвсэн', value: MaritalStatus.WIDOWED },
];

export const KYC_STATUS_OPTIONS = [
  { label: 'Эхлээгүй', value: KycStatus.NOT_STARTED },
  { label: 'Үргэлжилж байна', value: KycStatus.IN_PROGRESS },
  { label: 'Бүрэн', value: KycStatus.COMPLETED },
  { label: 'Татгалзсан', value: KycStatus.REJECTED },
];

export const RISK_RATING_OPTIONS = [
  { label: 'Бага', value: RiskRating.LOW },
  { label: 'Дунд', value: RiskRating.MEDIUM },
  { label: 'Өндөр', value: RiskRating.HIGH },
];

export const CUSTOMER_STATUS_OPTIONS = [
  { label: 'Идэвхтэй', value: CustomerStatus.ACTIVE },
  { label: 'Идэвхгүй', value: CustomerStatus.INACTIVE },
  { label: 'Баталгаажуулалт хүлээгдэж байна', value: CustomerStatus.PENDING_VERIFICATION },
  { label: 'Түдгэлзүүлсэн', value: CustomerStatus.SUSPENDED },
];

export const EMPLOYMENT_TYPE_OPTIONS = [
  { label: 'Бүтэн цагийн', value: EmploymentType.FULL_TIME },
  { label: 'Хагас цагийн', value: EmploymentType.PART_TIME },
  { label: 'Гэрээт', value: EmploymentType.CONTRACT },
  { label: 'Хувиараа хөдөлмөрлөгч', value: EmploymentType.SELF_EMPLOYED },
  { label: 'Ажилгүй', value: EmploymentType.UNEMPLOYED },
];

export const PROVINCE_OPTIONS = [
  'Улаанбаатар', 'Архангай', 'Баян-Өлгий', 'Баянхонгор', 'Булган', 'Говь-Алтай', 'Говьсүмбэр',
  'Дархан-Уул', 'Дорноговь', 'Дорнод', 'Дундговь', 'Завхан', 'Орхон', 'Өвөрхангай', 'Өмнөговь',
  'Сүхбаатар', 'Сэлэнгэ', 'Төв', 'Увс', 'Ховд', 'Хөвсгөл', 'Хэнтий',
];

export const DEFAULT_CUSTOMER_FORM: CustomerFormData = {
  customerType: CustomerType.INDIVIDUAL,
  registerNumber: '',
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  dateOfBirth: '',
  socialSecurityNumber: '',
  gender: undefined,
  maritalStatus: undefined,
  companyName: '',
  businessType: '',
  establishmentDate: '',
  taxNumber: '',
  businessRegistrationNumber: '',
  annualRevenue: 0,
  address: '',
  city: '',
  province: '',
  postalCode: '',
  employerName: '',
  jobTitle: '',
  monthlyIncome: 0,
  workExperienceYears: 0,
  bankName: '',
  accountNumber: '',
  notes: '',
  kycStatus: KycStatus.NOT_STARTED,
  riskLevel: RiskRating.LOW,
  status: CustomerStatus.PENDING_VERIFICATION,
  preferredLanguage: 'mn',
  employmentInfo_employerName: '',
  employmentInfo_jobTitle: '',
  employmentInfo_employmentType: EmploymentType.FULL_TIME,
  employmentInfo_monthlyIncome: 0,
  employmentInfo_employmentStartDate: '',
  employmentInfo_workPhone: '',
  communicationPreferences_emailNotifications: true,
  communicationPreferences_smsNotifications: true,
  communicationPreferences_phoneNotifications: false,
  communicationPreferences_marketingConsent: false,
  communicationPreferences_preferredContactTime: 'ANYTIME',
};


const { Option } = Select;
const { TextArea } = Input;
const { Title, Text } = Typography;

const CustomerForm: React.FC<CustomerFormProps> = ({
  customer,
  onSubmit,
  onCancel,
  loading = false,
  readonly = false,
  mode = 'create', // Default mode
}) => {
  const [form] = Form.useForm<CustomerFormData>();
  const [customerType, setCustomerType] = useState<CustomerType>(CustomerType.INDIVIDUAL);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  const isEditing = !!customer && mode !== 'create';
  const isReadOnly = mode === 'view' || readonly;

  // Initialize form
  useEffect(() => {
    if (customer) {
      const formData: Partial<CustomerFormData> = {
        customerType: customer.customerType,
        registerNumber: customer.registerNumber,
        firstName: customer.firstName || '',
        lastName: customer.lastName || '',
        middleName: customer.middleName || '',
        dateOfBirth: customer.dateOfBirth ? dayjs(customer.dateOfBirth).format('YYYY-MM-DD') : '', // Format date for DatePicker
        gender: customer.gender || Gender.MALE,
        maritalStatus: customer.maritalStatus || MaritalStatus.SINGLE,
        companyName: customer.companyName || '',
        businessType: customer.businessType || '',
        establishmentDate: customer.establishmentDate ? dayjs(customer.establishmentDate).format('YYYY-MM-DD') : '', // Format date for DatePicker
        taxNumber: customer.taxNumber || '',
        businessRegistrationNumber: customer.businessRegistrationNumber || '',
        annualRevenue: customer.annualRevenue || 0,
        phone: customer.phone,
        email: customer.email || '',
        // Flatten address fields if they are objects in customer
        address: customer.address?.street || '',
        city: customer.address?.city || '',
        province: customer.address?.state || '', // Using state from address as province
        postalCode: customer.address?.zipCode || '',
        
        // Flatten employment info
        employmentInfo_employerName: customer.employmentInfo?.employerName || '',
        employmentInfo_jobTitle: customer.employmentInfo?.jobTitle || '',
        employmentInfo_employmentType: customer.employmentInfo?.employmentType || EmploymentType.FULL_TIME,
        employmentInfo_monthlyIncome: customer.employmentInfo?.monthlyIncome || 0,
        employmentInfo_employmentStartDate: customer.employmentInfo?.employmentStartDate ? dayjs(customer.employmentInfo.employmentStartDate).format('YYYY-MM-DD') : '',
        employmentInfo_workPhone: customer.employmentInfo?.workPhone || '',

        // Flatten communication preferences
        communicationPreferences_emailNotifications: customer.communicationPreferences?.emailNotifications ?? true,
        communicationPreferences_smsNotifications: customer.communicationPreferences?.smsNotifications ?? true,
        communicationPreferences_phoneNotifications: customer.communicationPreferences?.phoneNotifications ?? false,
        communicationPreferences_marketingConsent: customer.communicationPreferences?.marketingConsent ?? false,
        communicationPreferences_preferredContactTime: customer.communicationPreferences?.preferredContactTime || 'ANYTIME',

        workExperienceYears: customer.workExperienceYears || 0,
        bankName: customer.bankName || '',
        accountNumber: customer.accountNumber || '',
        notes: customer.notes || '',
        kycStatus: customer.kycStatus || KycStatus.NOT_STARTED,
        riskLevel: customer.riskLevel || RiskRating.LOW,
        status: customer.status || CustomerStatus.PENDING_VERIFICATION,
        preferredLanguage: customer.preferredLanguage || 'mn',
        socialSecurityNumber: customer.socialSecurityNumber || '',
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
        establishmentDate: undefined,
        taxNumber: '',
        businessRegistrationNumber: '',
        annualRevenue: 0
      });
    } else {
      form.setFieldsValue({
        firstName: '',
        lastName: '',
        middleName: '',
        dateOfBirth: undefined,
        gender: undefined,
        maritalStatus: undefined
      });
    }
  };

  // Handle form submission
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      const submitData: Partial<Customer> = {
        ...(isEditing && customer?.id && { id: customer.id }),
        customerType: values.customerType,
        registerNumber: values.registerNumber,
        socialSecurityNumber: values.socialSecurityNumber, // Added socialSecurityNumber

        ...(values.customerType === CustomerType.INDIVIDUAL ? {
          firstName: values.firstName,
          lastName: values.lastName,
          middleName: values.middleName,
          dateOfBirth: values.dateOfBirth ? dayjs(values.dateOfBirth).format('YYYY-MM-DD') : undefined,
          gender: values.gender,
          maritalStatus: values.maritalStatus
        } : {
          companyName: values.companyName,
          businessType: values.businessType,
          establishmentDate: values.establishmentDate ? dayjs(values.establishmentDate).format('YYYY-MM-DD') : undefined,
          taxNumber: values.taxNumber,
          businessRegistrationNumber: values.businessRegistrationNumber,
          annualRevenue: values.annualRevenue
        }),
        phone: values.phone,
        email: values.email,
        address: { // Reconstruct address object
          street: values.address || '',
          city: values.city || '',
          state: values.province || '', // Using province as state
          zipCode: values.postalCode || '',
          country: customer?.address?.country || 'Mongolia', // Keep existing country or default
          addressType: customer?.address?.addressType || 'PRIMARY', // Keep existing type or default
        },
        // Reconstruct employmentInfo object
        employmentInfo: {
          employerName: values.employmentInfo_employerName || '',
          jobTitle: values.employmentInfo_jobTitle || '',
          employmentType: values.employmentInfo_employmentType || EmploymentType.FULL_TIME,
          monthlyIncome: values.employmentInfo_monthlyIncome || 0,
          employmentStartDate: values.employmentInfo_employmentStartDate ? dayjs(values.employmentInfo_employmentStartDate).format('YYYY-MM-DD') : '',
          workPhone: values.employmentInfo_workPhone || '',
        },
        // Reconstruct communicationPreferences object
        communicationPreferences: {
          emailNotifications: values.communicationPreferences_emailNotifications ?? true,
          smsNotifications: values.communicationPreferences_smsNotifications ?? true,
          phoneNotifications: values.communicationPreferences_phoneNotifications ?? false,
          marketingConsent: values.communicationPreferences_marketingConsent ?? false,
          preferredContactTime: values.communicationPreferences_preferredContactTime || 'ANYTIME',
        },
        workExperienceYears: values.workExperienceYears,
        bankName: values.bankName,
        accountNumber: values.accountNumber,
        notes: values.notes,
        kycStatus: values.kycStatus,
        riskLevel: values.riskLevel,
        status: values.status,
        preferredLanguage: values.preferredLanguage,
      };

      await onSubmit(submitData);
      setHasUnsavedChanges(false);
      message.success(isEditing ? 'Харилцагчийн мэдээлэл амжилттай шинэчлэгдлээ!' : 'Шинэ харилцагч амжилттай бүртгэгдлээ!');
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
    socialSecurityNumber: [ // Added validation for socialSecurityNumber
      { required: true, message: 'Регистрийн дугаар заавал бөглөх ёстой' },
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
        pattern: /^\+976\d{8}$|^\d{8}$/, // Allow 8 digits or +976 prefix
        message: 'Утасны дугаар буруу байна (жнь: +97612345678 эсвэл 99112233)' 
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
    ] : [],
    employmentInfo_employerName: [ // Added validation for employment info
      { required: true, message: 'Ажлын газрын нэр заавал бөглөх ёстой' }
    ],
    employmentInfo_jobTitle: [
      { required: true, message: 'Албан тушаал заавал бөглөх ёстой' }
    ],
    employmentInfo_monthlyIncome: [
      { required: true, message: 'Сарын орлого заавал бөглөх ёстой' },
      { type: 'number' as const, min: 0, message: 'Сарын орлого 0-ээс их байх ёстой' }
    ],
    employmentInfo_employmentStartDate: [
      { required: true, message: 'Ажилд орсон огноо заавал бөглөх ёстой' }
    ],
    address_street: [
      { required: true, message: 'Гудамж, байр заавал бөглөх ёстой' }
    ],
    address_city: [
      { required: true, message: 'Хот/Дүүрэг заавал бөглөх ёстой' }
    ],
    address_zipCode: [
      { required: true, message: 'Шуудангийн код заавал бөглөх ёстой' }
    ],
  };

  return (
    <div className="customer-form">
      <Spin spinning={loading}>
        <Form
          form={form}
          layout="vertical"
          onValuesChange={handleValuesChange}
          disabled={isReadOnly}
          size="large"
          initialValues={DEFAULT_CUSTOMER_FORM}
        >
          {/* Header */}
          <Card>
            <Row justify="space-between" align="middle">
              <Col>
                <Title level={4} style={{ margin: 0 }}>
                  {mode === 'create' ? 'Шинэ харилцагч бүртгэх' : 
                   mode === 'edit' ? 'Харилцагчийн мэдээлэл засах' : 
                   'Харилцагчийн мэдээлэл'}
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
                    {isReadOnly ? 'Хаах' : 'Болих'}
                  </Button>
                  {!isReadOnly && (
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

            {hasUnsavedChanges && !isReadOnly && (
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
              <Col span={8}>
                <Form.Item
                  name="socialSecurityNumber"
                  label="Регистрийн дугаар (НД)" // Added social security number
                  rules={validationRules.socialSecurityNumber}
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
                        format="YYYY-MM-DD"
                        value={form.getFieldValue('dateOfBirth') ? dayjs(form.getFieldValue('dateOfBirth')) : null}
                        onChange={(date, dateString) => form.setFieldsValue({ dateOfBirth: dateString })}
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
                        format="YYYY-MM-DD"
                        value={form.getFieldValue('establishmentDate') ? dayjs(form.getFieldValue('establishmentDate')) : null}
                        onChange={(date, dateString) => form.setFieldsValue({ establishmentDate: dateString })}
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
              <Col span={8}>
                <Form.Item
                  name="preferredLanguage"
                  label="Хэл сонголт"
                >
                  <Select placeholder="Хэл сонгох">
                    <Option value="mn">Монгол</Option>
                    <Option value="en">English</Option>
                  </Select>
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={24}>
              <Col span={12}>
                <Form.Item
                  name="address"
                  label="Гудамж, байр"
                  rules={validationRules.address_street}
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
                  rules={validationRules.address_city}
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
                  rules={validationRules.address_zipCode}
                >
                  <Input placeholder="14200" />
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* Employment Information */}
          <Card title={<><DollarOutlined /> Ажил мэргэжил/Орлогын мэдээлэл</>} style={{ marginTop: 16 }}>
            <Row gutter={24}>
              <Col span={8}>
                <Form.Item
                  name="employmentInfo_employerName"
                  label="Ажлын газрын нэр"
                  rules={validationRules.employmentInfo_employerName}
                >
                  <Input placeholder="Монгол банк ХХК" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="employmentInfo_jobTitle"
                  label="Албан тушаал"
                  rules={validationRules.employmentInfo_jobTitle}
                >
                  <Input placeholder="Ахлах мэргэжилтэн" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="employmentInfo_employmentType"
                  label="Ажлын төрөл"
                >
                  <Select placeholder="Төрөл сонгох">
                    {EMPLOYMENT_TYPE_OPTIONS.map(option => (
                      <Option key={option.value} value={option.value}>
                        {option.label}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={24}>
              <Col span={8}>
                <Form.Item
                  name="employmentInfo_monthlyIncome"
                  label="Сарын орлого (₮)"
                  rules={validationRules.employmentInfo_monthlyIncome}
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
              <Col span={8}>
                <Form.Item
                  name="employmentInfo_employmentStartDate"
                  label="Ажилд орсон огноо"
                  rules={validationRules.employmentInfo_employmentStartDate}
                >
                  <DatePicker
                    style={{ width: '100%' }}
                    placeholder="Огноо сонгох"
                    format="YYYY-MM-DD"
                    value={form.getFieldValue('employmentInfo_employmentStartDate') ? dayjs(form.getFieldValue('employmentInfo_employmentStartDate')) : null}
                    onChange={(date, dateString) => form.setFieldsValue({ employmentInfo_employmentStartDate: dateString })}
                    disabledDate={(current) => current && current > dayjs()}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="employmentInfo_workPhone"
                  label="Ажлын утас"
                >
                  <Input placeholder="77777777" />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={24}>
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

          {/* KYC, Risk, Status */}
          <Card title={<><SafetyOutlined /> Статус ба Үнэлгээ</>} style={{ marginTop: 16 }}>
            <Row gutter={24}>
              <Col span={8}>
                <Form.Item
                  name="kycStatus"
                  label={<><CheckCircleOutlined /> KYC Статус</>}
                  rules={[{ required: true, message: 'KYC статус сонгох шаардлагатай' }]}
                >
                  <Select placeholder="KYC статус сонгох">
                    {KYC_STATUS_OPTIONS.map(option => (
                      <Option key={option.value} value={option.value}>
                        {option.label}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="riskLevel"
                  label={<><WarningOutlined /> Эрсдлийн түвшин</>}
                  rules={[{ required: true, message: 'Эрсдлийн түвшин сонгох шаардлагатай' }]}
                >
                  <Select placeholder="Эрсдлийн түвшин сонгох">
                    {RISK_RATING_OPTIONS.map(option => (
                      <Option key={option.value} value={option.value}>
                        {option.label}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="status"
                  label={<><InfoCircleOutlined /> Харилцагчийн статус</>}
                  rules={[{ required: true, message: 'Статус сонгох шаардлагатай' }]}
                >
                  <Select placeholder="Харилцагчийн статус сонгох">
                    {CUSTOMER_STATUS_OPTIONS.map(option => (
                      <Option key={option.value} value={option.value}>
                        {option.label}
                      </Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            </Row>
          </Card>

          {/* Communication Preferences */}
          <Card title={<><CommentOutlined /> Харилцааны тохиргоо</>} style={{ marginTop: 16 }}>
            <Row gutter={24}>
              <Col span={8}>
                <Form.Item
                  name="communicationPreferences_emailNotifications"
                  label="И-мэйл мэдэгдэл"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="communicationPreferences_smsNotifications"
                  label="SMS мэдэгдэл"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="communicationPreferences_phoneNotifications"
                  label="Утсаар мэдэгдэл"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={24}>
              <Col span={8}>
                <Form.Item
                  name="communicationPreferences_marketingConsent"
                  label="Маркетингийн зөвшөөрөл"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  name="communicationPreferences_preferredContactTime"
                  label="Холбоо барих цаг"
                >
                  <Input placeholder="Ямар ч цагт" />
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
