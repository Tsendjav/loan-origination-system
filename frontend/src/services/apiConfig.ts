// frontend/src/services/apiConfig.ts - САЙЖРУУЛСАН
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8080/los/api/v1',
  TIMEOUT: 10000,
  HEADERS: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  }
};

// API endpoints - Backend controller-тай тохирсон
export const ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout', 
    ME: '/auth/me',
  },
  HEALTH: {
    CHECK: '/health',
    SIMPLE: '/health/simple', // Backend дээр энэ endpoint байгаа эсэхийг шалгах хэрэгтэй
  },
  DOCUMENTS: {
    BASE: '/documents',
    UPLOAD: '/documents/upload',
    DOWNLOAD: (id: string) => `/documents/${id}/download`,
    BY_CUSTOMER: (customerId: string) => `/documents/customer/${customerId}`,
    VERIFY: (id: string) => `/documents/${id}/verify`,
    APPROVE: (id: string) => `/documents/${id}/approve`,
    REJECT: (id: string) => `/documents/${id}/reject`,
  },
  CUSTOMERS: {
    BASE: '/customers',
    BY_ID: (id: string) => `/customers/${id}`,
  },
  LOANS: {
    BASE: '/loan-applications',
    BY_ID: (id: string) => `/loan-applications/${id}`,
  }
};

// Error messages - Монгол хэл
export const ERROR_MESSAGES = {
  NETWORK_ERROR: 'Сүлжээний алдаа гарлаа',
  TIMEOUT_ERROR: 'Хүсэлт хугацаа хэтэрлээ', 
  SERVER_ERROR: 'Серверийн алдаа гарлаа',
  UNAUTHORIZED: 'Нэвтрэх шаардлагатай',
  FORBIDDEN: 'Хандах эрх байхгүй',
  NOT_FOUND: 'Өгөгдөл олдсонгүй',
  VALIDATION_ERROR: 'Өгөгдлийн алдаа',
  CORS_ERROR: 'CORS тохиргооны алдаа',
  CONNECTION_ERROR: 'Backend холболт тасарсан'
};

// Development URLs for easy access
export const DEV_URLS = {
  BACKEND_BASE: 'http://localhost:8080/los',
  FRONTEND_BASE: 'http://localhost:3001',
  HEALTH_CHECK: 'http://localhost:8080/los/api/v1/health',
  H2_CONSOLE: 'http://localhost:8080/los/h2-console',
  SWAGGER_UI: 'http://localhost:8080/los/swagger-ui.html'
};