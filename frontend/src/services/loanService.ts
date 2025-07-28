import apiClient, { API_ENDPOINTS, apiUtils, PaginatedResponse } from '../config/api';
import { Customer } from './customerService';

// Loan Application interfaces
export interface LoanApplication {
  id?: number;
  customerId: number;
  customer?: Customer;
  loanProductId: number;
  loanProduct?: LoanProduct;
  requestedAmount: number;
  approvedAmount?: number;
  loanTerm: number; // in months
  interestRate?: number;
  purpose: LoanPurpose;
  status: LoanStatus;
  applicationDate: string;
  submissionDate?: string;
  approvalDate?: string;
  rejectionDate?: string;
  disbursementDate?: string;
  documents: LoanDocument[];
  collateral: Collateral[];
  creditAssessment?: CreditAssessment;
  riskAssessment?: RiskAssessment;
  approvedBy?: number;
  rejectedBy?: number;
  rejectionReason?: string;
  notes?: string;
  monthlyPayment?: number;
  totalInterest?: number;
  totalAmount?: number;
  workflowStatus: WorkflowStatus;
  lastUpdated?: string;
}

export interface LoanProduct {
  id: number;
  name: string;
  description: string;
  productType: LoanType;
  minAmount: number;
  maxAmount: number;
  minTerm: number; // months
  maxTerm: number; // months
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

export interface LoanDocument {
  id?: number;
  documentType: DocumentType;
  fileName: string;
  fileSize: number;
  uploadDate: string;
  uploadedBy: number;
  isRequired: boolean;
  status: DocumentStatus;
  notes?: string;
}

export interface Collateral {
  id?: number;
  type: CollateralType;
  description: string;
  estimatedValue: number;
  appraisedValue?: number;
  appraisalDate?: string;
  condition: CollateralCondition;
  documents: string[];
}

export interface CreditAssessment {
  creditScore: number;
  creditHistory: string;
  paymentHistory: string;
  creditUtilization: number;
  totalDebt: number;
  debtToIncomeRatio: number;
  assessmentDate: string;
  assessedBy: number;
  riskRating: RiskRating;
  recommendations: string[];
}

export interface RiskAssessment {
  overallRisk: RiskLevel;
  creditRisk: RiskLevel;
  operationalRisk: RiskLevel;
  marketRisk: RiskLevel;
  riskFactors: RiskFactor[];
  mitigationStrategies: string[];
  assessmentDate: string;
  assessedBy: number;
  approvalRecommendation: ApprovalRecommendation;
}

export interface RiskFactor {
  category: string;
  description: string;
  impact: 'LOW' | 'MEDIUM' | 'HIGH';
  probability: 'LOW' | 'MEDIUM' | 'HIGH';
}

// Enums
export enum LoanStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  UNDER_REVIEW = 'UNDER_REVIEW',
  PENDING_DOCUMENTS = 'PENDING_DOCUMENTS',
  CREDIT_CHECK = 'CREDIT_CHECK',
  RISK_ASSESSMENT = 'RISK_ASSESSMENT',
  PENDING_APPROVAL = 'PENDING_APPROVAL',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  DISBURSED = 'DISBURSED',
  ACTIVE = 'ACTIVE',
  CLOSED = 'CLOSED',
  DEFAULTED = 'DEFAULTED',
}

export enum LoanType {
  PERSONAL = 'PERSONAL',
  MORTGAGE = 'MORTGAGE',
  AUTO = 'AUTO',
  BUSINESS = 'BUSINESS',
  EDUCATION = 'EDUCATION',
  REFINANCE = 'REFINANCE',
}

export enum LoanPurpose {
  HOME_PURCHASE = 'HOME_PURCHASE',
  HOME_IMPROVEMENT = 'HOME_IMPROVEMENT',
  DEBT_CONSOLIDATION = 'DEBT_CONSOLIDATION',
  EDUCATION = 'EDUCATION',
  MEDICAL = 'MEDICAL',
  BUSINESS_EXPANSION = 'BUSINESS_EXPANSION',
  VEHICLE_PURCHASE = 'VEHICLE_PURCHASE',
  OTHER = 'OTHER',
}

export enum DocumentType {
  IDENTITY_PROOF = 'IDENTITY_PROOF',
  INCOME_PROOF = 'INCOME_PROOF',
  ADDRESS_PROOF = 'ADDRESS_PROOF',
  BANK_STATEMENTS = 'BANK_STATEMENTS',
  TAX_RETURNS = 'TAX_RETURNS',
  EMPLOYMENT_LETTER = 'EMPLOYMENT_LETTER',
  PROPERTY_DOCUMENTS = 'PROPERTY_DOCUMENTS',
  COLLATERAL_DOCUMENTS = 'COLLATERAL_DOCUMENTS',
  OTHER = 'OTHER',
}

export enum DocumentStatus {
  PENDING = 'PENDING',
  UPLOADED = 'UPLOADED',
  VERIFIED = 'VERIFIED',
  REJECTED = 'REJECTED',
}

export enum CollateralType {
  REAL_ESTATE = 'REAL_ESTATE',
  VEHICLE = 'VEHICLE',
  JEWELRY = 'JEWELRY',
  SECURITIES = 'SECURITIES',
  EQUIPMENT = 'EQUIPMENT',
  OTHER = 'OTHER',
}

export enum CollateralCondition {
  EXCELLENT = 'EXCELLENT',
  GOOD = 'GOOD',
  FAIR = 'FAIR',
  POOR = 'POOR',
}

export enum RiskLevel {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  VERY_HIGH = 'VERY_HIGH',
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
  D = 'D',
}

export enum ApprovalRecommendation {
  APPROVE = 'APPROVE',
  APPROVE_WITH_CONDITIONS = 'APPROVE_WITH_CONDITIONS',
  REJECT = 'REJECT',
  REQUIRE_MORE_INFO = 'REQUIRE_MORE_INFO',
}

export enum WorkflowStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  WAITING_FOR_DOCUMENTS = 'WAITING_FOR_DOCUMENTS',
  WAITING_FOR_APPROVAL = 'WAITING_FOR_APPROVAL',
  COMPLETED = 'COMPLETED',
}

// Search and filter interfaces
export interface LoanSearchParams {
  customerId?: number;
  status?: LoanStatus;
  loanType?: LoanType;
  minAmount?: number;
  maxAmount?: number;
  applicationDateFrom?: string;
  applicationDateTo?: string;
  approvedBy?: number;
  riskLevel?: RiskLevel;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}

export interface LoanCalculationRequest {
  loanAmount: number;
  interestRate: number;
  loanTerm: number; // months
  paymentFrequency: 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
}

export interface LoanCalculationResponse {
  monthlyPayment: number;
  totalInterest: number;
  totalAmount: number;
  paymentSchedule: PaymentScheduleItem[];
}

export interface PaymentScheduleItem {
  paymentNumber: number;
  paymentDate: string;
  principalAmount: number;
  interestAmount: number;
  totalPayment: number;
  remainingBalance: number;
}

export interface LoanStats {
  totalApplications: number;
  pendingApplications: number;
  approvedApplications: number;
  rejectedApplications: number;
  totalDisbursedAmount: number;
  averageProcessingTime: number;
  approvalRate: number;
  applicationsByStatus: Record<LoanStatus, number>;
  applicationsByType: Record<LoanType, number>;
}

class LoanService {
  // Get loan applications with pagination and filtering
  async getLoanApplications(params: LoanSearchParams = {}): Promise<PaginatedResponse<LoanApplication>> {
    try {
      const queryParams = apiUtils.buildParams(params);
      const url = `${API_ENDPOINTS.LOANS.BASE}?${queryParams}`;
      
      const response = await apiClient.get(url);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get loan application by ID
  async getLoanApplicationById(id: number): Promise<LoanApplication> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.LOANS.BY_ID(id));
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Create new loan application
  async createLoanApplication(application: Omit<LoanApplication, 'id' | 'applicationDate' | 'lastUpdated'>): Promise<LoanApplication> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.LOANS.BASE, application);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Update loan application
  async updateLoanApplication(id: number, application: Partial<LoanApplication>): Promise<LoanApplication> {
    try {
      const response = await apiClient.put(API_ENDPOINTS.LOANS.BY_ID(id), application);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Submit loan application
  async submitLoanApplication(id: number): Promise<LoanApplication> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.LOANS.SUBMIT, { applicationId: id });
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Approve loan application
  async approveLoanApplication(id: number, approvalData: {
    approvedAmount: number;
    interestRate: number;
    loanTerm: number;
    conditions?: string[];
    notes?: string;
  }): Promise<LoanApplication> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.LOANS.APPROVE(id), approvalData);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Reject loan application
  async rejectLoanApplication(id: number, rejectionData: {
    reason: string;
    notes?: string;
  }): Promise<LoanApplication> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.LOANS.REJECT(id), rejectionData);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get loan applications by customer
  async getLoanApplicationsByCustomer(customerId: number): Promise<LoanApplication[]> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.LOANS.BY_CUSTOMER(customerId));
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get loan products
  async getLoanProducts(): Promise<LoanProduct[]> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.PRODUCTS.BASE);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get active loan products
  async getActiveLoanProducts(): Promise<LoanProduct[]> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.PRODUCTS.ACTIVE);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Calculate loan payment
  async calculateLoanPayment(request: LoanCalculationRequest): Promise<LoanCalculationResponse> {
    try {
      const response = await apiClient.post(`${API_ENDPOINTS.LOANS.BASE}/calculate`, request);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Upload loan document
  async uploadLoanDocument(applicationId: number, file: File, documentType: DocumentType): Promise<LoanDocument> {
    try {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('documentType', documentType);

      const response = await apiClient.post(
        `${API_ENDPOINTS.LOANS.BY_ID(applicationId)}/documents`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get loan statistics
  async getLoanStats(): Promise<LoanStats> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.LOANS.BASE}/stats`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Update loan status
  async updateLoanStatus(id: number, status: LoanStatus, notes?: string): Promise<LoanApplication> {
    try {
      const response = await apiClient.patch(API_ENDPOINTS.LOANS.STATUS(id), { status, notes });
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Get payment schedule
  async getPaymentSchedule(applicationId: number): Promise<PaymentScheduleItem[]> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.LOANS.BY_ID(applicationId)}/payment-schedule`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }

  // Export loan applications
  async exportLoanApplications(params: LoanSearchParams = {}): Promise<Blob> {
    try {
      const queryParams = apiUtils.buildParams(params);
      const url = `${API_ENDPOINTS.LOANS.BASE}/export?${queryParams}`;
      
      const response = await apiClient.get(url, {
        responseType: 'blob',
      });
      
      return response.data;
    } catch (error: any) {
      apiUtils.handleError(error);
    }
  }
}

// Export singleton instance
export const loanService = new LoanService();
export default loanService;

// Utility functions
export const loanUtils = {
  // Format loan amount
  formatAmount: (amount: number): string => {
    return new Intl.NumberFormat('mn-MN', {
      style: 'currency',
      currency: 'MNT',
    }).format(amount);
  },

  // Format interest rate
  formatInterestRate: (rate: number): string => {
    return `${rate.toFixed(2)}%`;
  },

  // Get status color
  getStatusColor: (status: LoanStatus): string => {
    const colors = {
      [LoanStatus.DRAFT]: 'gray',
      [LoanStatus.SUBMITTED]: 'blue',
      [LoanStatus.UNDER_REVIEW]: 'yellow',
      [LoanStatus.PENDING_DOCUMENTS]: 'orange',
      [LoanStatus.CREDIT_CHECK]: 'blue',
      [LoanStatus.RISK_ASSESSMENT]: 'purple',
      [LoanStatus.PENDING_APPROVAL]: 'yellow',
      [LoanStatus.APPROVED]: 'green',
      [LoanStatus.REJECTED]: 'red',
      [LoanStatus.DISBURSED]: 'green',
      [LoanStatus.ACTIVE]: 'green',
      [LoanStatus.CLOSED]: 'gray',
      [LoanStatus.DEFAULTED]: 'red',
    };
    return colors[status] || 'gray';
  },

  // Get risk level color
  getRiskLevelColor: (riskLevel: RiskLevel): string => {
    const colors = {
      [RiskLevel.LOW]: 'green',
      [RiskLevel.MEDIUM]: 'yellow',
      [RiskLevel.HIGH]: 'orange',
      [RiskLevel.VERY_HIGH]: 'red',
    };
    return colors[riskLevel] || 'gray';
  },

  // Calculate monthly payment
  calculateMonthlyPayment: (principal: number, annualRate: number, termInMonths: number): number => {
    const monthlyRate = annualRate / 100 / 12;
    if (monthlyRate === 0) return principal / termInMonths;
    
    const monthlyPayment = principal * (monthlyRate * Math.pow(1 + monthlyRate, termInMonths)) /
      (Math.pow(1 + monthlyRate, termInMonths) - 1);
    
    return Math.round(monthlyPayment * 100) / 100;
  },

  // Get loan type display name
  getLoanTypeDisplayName: (type: LoanType): string => {
    const names = {
      [LoanType.PERSONAL]: 'Хувийн зээл',
      [LoanType.MORTGAGE]: 'Орон сууцны зээл',
      [LoanType.AUTO]: 'Машины зээл',
      [LoanType.BUSINESS]: 'Бизнесийн зээл',
      [LoanType.EDUCATION]: 'Боловсролын зээл',
      [LoanType.REFINANCE]: 'Дахин санхүүжүүлэх зээл',
    };
    return names[type] || type;
  },

  // Get status display name
  getStatusDisplayName: (status: LoanStatus): string => {
    const names = {
      [LoanStatus.DRAFT]: 'Ноорог',
      [LoanStatus.SUBMITTED]: 'Илгээсэн',
      [LoanStatus.UNDER_REVIEW]: 'Хянаж байгаа',
      [LoanStatus.PENDING_DOCUMENTS]: 'Баримт бичиг хүлээж байгаа',
      [LoanStatus.CREDIT_CHECK]: 'Зээлийн түүх шалгаж байгаа',
      [LoanStatus.RISK_ASSESSMENT]: 'Эрсдэлийн үнэлгээ',
      [LoanStatus.PENDING_APPROVAL]: 'Зөвшөөрөл хүлээж байгаа',
      [LoanStatus.APPROVED]: 'Зөвшөөрсөн',
      [LoanStatus.REJECTED]: 'Татгалзсан',
      [LoanStatus.DISBURSED]: 'Олгосон',
      [LoanStatus.ACTIVE]: 'Идэвхтэй',
      [LoanStatus.CLOSED]: 'Хаасан',
      [LoanStatus.DEFAULTED]: 'Төлбөрийн чадваргүй',
    };
    return names[status] || status;
  },
};