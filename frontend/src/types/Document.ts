// frontend/src/types/document.ts

/**
 * Баримт бичигтэй холбоотой TypeScript типүүд
 * Document related TypeScript types
 */

export interface Document {
  id: string;
  customerId: string;
  loanApplicationId?: string;
  documentTypeId: string;
  
  // Document Information
  fileName: string;
  originalFilename: string;
  storedFilename: string;
  filePath: string;
  contentType: string;
  fileSize: number;
  checksum?: string;
  
  // Document Metadata
  description?: string;
  tags?: string;
  versionNumber: number;
  previousDocumentId?: string;
  
  // Verification
  verificationStatus: VerificationStatus;
  verifiedBy?: string;
  verifiedAt?: string;
  verificationNotes?: string;
  status: string;
  
  // Expiry
  expiryDate?: string;
  
  // Requirements
  isRequired: boolean;
  
  // Processing
  processingStatus?: string;
  processingError?: string;
  ocrText?: string;
  extractedData?: string;
  aiConfidenceScore?: number;
  
  // Upload Information
  uploadedAt: string;
  uploadedBy?: string;
  
  // Related data
  documentType?: DocumentType;
  customer?: {
    id: string;
    firstName?: string;
    lastName?: string;
    companyName?: string;
    registerNumber: string;
  };
  loanApplication?: {
    id: string;
    applicationNumber: string;
    loanType: string;
    status: string;
  };
  
  // Audit Fields
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  isDeleted: boolean;
  isActive: boolean;
}

export interface DocumentType {
  id: string;
  name: string;
  description?: string;
  isRequired: boolean;
  isActive: boolean;
  category?: string;
  allowedFileTypes?: string[];
  maxFileSize?: number;
  validityPeriod?: number; // in days
  templateUrl?: string;
  instructions?: string;
  
  // Audit Fields
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  isDeleted: boolean;
}

export interface DocumentUploadRequest {
  file: File;
  customerId: string;
  documentTypeId: string;
  loanApplicationId?: string;
  description?: string;
  tags?: string;
}

export interface DocumentUpdateRequest {
  id: string;
  description?: string;
  tags?: string;
  expiryDate?: string;
}

export interface DocumentVerificationRequest {
  verificationStatus: VerificationStatus;
  verificationNotes?: string;
}

export interface DocumentSearchFilters {
  customerId?: string;
  loanApplicationId?: string;
  documentTypeId?: string;
  verificationStatus?: VerificationStatus;
  fileName?: string;
  uploadedBy?: string;
  dateFrom?: string;
  dateTo?: string;
  expiryDateFrom?: string;
  expiryDateTo?: string;
  isRequired?: boolean;
  hasOcrText?: boolean;
  tags?: string;
}

export interface DocumentStatistics {
  totalDocuments: number;
  pendingVerification: number;
  approvedDocuments: number;
  rejectedDocuments: number;
  expiringSoon: number; // expiring within 30 days
  expired: number;
  documentsByType: Record<string, number>;
  documentsByVerificationStatus: Record<VerificationStatus, number>;
  averageProcessingTime: number; // hours
  uploadTrends: Array<{
    date: string;
    count: number;
    size: number; // total size in bytes
  }>;
  topUploaders: Array<{
    userId: string;
    userName: string;
    count: number;
  }>;
}

// Enums
export enum VerificationStatus {
  PENDING = 'PENDING',
  IN_REVIEW = 'IN_REVIEW',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED',
  RESUBMIT_REQUIRED = 'RESUBMIT_REQUIRED',
  ON_HOLD = 'ON_HOLD'
}

export enum DocumentCategory {
  IDENTITY = 'IDENTITY',
  INCOME = 'INCOME',
  BANKING = 'BANKING',
  COLLATERAL = 'COLLATERAL',
  BUSINESS = 'BUSINESS',
  OTHER = 'OTHER'
}

// Form related types
export interface DocumentUploadFormData {
  customerId: string;
  documentTypeId: string;
  loanApplicationId: string;
  description: string;
  tags: string;
  expiryDate: string;
}

export interface DocumentUploadFormErrors {
  file?: string;
  customerId?: string;
  documentTypeId?: string;
  description?: string;
  expiryDate?: string;
}

// Component Props types
export interface DocumentListProps {
  documents?: Document[];
  onDocumentSelect?: (document: Document) => void;
  onDocumentDownload?: (documentId: string) => void;
  onDocumentDelete?: (documentId: string) => void;
  onDocumentVerify?: (documentId: string, status: VerificationStatus, notes?: string) => void;
  filters?: DocumentSearchFilters;
  showActions?: boolean;
  customerId?: string;
  loanApplicationId?: string;
  loading?: boolean;
  pageSize?: number;
}

export interface DocumentUploadProps {
  customerId?: string;
  loanApplicationId?: string;
  documentTypeId?: string;
  onSuccess?: (document: Document) => void;
  onError?: (error: string) => void;
  onCancel?: () => void;
  multiple?: boolean;
  maxSize?: number; // in MB
  acceptedTypes?: string[];
  showPreview?: boolean;
}

export interface DocumentViewerProps {
  documentId: string;
  document?: Document;
  onClose?: () => void;
  onDownload?: () => void;
  onDelete?: () => void;
  onVerify?: (status: VerificationStatus, notes?: string) => void;
  readonly?: boolean;
  showActions?: boolean;
}

export interface DocumentPreviewProps {
  document: Document;
  width?: number;
  height?: number;
  onClick?: () => void;
  showMetadata?: boolean;
}

// File upload types
export interface FileUploadProgress {
  file: File;
  progress: number;
  status: 'uploading' | 'success' | 'error';
  error?: string;
  documentId?: string;
}

export interface FileDropZoneProps {
  onDrop: (files: File[]) => void;
  accept?: string[];
  maxSize?: number; // in MB
  multiple?: boolean;
  disabled?: boolean;
  children?: React.ReactNode;
}

// OCR and AI related types
export interface OcrResult {
  text: string;
  confidence: number;
  extractedData?: Record<string, any>;
  boundingBoxes?: Array<{
    text: string;
    x: number;
    y: number;
    width: number;
    height: number;
    confidence: number;
  }>;
}

export interface DocumentAnalysisResult {
  documentType: string;
  confidence: number;
  extractedFields: Record<string, {
    value: string;
    confidence: number;
    boundingBox?: {
      x: number;
      y: number;
      width: number;
      height: number;
    };
  }>;
  validationResults: Array<{
    field: string;
    isValid: boolean;
    message?: string;
  }>;
}

// Constants
export const VERIFICATION_STATUS_OPTIONS = [
  { value: VerificationStatus.PENDING, label: 'Хүлээгдэж буй' },
  { value: VerificationStatus.IN_REVIEW, label: 'Хянагдаж буй' },
  { value: VerificationStatus.APPROVED, label: 'Баталгаажсан' },
  { value: VerificationStatus.REJECTED, label: 'Татгалзсан' },
  { value: VerificationStatus.EXPIRED, label: 'Хугацаа дууссан' },
  { value: VerificationStatus.RESUBMIT_REQUIRED, label: 'Дахин оруулах шаардлагатай' },
  { value: VerificationStatus.ON_HOLD, label: 'Түр зогсоосон' }
];

export const DOCUMENT_CATEGORY_OPTIONS = [
  { value: DocumentCategory.IDENTITY, label: 'Биеийн тодорхой баримт' },
  { value: DocumentCategory.INCOME, label: 'Орлогын баримт' },
  { value: DocumentCategory.BANKING, label: 'Банкны баримт' },
  { value: DocumentCategory.COLLATERAL, label: 'Барьцааны баримт' },
  { value: DocumentCategory.BUSINESS, label: 'Бизнесийн баримт' },
  { value: DocumentCategory.OTHER, label: 'Бусад' }
];

// Common document types
export const COMMON_DOCUMENT_TYPES = [
  { key: 'ID_CARD', label: 'Иргэний үнэмлэх', category: DocumentCategory.IDENTITY, required: true },
  { key: 'PASSPORT', label: 'Гадаад паспорт', category: DocumentCategory.IDENTITY, required: false },
  { key: 'INCOME_STATEMENT', label: 'Орлогын тодорхойлолт', category: DocumentCategory.INCOME, required: true },
  { key: 'SALARY_CERTIFICATE', label: 'Цалингийн тодорхойлолт', category: DocumentCategory.INCOME, required: false },
  { key: 'BANK_STATEMENT', label: 'Банкны хуулга', category: DocumentCategory.BANKING, required: true },
  { key: 'COLLATERAL_DOCUMENT', label: 'Барьцааны гэрчилгээ', category: DocumentCategory.COLLATERAL, required: false },
  { key: 'BUSINESS_LICENSE', label: 'Бизнесийн лиценз', category: DocumentCategory.BUSINESS, required: false },
  { key: 'TAX_RETURN', label: 'Татварын тайлан', category: DocumentCategory.BUSINESS, required: false },
  { key: 'FINANCIAL_STATEMENT', label: 'Санхүүгийн тайлан', category: DocumentCategory.BUSINESS, required: false }
];

// File type mappings
export const FILE_TYPE_ICONS: Record<string, string> = {
  'application/pdf': 'file-pdf',
  'image/jpeg': 'file-image',
  'image/jpg': 'file-image',
  'image/png': 'file-image',
  'image/gif': 'file-image',
  'application/msword': 'file-word',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'file-word',
  'application/vnd.ms-excel': 'file-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'file-excel',
  'text/plain': 'file-text',
  'default': 'file'
};

export const ALLOWED_FILE_TYPES = [
  'application/pdf',
  'image/jpeg',
  'image/jpg',
  'image/png',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
];

// File size limits
export const FILE_SIZE_LIMITS = {
  max: 50 * 1024 * 1024, // 50MB
  warning: 10 * 1024 * 1024 // 10MB
};

// Status colors for UI
export const VERIFICATION_STATUS_COLORS: Record<VerificationStatus, string> = {
  [VerificationStatus.PENDING]: '#faad14',
  [VerificationStatus.IN_REVIEW]: '#722ed1',
  [VerificationStatus.APPROVED]: '#52c41a',
  [VerificationStatus.REJECTED]: '#f5222d',
  [VerificationStatus.EXPIRED]: '#8c8c8c',
  [VerificationStatus.RESUBMIT_REQUIRED]: '#fa8c16',
  [VerificationStatus.ON_HOLD]: '#1890ff'
};

// Utility functions for file handling
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

export const getFileExtension = (filename: string): string => {
  return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2);
};

export const getFileTypeIcon = (contentType: string): string => {
  return FILE_TYPE_ICONS[contentType] || FILE_TYPE_ICONS.default;
};

export const isImageFile = (contentType: string): boolean => {
  return contentType.startsWith('image/');
};

export const isPdfFile = (contentType: string): boolean => {
  return contentType === 'application/pdf';
};

// API Response types
export interface DocumentListResponse {
  content: Document[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface DocumentUploadResponse extends Document {
  uploadUrl?: string;
  thumbnailUrl?: string;
}

export interface BulkDocumentOperationResult {
  success: number;
  failed: number;
  errors: Array<{
    documentId: string;
    error: string;
  }>;
}

// Export/Import types
export interface DocumentExportOptions {
  format: 'zip' | 'pdf';
  filters?: DocumentSearchFilters;
  includeMetadata?: boolean;
  includeVerificationNotes?: boolean;
}

// Default values
export const DEFAULT_DOCUMENT_UPLOAD_FORM: Partial<DocumentUploadFormData> = {
  description: '',
  tags: ''
};