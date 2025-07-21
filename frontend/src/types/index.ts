// ==========================================
// LOS Системийн үндсэн type-ууд
// ==========================================

// API Response типүүд
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  timestamp: string;
}

// Харилцагчийн типүүд
export interface Customer {
  id: string;
  customerType: 'INDIVIDUAL' | 'BUSINESS';
  firstName?: string;
  lastName?: string;
  registerNumber?: string;
  phone: string;
  email?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'BLOCKED';
  kycStatus: 'PENDING' | 'IN_PROGRESS' | 'APPROVED' | 'REJECTED';
  createdAt: string;
  updatedAt: string;
}

// Зээлийн хүсэлтийн типүүд
export interface LoanApplication {
  id: string;
  applicationNumber: string;
  customerId: string;
  customerName?: string;
  loanType: 'CONSUMER' | 'MORTGAGE' | 'BUSINESS' | 'AUTO' | 'MICROFINANCE';
  requestedAmount: number;
  requestedTermMonths: number;
  status: 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

// Утилити типүүд
export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
