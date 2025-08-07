// types/index.ts - ЦЕНТРАЛИЗОВАННЫЕ ТИПЫ ДЛЯ LOS СИСТЕМЫ

// ====================== AUTH TYPES ======================
export interface LoginCredentials {
  username: string;
  password: string;
}

export interface User {
  id: string;
  username: string;
  role: UserRole;
  name?: string;
  email?: string;
  fullName?: string;
  firstName?: string;
  lastName?: string;
  roles?: UserRole[];
}

export interface LoginResponse {
  success: boolean;
  token?: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
  user?: User;
  message?: string;
  error?: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  loading: boolean;
  error: string | null;
}

export interface ValidationTestResponse {
  valid: boolean;
  success: boolean;
  error?: string;
  message?: string;
  field?: string;
  username?: string;
  isEmail?: boolean;
}

export enum UserRole {
  SUPER_ADMIN = 'SUPER_ADMIN',
  ADMIN = 'ADMIN',
  MANAGER = 'MANAGER',
  LOAN_OFFICER = 'LOAN_OFFICER',
  DOCUMENT_REVIEWER = 'DOCUMENT_REVIEWER',
  CUSTOMER_SERVICE = 'CUSTOMER_SERVICE',
  AUDITOR = 'AUDITOR',
  USER = 'USER'
}

// ====================== CUSTOMER TYPES ======================
export enum CustomerType {
  INDIVIDUAL = 'INDIVIDUAL',
  BUSINESS = 'BUSINESS',
  VIP = 'VIP'
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

export enum CustomerStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  BLOCKED = 'BLOCKED',
  PENDING_VERIFICATION = 'PENDING_VERIFICATION'
}

export enum KycStatus {
  NOT_STARTED = 'NOT_STARTED',
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED'
}

export enum RiskRating {
  AAA = 'AAA',
  AA = 'AA',
  A = 'A',
  BBB = 'BBB',
  BB = 'BB',
  B = 'B',
  CCC = 'CCC',
  CC = 'CC',
  C = 'C',
  D = 'D'
}

export enum RiskLevel {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  VERY_HIGH = 'VERY_HIGH',
  CRITICAL = 'CRITICAL'
}

export enum EmploymentType {
  FULL_TIME = 'FULL_TIME',
  PART_TIME = 'PART_TIME',
  CONTRACT = 'CONTRACT',
  SELF_EMPLOYED = 'SELF_EMPLOYED',
  UNEMPLOYED = 'UNEMPLOYED'
}

export interface Address {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  postalCode?: string;
  country?: string;
  addressType?: string;
}

export interface EmploymentInfo {
  employerName: string;
  jobTitle: string;
  employmentType: EmploymentType;
  monthlyIncome: number;
  employmentStartDate: string;
  workPhone?: string;
}

export interface CommunicationPreferences {
  emailNotifications: boolean;
  smsNotifications: boolean;
  phoneNotifications: boolean;
  marketingConsent: boolean;
  preferredContactTime: string;
}

export interface Customer {
  id: string;
  customerType: CustomerType;
  registerNumber: string;
  socialSecurityNumber?: string;
  
  // Individual customer fields
  firstName?: string;
  lastName?: string;
  middleName?: string;
  dateOfBirth?: string;
  gender?: Gender;
  maritalStatus?: MaritalStatus;
  
  // Business customer fields
  companyName?: string;
  businessType?: string;
  establishmentDate?: string;
  taxNumber?: string;
  businessRegistrationNumber?: string;
  annualRevenue?: number;
  
  // Contact information
  phone: string;
  email?: string;
  address?: Address;
  
  // Employment information
  employmentInfo?: EmploymentInfo;
  workExperienceYears?: number;
  
  // Banking information
  bankName?: string;
  accountNumber?: string;
  
  // Communication preferences
  communicationPreferences?: CommunicationPreferences;
  
  // Status and assessment
  status: CustomerStatus;
  kycStatus: KycStatus;
  riskLevel: RiskLevel; // Changed from RiskRating to RiskLevel
  
  // Metadata
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  isActive: boolean;
  notes?: string;
  preferredLanguage?: string;
  
  // Location fields for compatibility
  province?: string;
  city?: string;
}

// ====================== LOAN TYPES ======================
export enum LoanType {
  PERSONAL = 'PERSONAL',
  MORTGAGE = 'MORTGAGE',
  AUTO = 'AUTO',
  BUSINESS = 'BUSINESS',
  EDUCATION = 'EDUCATION',
  CONSUMER = 'CONSUMER',
  MICROFINANCE = 'MICROFINANCE',
  STUDENT = 'STUDENT',
  CREDIT_CARD = 'CREDIT_CARD'
}

export enum LoanApplicationStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  UNDER_REVIEW = 'UNDER_REVIEW',
  ADDITIONAL_INFO_REQUIRED = 'ADDITIONAL_INFO_REQUIRED',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED',
  DISBURSED = 'DISBURSED',
  COMPLETED = 'COMPLETED'
}

export interface LoanApplication {
  id: string;
  applicationNumber?: string;
  customerId: string;
  customer?: Customer;
  
  // Loan details
  loanType?: LoanType;
  loanProduct?: string;
  loanProductId?: number;
  requestedAmount: number;
  approvedAmount?: number;
  requestedTermMonths: number;
  approvedTermMonths?: number;
  interestRate?: number;
  purpose: string;
  
  // Status and dates
  status?: LoanApplicationStatus;
  applicationDate: string;
  submittedDate?: string;
  reviewedDate?: string;
  approvedDate?: string;
  disbursedDate?: string;
  completedDate?: string;
  
  // Review information
  reviewedBy?: string;
  approvedBy?: string;
  rejectedBy?: string;
  rejectionReason?: string;
  
  // Documents and collateral
  documents?: Document[];
  collateral?: Collateral[];
  
  // Risk assessment
  creditScore?: number;
  riskAssessment?: any;
  
  // Metadata
  createdAt: string;
  updatedAt: string;
  notes?: string;
}

// ====================== DOCUMENT TYPES ======================
export enum DocumentType {
  IDENTITY_PROOF = 'IDENTITY_PROOF',
  INCOME_PROOF = 'INCOME_PROOF',
  PASSPORT = 'PASSPORT',
  BANK_STATEMENT = 'BANK_STATEMENT',
  TAX_RETURN = 'TAX_RETURN',
  EMPLOYMENT_LETTER = 'EMPLOYMENT_LETTER',
  BUSINESS_REGISTRATION = 'BUSINESS_REGISTRATION',
  COLLATERAL_DOCUMENT = 'COLLATERAL_DOCUMENT',
  OTHER = 'OTHER'
}

export enum DocumentStatus {
  UPLOADED = 'UPLOADED',
  VERIFIED = 'VERIFIED',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED',
  PENDING = 'PENDING' // Added for completeness
}

export interface Document {
  id: string;
  filename: string;
  originalFilename: string;
  fileSize: number;
  mimeType: string;
  documentType: DocumentType;
  status: DocumentStatus;
  uploadedAt: string;
  uploadedBy: string;
  verifiedAt?: string;
  verifiedBy?: string;
  rejectedAt?: string;
  rejectedBy?: string;
  rejectionReason?: string;
  expiryDate?: string;
  notes?: string;
  customerId?: string;
  loanApplicationId?: string;
}

// ====================== COLLATERAL TYPES ======================
export enum CollateralType {
  REAL_ESTATE = 'REAL_ESTATE',
  VEHICLE = 'VEHICLE',
  JEWELRY = 'JEWELRY',
  SECURITIES = 'SECURITIES',
  EQUIPMENT = 'EQUIPMENT',
  CASH = 'CASH',
  OTHER = 'OTHER'
}

export enum CollateralCondition {
  NEW = 'NEW',
  EXCELLENT = 'EXCELLENT',
  GOOD = 'GOOD',
  FAIR = 'FAIR',
  POOR = 'POOR'
}

export interface Collateral {
  id?: string;
  type: CollateralType;
  description: string;
  estimatedValue: number;
  condition: CollateralCondition;
  location?: string;
  ownershipProof?: string;
  valuationDate?: string;
  valuationBy?: string;
  insuranceDetails?: string;
  documents: Document[];
  notes?: string;
}

// ====================== GENERAL TYPES ======================
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ValidationError {
  field: string;
  message: string;
  code?: string;
}

export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
  errors?: ValidationError[];
}

// ====================== ENHANCED TYPES ======================
export interface LoanProduct {
  id: number;
  name: string;
  description: string;
  productType: LoanType;
  minAmount: number;
  maxAmount: number;
  minTerm: number;
  maxTerm: number;
  baseInterestRate: number;
  isActive: boolean;
  eligibilityCriteria: EligibilityCriteria;
  requiredDocuments: string[];
  processingFee: number;
  earlyRepaymentPenalty: number;
}

export interface EligibilityCriteria {
  minAge: number;
  maxAge: number;
  minIncome: number;
  minCreditScore: number;
  maxDebtToIncomeRatio: number;
  requiredEmploymentType: string[];
  citizenshipRequired: boolean;
}

// ====================== RE-EXPORTS FOR COMPATIBILITY ======================
export type CompatCustomerStatus = CustomerType;
export type CompatRiskRating = RiskLevel;

// Default export
export default {
  CustomerType,
  Gender,
  MaritalStatus,
  CustomerStatus,
  KycStatus,
  RiskRating,
  RiskLevel,
  EmploymentType,
  LoanType,
  LoanApplicationStatus,
  DocumentType,
  DocumentStatus,
  CollateralType,
  CollateralCondition,
  UserRole
};