// frontend/src/types/customer.ts

/**
 * Харилцагчийн мэдээллийн TypeScript типүүд
 * Customer related TypeScript types
 */

export interface Customer {
  id: string;
  customerType: CustomerType;
  registerNumber: string;
  
  // Individual Customer fields
  firstName?: string;
  lastName?: string;
  middleName?: string;
  dateOfBirth?: string;
  gender?: Gender;
  maritalStatus?: MaritalStatus;
  
  // Business Customer fields
  companyName?: string;
  businessType?: string;
  establishmentDate?: string;
  taxNumber?: string;
  businessRegistrationNumber?: string;
  annualRevenue?: number;
  
  // Contact Information
  phone: string;
  email?: string;
  address?: string;
  city?: string;
  state?: string;
  province?: string;
  zipCode?: string;
  postalCode?: string;
  country?: string;
  
  // Employment/Business Information
  employerName?: string;
  jobTitle?: string;
  workPhone?: string;
  workAddress?: string;
  monthlyIncome?: number;
  employmentStartDate?: string;
  workExperienceYears?: number;
  
  // Banking Information
  bankName?: string;
  accountNumber?: string;
  
  // KYC Status
  kycStatus: KycStatus;
  kycCompletedAt?: string;
  kycVerifiedBy?: string;
  riskRating: RiskRating;
  
  // Internal Fields
  assignedTo?: string;
  notes?: string;
  
  // Audit Fields
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  isDeleted: boolean;
  isActive: boolean;
}

export interface CreateCustomerRequest {
  customerType: CustomerType;
  registerNumber: string;
  
  // Individual fields
  firstName?: string;
  lastName?: string;
  middleName?: string;
  dateOfBirth?: string;
  gender?: Gender;
  maritalStatus?: MaritalStatus;
  
  // Business fields
  companyName?: string;
  businessType?: string;
  establishmentDate?: string;
  taxNumber?: string;
  businessRegistrationNumber?: string;
  annualRevenue?: number;
  
  // Contact
  phone: string;
  email?: string;
  address?: string;
  city?: string;
  province?: string;
  postalCode?: string;
  
  // Employment
  employerName?: string;
  jobTitle?: string;
  monthlyIncome?: number;
  workExperienceYears?: number;
  
  // Banking
  bankName?: string;
  accountNumber?: string;
  
  notes?: string;
}

export interface UpdateCustomerRequest extends Partial<CreateCustomerRequest> {
  id: string;
}

export interface CustomerSearchFilters {
  search?: string;
  customerType?: CustomerType;
  kycStatus?: KycStatus;
  province?: string;
  city?: string;
  riskRating?: RiskRating;
  assignedTo?: string;
  isActive?: boolean;
  dateFrom?: string;
  dateTo?: string;
}

export interface CustomerStatistics {
  totalCustomers: number;
  individualCustomers: number;
  businessCustomers: number;
  activeCustomers: number;
  inactiveCustomers: number;
  kycCompleted: number;
  kycPending: number;
  newCustomersThisMonth: number;
  customersByProvince: Record<string, number>;
  customersByRiskRating: Record<RiskRating, number>;
  averageMonthlyIncome: number;
  topEmployers: Array<{
    name: string;
    count: number;
  }>;
}

// Enums
export enum CustomerType {
  INDIVIDUAL = 'INDIVIDUAL',
  BUSINESS = 'BUSINESS'
}

export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  OTHER = 'OTHER'
}

export enum MaritalStatus {
  SINGLE = 'SINGLE',
  MARRIED = 'MARRIED',
  DIVORCED = 'DIVORCED',
  WIDOWED = 'WIDOWED'
}

export enum KycStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  REJECTED = 'REJECTED',
  FAILED = 'FAILED'
}

export enum RiskRating {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH'
}

// Form related types
export interface CustomerFormData {
  // Basic Info
  customerType: CustomerType;
  registerNumber: string;
  
  // Individual
  firstName: string;
  lastName: string;
  middleName: string;
  dateOfBirth: string;
  gender: Gender;
  maritalStatus: MaritalStatus;
  
  // Business
  companyName: string;
  businessType: string;
  establishmentDate: string;
  taxNumber: string;
  businessRegistrationNumber: string;
  annualRevenue: number;
  
  // Contact
  phone: string;
  email: string;
  address: string;
  city: string;
  province: string;
  postalCode: string;
  
  // Employment
  employerName: string;
  jobTitle: string;
  monthlyIncome: number;
  workExperienceYears: number;
  
  // Banking
  bankName: string;
  accountNumber: string;
  
  notes: string;
}

export interface CustomerFormErrors {
  customerType?: string;
  registerNumber?: string;
  firstName?: string;
  lastName?: string;
  companyName?: string;
  phone?: string;
  email?: string;
  dateOfBirth?: string;
  establishmentDate?: string;
  monthlyIncome?: string;
  annualRevenue?: string;
  taxNumber?: string;
  businessRegistrationNumber?: string;
}

// Table column definitions
export interface CustomerTableColumn {
  key: string;
  title: string;
  dataIndex: string;
  width?: number;
  sorter?: boolean;
  filterable?: boolean;
  render?: (value: any, record: Customer) => React.ReactNode;
}

// API Response types
export interface CustomerListResponse {
  content: Customer[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface CustomerDetailResponse extends Customer {
  loanApplications?: Array<{
    id: string;
    applicationNumber: string;
    loanType: string;
    requestedAmount: number;
    status: string;
    createdAt: string;
  }>;
  documents?: Array<{
    id: string;
    fileName: string;
    documentType: string;
    verificationStatus: string;
    uploadedAt: string;
  }>;
}

// Component Props types
export interface CustomerListProps {
  onCustomerSelect?: (customer: Customer) => void;
  onCustomerEdit?: (customer: Customer) => void;
  onCustomerDelete?: (customerId: string) => void;
  filters?: CustomerSearchFilters;
  showActions?: boolean;
  selectable?: boolean;
  pageSize?: number;
}

export interface CustomerFormProps {
  customer?: Customer;
  onSubmit: (data: CreateCustomerRequest | UpdateCustomerRequest) => void;
  onCancel: () => void;
  loading?: boolean;
  readonly?: boolean;
}

export interface CustomerDetailProps {
  customerId: string;
  onEdit?: () => void;
  onDelete?: () => void;
  showActions?: boolean;
}

export interface CustomerSearchProps {
  onSearch: (filters: CustomerSearchFilters) => void;
  onReset: () => void;
  loading?: boolean;
  initialFilters?: CustomerSearchFilters;
}

// Validation types
export interface CustomerValidationRule {
  field: keyof CustomerFormData;
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  pattern?: RegExp;
  custom?: (value: any, formData: CustomerFormData) => string | null;
}

// Export/Import types
export interface CustomerExportOptions {
  format: 'excel' | 'csv' | 'pdf';
  filters?: CustomerSearchFilters;
  columns?: string[];
  includeDocuments?: boolean;
  includeLoanHistory?: boolean;
}

export interface CustomerImportResult {
  success: number;
  failed: number;
  errors: Array<{
    row: number;
    field: string;
    message: string;
  }>;
  duplicates: Array<{
    row: number;
    registerNumber: string;
    existingCustomerId: string;
  }>;
}

// Constants
export const CUSTOMER_TYPE_OPTIONS = [
  { value: CustomerType.INDIVIDUAL, label: 'Хувь хүн' },
  { value: CustomerType.BUSINESS, label: 'Байгууллага' }
];

export const GENDER_OPTIONS = [
  { value: Gender.MALE, label: 'Эрэгтэй' },
  { value: Gender.FEMALE, label: 'Эмэгтэй' },
  { value: Gender.OTHER, label: 'Бусад' }
];

export const MARITAL_STATUS_OPTIONS = [
  { value: MaritalStatus.SINGLE, label: 'Ганц бие' },
  { value: MaritalStatus.MARRIED, label: 'Гэрлэсэн' },
  { value: MaritalStatus.DIVORCED, label: 'Салсан' },
  { value: MaritalStatus.WIDOWED, label: 'Бэлэвсэн' }
];

export const KYC_STATUS_OPTIONS = [
  { value: KycStatus.PENDING, label: 'Хүлээгдэж буй' },
  { value: KycStatus.IN_PROGRESS, label: 'Явагдаж буй' },
  { value: KycStatus.COMPLETED, label: 'Дууссан' },
  { value: KycStatus.REJECTED, label: 'Татгалзсан' },
  { value: KycStatus.FAILED, label: 'Амжилтгүй' }
];

export const RISK_RATING_OPTIONS = [
  { value: RiskRating.LOW, label: 'Бага эрсдэлтэй' },
  { value: RiskRating.MEDIUM, label: 'Дунд эрсдэлтэй' },
  { value: RiskRating.HIGH, label: 'Өндөр эрсдэлтэй' }
];

export const PROVINCE_OPTIONS = [
  'Улаанбаатар',
  'Архангай',
  'Баян-Өлгий',
  'Баянхонгор',
  'Булган',
  'Говь-Алтай',
  'Говьсүмбэр',
  'Дархан-Уул',
  'Дорноговь',
  'Дорнод',
  'Дундговь',
  'Завхан',
  'Орхон',
  'Өвөрхангай',
  'Өмнөговь',
  'Сүхбаатар',
  'Сэлэнгэ',
  'Төв',
  'Увс',
  'Ховд',
  'Хөвсгөл',
  'Хэнтий'
];

// Default values
export const DEFAULT_CUSTOMER_FORM: Partial<CustomerFormData> = {
  customerType: CustomerType.INDIVIDUAL,
  gender: Gender.MALE,
  maritalStatus: MaritalStatus.SINGLE,
  province: 'Улаанбаатар',
  country: 'Mongolia'
};