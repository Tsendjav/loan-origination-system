// loanService.ts - FIXED VERSION
import { apiClient } from './apiClient';

// Import types from our fixed type definitions
import {
  LoanApplication,
  LoanApplicationStatus,
  LoanType,
  Document,
  DocumentType,
  PaginatedResponse
} from '../types';

// Fixed API endpoints - create proper structure  
const API_ENDPOINTS = {
  LOANS: {
    BASE: '/loan-applications',
    BY_ID: (id: string) => `/loan-applications/${id}`,
    BY_CUSTOMER: (customerId: string) => `/loan-applications/customer/${customerId}`,
    SUBMIT: '/loan-applications/submit',
    APPROVE: (id: string) => `/loan-applications/${id}/approve`,
    REJECT: (id: string) => `/loan-applications/${id}/reject`,
    STATUS: (id: string) => `/loan-applications/${id}/status`
  },
  PRODUCTS: {
    BASE: '/loan-products',
    ACTIVE: '/loan-products/active'
  }
};

// API utilities - create proper implementation
const apiUtils = {
  buildParams: (params: Record<string, any>): string => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        searchParams.set(key, value.toString());
      }
    });
    return searchParams.toString();
  },
  
  handleResponse: <T>(response: any): T => {
    // Handle different response formats
    if (response?.data) {
      return response.data;
    }
    return response;
  },
  
  handleError: (error: any): never => {
    console.error('API Error:', error);
    throw error;
  }
};

// Loan Application interfaces
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
  customerId?: string;
  status?: LoanApplicationStatus;
  loanType?: LoanType;
  minAmount?: number;
  maxAmount?: number;
  applicationDateFrom?: string;
  applicationDateTo?: string;
  approvedBy?: string;
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
  applicationsByStatus: Record<LoanApplicationStatus, number>;
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
      return apiUtils.handleError(error);
    }
  }

  // Get loan application by ID
  async getLoanApplicationById(id: string): Promise<LoanApplication> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.LOANS.BY_ID(id));
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Create new loan application
  async createLoanApplication(application: Omit<LoanApplication, 'id' | 'applicationDate' | 'updatedAt'>): Promise<LoanApplication> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.LOANS.BASE, application);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Update loan application
  async updateLoanApplication(id: string, application: Partial<LoanApplication>): Promise<LoanApplication> {
    try {
      const response = await apiClient.put(API_ENDPOINTS.LOANS.BY_ID(id), application);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Submit loan application
  async submitLoanApplication(id: string): Promise<LoanApplication> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.LOANS.SUBMIT, { applicationId: id });
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Approve loan application
  async approveLoanApplication(id: string, approvalData: {
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
      return apiUtils.handleError(error);
    }
  }

  // Reject loan application
  async rejectLoanApplication(id: string, rejectionData: {
    reason: string;
    notes?: string;
  }): Promise<LoanApplication> {
    try {
      const response = await apiClient.post(API_ENDPOINTS.LOANS.REJECT(id), rejectionData);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Get loan applications by customer
  async getLoanApplicationsByCustomer(customerId: string): Promise<LoanApplication[]> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.LOANS.BY_CUSTOMER(customerId));
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Get loan products
  async getLoanProducts(): Promise<LoanProduct[]> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.PRODUCTS.BASE);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Get active loan products
  async getActiveLoanProducts(): Promise<LoanProduct[]> {
    try {
      const response = await apiClient.get(API_ENDPOINTS.PRODUCTS.ACTIVE);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Calculate loan payment
  async calculateLoanPayment(request: LoanCalculationRequest): Promise<LoanCalculationResponse> {
    try {
      const response = await apiClient.post(`${API_ENDPOINTS.LOANS.BASE}/calculate`, request);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Upload loan document
  async uploadLoanDocument(applicationId: string, file: File, documentType: DocumentType): Promise<Document> {
    try {
      const response = await apiClient.uploadFile(
        `${API_ENDPOINTS.LOANS.BY_ID(applicationId)}/documents`,
        file,
        {
          documentType: documentType.toString()
        }
      );
      
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Get loan statistics
  async getLoanStats(): Promise<LoanStats> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.LOANS.BASE}/stats`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Update loan status
  async updateLoanStatus(id: string, status: LoanApplicationStatus, notes?: string): Promise<LoanApplication> {
    try {
      const response = await apiClient.patch(API_ENDPOINTS.LOANS.STATUS(id), { status, notes });
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Get payment schedule
  async getPaymentSchedule(applicationId: string): Promise<PaymentScheduleItem[]> {
    try {
      const response = await apiClient.get(`${API_ENDPOINTS.LOANS.BY_ID(applicationId)}/payment-schedule`);
      return apiUtils.handleResponse(response);
    } catch (error: any) {
      return apiUtils.handleError(error);
    }
  }

  // Export loan applications
  async exportLoanApplications(params: LoanSearchParams = {}): Promise<Blob> {
    try {
      const queryParams = apiUtils.buildParams(params);
      const url = `${API_ENDPOINTS.LOANS.BASE}/export?${queryParams}`;
      const response = await apiClient.get(url);
      return new Blob([response.data]);
    } catch (error: any) {
      return apiUtils.handleError(error);
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
  getStatusColor: (status: LoanApplicationStatus): string => {
    const colors = {
      [LoanApplicationStatus.DRAFT]: 'gray',
      [LoanApplicationStatus.SUBMITTED]: 'blue',
      [LoanApplicationStatus.UNDER_REVIEW]: 'yellow',
      [LoanApplicationStatus.ADDITIONAL_INFO_REQUIRED]: 'orange',
      [LoanApplicationStatus.APPROVED]: 'green',
      [LoanApplicationStatus.REJECTED]: 'red',
      [LoanApplicationStatus.DISBURSED]: 'green',
      [LoanApplicationStatus.COMPLETED]: 'gray',
      [LoanApplicationStatus.CANCELLED]: 'gray',
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
      [LoanType.CONSUMER]: 'Хэрэглээний зээл',
      [LoanType.MICROFINANCE]: 'Бичил санхүүжилт',
      [LoanType.STUDENT]: 'Оюутны зээл',
      [LoanType.CREDIT_CARD]: 'Кредит карт',
    };
    return names[type] || type;
  },

  // Get status display name
  getStatusDisplayName: (status: LoanApplicationStatus): string => {
    const names = {
      [LoanApplicationStatus.DRAFT]: 'Ноорог',
      [LoanApplicationStatus.SUBMITTED]: 'Илгээсэн',
      [LoanApplicationStatus.UNDER_REVIEW]: 'Хянаж байгаа',
      [LoanApplicationStatus.ADDITIONAL_INFO_REQUIRED]: 'Нэмэлт мэдээлэл шаардлагатай',
      [LoanApplicationStatus.APPROVED]: 'Зөвшөөрсөн',
      [LoanApplicationStatus.REJECTED]: 'Татгалзсан',
      [LoanApplicationStatus.CANCELLED]: 'Цуцалсан',
      [LoanApplicationStatus.DISBURSED]: 'Олгосон',
      [LoanApplicationStatus.COMPLETED]: 'Дууссан',
    };
    return names[status] || status;
  },
};