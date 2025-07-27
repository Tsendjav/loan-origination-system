// frontend/src/types/loan.ts

/**
 * Зээлийн хүсэлттэй холбоотой TypeScript типүүд
 * Loan Application related TypeScript types
 */

export interface LoanApplication {
  id: string;
  customerId: string;
  loanProductId: string;
  applicationNumber: string;
  loanType: LoanType;
  
  // Loan Details
  requestedAmount: number;
  requestedTermMonths: number;
  purpose?: string;
  
  // Approved Details
  approvedAmount?: number;
  approvedTermMonths?: number;
  approvedRate?: number;
  monthlyPayment?: number;
  interestRate?: number;
  totalPayment?: number;
  description?: string;
  
  // Financial Information
  declaredIncome?: number;
  debtToIncomeRatio?: number;
  creditScore?: number;
  
  // Status and Workflow
  status: LoanStatus;
  currentStep?: string;
  assignedTo?: string;
  priority: Priority;
  
  // Decision Information
  decisionReason?: string;
  decisionDate?: string;
  approvedBy?: string;
  approvedDate?: string;
  rejectedBy?: string;
  rejectedDate?: string;
  
  // Processing dates
  submittedAt?: string;
  reviewedAt?: string;
  approvedAt?: string;
  rejectedAt?: string;
  disbursedAt?: string;
  
  // Review information
  reviewedBy?: string;
  rejectionReason?: string;
  reviewerNotes?: string;
  
  // Disbursement
  disbursedAmount?: number;
  disbursedDate?: string;
  disbursedBy?: string;
  
  // Risk Assessment
  riskScore?: number;
  riskFactors?: string;
  
  // Additional fields
  requiresCollateral: boolean;
  requiresGuarantor: boolean;
  expectedDisbursementDate?: string;
  processingFee?: number;
  otherCharges?: number;
  contractTerms?: string;
  specialConditions?: string;
  
  // Important Dates
  dueDate?: string;
  
  // Related data
  customer?: {
    id: string;
    firstName?: string;
    lastName?: string;
    companyName?: string;
    registerNumber: string;
    phone: string;
    email?: string;
  };
  
  loanProduct?: {
    id: string;
    name: string;
    loanType: string;
    minAmount: number;
    maxAmount: number;
    baseRate: number;
  };
  
  // Audit Fields
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  isDeleted: boolean;
  isActive: boolean;
}

export interface LoanProduct {
  id: string;
  name: string;
  productName?: string;
  loanType: LoanType;
  
  // Amount and Term limits
  minAmount: number;
  maxAmount: number;
  minTermMonths: number;
  maxTermMonths: number;
  
  // Interest Rates and Fees
  baseRate?: number;
  defaultInterestRate?: number;
  minInterestRate?: number;
  maxInterestRate?: number;
  processingFee?: number;
  processingFeeRate?: number;
  earlyPaymentPenalty?: number;
  earlyPaymentPenaltyRate?: number;
  latePaymentPenalty?: number;
  latePaymentPenaltyRate?: number;
  
  // Requirements
  minCreditScore?: number;
  minIncome?: number;
  maxDebtRatio?: number;
  requiresCollateral: boolean;
  requiresGuarantor: boolean;
  
  // Approval Settings
  approvalRequired: boolean;
  autoApprovalLimit?: number;
  
  // Display and Marketing
  displayOrder?: number;
  isFeatured: boolean;
  marketingMessage?: string;
  loanTypes?: string;
  
  // Content
  description?: string;
  requiredDocuments?: string;
  specialConditions?: string;
  termsAndConditions?: string;
  
  // Audit Fields
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  isDeleted: boolean;
  isActive: boolean;
}

export interface CreateLoanApplicationRequest {
  customerId: string;
  loanProductId: string;
  loanType: LoanType;
  requestedAmount: number;
  requestedTermMonths: number;
  purpose?: string;
  declaredIncome?: number;
  requiresCollateral?: boolean;
  requiresGuarantor?: boolean;
  description?: string;
}

export interface UpdateLoanApplicationRequest extends Partial<CreateLoanApplicationRequest> {
  id: string;
  status?: LoanStatus;
  currentStep?: string;
  assignedTo?: string;
  priority?: Priority;
  reviewerNotes?: string;
}

export interface LoanApprovalRequest {
  approvedAmount: number;
  approvedTermMonths: number;
  approvedRate: number;
  reason: string;
  specialConditions?: string;
  contractTerms?: string;
}

export interface LoanRejectionRequest {
  rejectionReason: string;
  reviewerNotes?: string;
}

export interface LoanDisbursementRequest {
  disbursedAmount: number;
  disbursementMethod: string;
  accountNumber: string;
  disbursementNotes?: string;
}

export interface LoanCalculationRequest {
  loanAmount: number;
  termMonths: number;
  interestRate: number;
  processingFee?: number;
  otherCharges?: number;
}

export interface LoanCalculationResult {
  monthlyPayment: number;
  totalPayment: number;
  totalInterest: number;
  effectiveRate: number;
  paymentSchedule: Array<{
    month: number;
    payment: number;
    principal: number;
    interest: number;
    balance: number;
  }>;
}

export interface LoanSearchFilters {
  search?: string;
  customerId?: string;
  loanType?: LoanType;
  status?: LoanStatus;
  assignedTo?: string;
  amountFrom?: number;
  amountTo?: number;
  dateFrom?: string;
  dateTo?: string;
  priority?: Priority;
  requiresCollateral?: boolean;
  requiresGuarantor?: boolean;
}

export interface LoanStatistics {
  totalApplications: number;
  pendingApplications: number;
  approvedApplications: number;
  rejectedApplications: number;
  disbursedApplications: number;
  totalDisbursedAmount: number;
  averageApprovalTime: number; // days
  averageDisbursementTime: number; // days
  approvalRate: number; // percentage
  applicationsByStatus: Record<LoanStatus, number>;
  applicationsByType: Record<LoanType, number>;
  applicationsByMonth: Array<{
    month: string;
    count: number;
    amount: number;
  }>;
  topLoanProducts: Array<{
    productId: string;
    productName: string;
    count: number;
    totalAmount: number;
  }>;
}

// Enums
export enum LoanType {
  PERSONAL = 'PERSONAL',
  BUSINESS = 'BUSINESS',
  MORTGAGE = 'MORTGAGE',
  CAR_LOAN = 'CAR_LOAN',
  CONSUMER = 'CONSUMER',
  EDUCATION = 'EDUCATION',
  MEDICAL = 'MEDICAL'
}

export enum LoanStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  PENDING = 'PENDING',
  PENDING_DOCUMENTS = 'PENDING_DOCUMENTS',
  UNDER_REVIEW = 'UNDER_REVIEW',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED',
  DISBURSED = 'DISBURSED'
}

export enum Priority {
  LOW = 1,
  NORMAL = 2,
  MEDIUM = 3,
  HIGH = 4,
  URGENT = 5
}

// Form related types
export interface LoanApplicationFormData {
  customerId: string;
  loanProductId: string;
  loanType: LoanType;
  requestedAmount: number;
  requestedTermMonths: number;
  purpose: string;
  declaredIncome: number;
  requiresCollateral: boolean;
  requiresGuarantor: boolean;
  description: string;
}

export interface LoanApplicationFormErrors {
  customerId?: string;
  loanProductId?: string;
  loanType?: string;
  requestedAmount?: string;
  requestedTermMonths?: string;
  purpose?: string;
  declaredIncome?: string;
}

// Component Props types
export interface LoanApplicationListProps {
  onApplicationSelect?: (application: LoanApplication) => void;
  onApplicationEdit?: (application: LoanApplication) => void;
  onApplicationDelete?: (applicationId: string) => void;
  filters?: LoanSearchFilters;
  showActions?: boolean;
  customerId?: string;
  pageSize?: number;
}

export interface LoanApplicationFormProps {
  application?: LoanApplication;
  customerId?: string;
  onSubmit: (data: CreateLoanApplicationRequest | UpdateLoanApplicationRequest) => void;
  onCancel: () => void;
  loading?: boolean;
  readonly?: boolean;
}

export interface LoanApplicationDetailProps {
  applicationId: string;
  onEdit?: () => void;
  onDelete?: () => void;
  onApprove?: (data: LoanApprovalRequest) => void;
  onReject?: (data: LoanRejectionRequest) => void;
  onDisburse?: (data: LoanDisbursementRequest) => void;
  showActions?: boolean;
}

export interface LoanCalculatorProps {
  onCalculate: (request: LoanCalculationRequest) => void;
  result?: LoanCalculationResult;
  loading?: boolean;
  loanProducts?: LoanProduct[];
}

// Constants
export const LOAN_TYPE_OPTIONS = [
  { value: LoanType.PERSONAL, label: 'Хувийн зээл' },
  { value: LoanType.BUSINESS, label: 'Бизнесийн зээл' },
  { value: LoanType.MORTGAGE, label: 'Ипотекийн зээл' },
  { value: LoanType.CAR_LOAN, label: 'Автомашины зээл' },
  { value: LoanType.CONSUMER, label: 'Хэрэглээний зээл' },
  { value: LoanType.EDUCATION, label: 'Боловсролын зээл' },
  { value: LoanType.MEDICAL, label: 'Эмнэлгийн зээл' }
];

export const LOAN_STATUS_OPTIONS = [
  { value: LoanStatus.DRAFT, label: 'Ноорог' },
  { value: LoanStatus.SUBMITTED, label: 'Илгээгдсэн' },
  { value: LoanStatus.PENDING, label: 'Хүлээгдэж буй' },
  { value: LoanStatus.PENDING_DOCUMENTS, label: 'Баримт хүлээгдэж буй' },
  { value: LoanStatus.UNDER_REVIEW, label: 'Хянагдаж буй' },
  { value: LoanStatus.APPROVED, label: 'Зөвшөөрөгдсөн' },
  { value: LoanStatus.REJECTED, label: 'Татгалзсан' },
  { value: LoanStatus.CANCELLED, label: 'Цуцлагдсан' },
  { value: LoanStatus.DISBURSED, label: 'Олгогдсон' }
];

export const PRIORITY_OPTIONS = [
  { value: Priority.LOW, label: 'Бага' },
  { value: Priority.NORMAL, label: 'Энгийн' },
  { value: Priority.MEDIUM, label: 'Дунд' },
  { value: Priority.HIGH, label: 'Өндөр' },
  { value: Priority.URGENT, label: 'Яаралтай' }
];

// Status colors for UI
export const LOAN_STATUS_COLORS: Record<LoanStatus, string> = {
  [LoanStatus.DRAFT]: '#d9d9d9',
  [LoanStatus.SUBMITTED]: '#1890ff',
  [LoanStatus.PENDING]: '#faad14',
  [LoanStatus.PENDING_DOCUMENTS]: '#fa8c16',
  [LoanStatus.UNDER_REVIEW]: '#722ed1',
  [LoanStatus.APPROVED]: '#52c41a',
  [LoanStatus.REJECTED]: '#f5222d',
  [LoanStatus.CANCELLED]: '#8c8c8c',
  [LoanStatus.DISBURSED]: '#13c2c2'
};

export const PRIORITY_COLORS: Record<Priority, string> = {
  [Priority.LOW]: '#52c41a',
  [Priority.NORMAL]: '#1890ff',
  [Priority.MEDIUM]: '#faad14',
  [Priority.HIGH]: '#fa8c16',
  [Priority.URGENT]: '#f5222d'
};

// Default values
export const DEFAULT_LOAN_FORM: Partial<LoanApplicationFormData> = {
  loanType: LoanType.PERSONAL,
  requiresCollateral: false,
  requiresGuarantor: false,
  requestedTermMonths: 12
};

// Validation rules
export const LOAN_AMOUNT_LIMITS = {
  min: 100000, // 100K MNT
  max: 1000000000 // 1B MNT
};

export const LOAN_TERM_LIMITS = {
  min: 6, // 6 months
  max: 360 // 30 years
};

// Payment frequency options
export enum PaymentFrequency {
  MONTHLY = 'MONTHLY',
  QUARTERLY = 'QUARTERLY',
  ANNUALLY = 'ANNUALLY'
}

export const PAYMENT_FREQUENCY_OPTIONS = [
  { value: PaymentFrequency.MONTHLY, label: 'Сар бүр' },
  { value: PaymentFrequency.QUARTERLY, label: 'Улирал бүр' },
  { value: PaymentFrequency.ANNUALLY, label: 'Жил бүр' }
];

// Workflow steps
export const LOAN_WORKFLOW_STEPS = [
  { key: 'draft', label: 'Ноорог', status: LoanStatus.DRAFT },
  { key: 'submitted', label: 'Илгээгдсэн', status: LoanStatus.SUBMITTED },
  { key: 'document_review', label: 'Баримт хянах', status: LoanStatus.PENDING_DOCUMENTS },
  { key: 'under_review', label: 'Хянагдаж буй', status: LoanStatus.UNDER_REVIEW },
  { key: 'decision', label: 'Шийдвэр', status: [LoanStatus.APPROVED, LoanStatus.REJECTED] },
  { key: 'disbursement', label: 'Олголт', status: LoanStatus.DISBURSED }
];

// Export/Import types
export interface LoanApplicationExportOptions {
  format: 'excel' | 'csv' | 'pdf';
  filters?: LoanSearchFilters;
  columns?: string[];
  includeCustomerInfo?: boolean;
  includeDocuments?: boolean;
  includePaymentSchedule?: boolean;
}

// API Response types
export interface LoanApplicationListResponse {
  content: LoanApplication[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface LoanProductListResponse {
  content: LoanProduct[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}