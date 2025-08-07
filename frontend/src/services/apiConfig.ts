// frontend/src/services/apiConfig.ts - FINAL BACKEND-COMPATIBLE VERSION
export const API_CONFIG = {
  BASE_URL: process.env.REACT_APP_API_URL || 'http://localhost:8080/los/api/v1',
  TIMEOUT: 15000, // 15 seconds for backend compatibility
  RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000,
  HEADERS: {
    'Content-Type': 'application/json;charset=UTF-8', // ⭐ Backend AuthController тохирсон
    'Accept': 'application/json;charset=UTF-8',
  }
};

// ⭐ API ENDPOINTS - BACKEND AuthController-тай бүрэн тохирсон ⭐
export const ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',                    // ✅ POST /api/v1/auth/login
    LOGOUT: '/auth/logout',                  // ✅ POST /api/v1/auth/logout  
    ME: '/auth/me',                         // ✅ GET /api/v1/auth/me
    REFRESH: '/auth/refresh',               // ✅ POST /api/v1/auth/refresh
    VALIDATE: '/auth/validate',             // ✅ GET /api/v1/auth/validate
    CHANGE_PASSWORD: '/auth/change-password', // ✅ POST /api/v1/auth/change-password
    TEST: '/auth/test',                     // ✅ GET /api/v1/auth/test
    HEALTH: '/auth/health',                 // ✅ GET /api/v1/auth/health
    TEST_USERS: '/auth/test-users',         // ✅ GET /api/v1/auth/test-users
    TEST_VALIDATION: '/auth/test-validation', // ✅ POST /api/v1/auth/test-validation
    DEBUG_JSON: '/auth/debug-json'          // ✅ POST /api/v1/auth/debug-json
  },
  HEALTH: {
    CHECK: '/health',                       // General health endpoint
    SIMPLE: '/health/simple',               // Simple health check
    DETAILED: '/actuator/health'            // Spring Actuator health
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
    SEARCH: '/customers/search',
    CREATE: '/customers',
    UPDATE: (id: string) => `/customers/${id}`,
    DELETE: (id: string) => `/customers/${id}`,
  },
  LOANS: {
    BASE: '/loan-applications',
    BY_ID: (id: string) => `/loan-applications/${id}`,
    CREATE: '/loan-applications',
    UPDATE: (id: string) => `/loan-applications/${id}`,
    DELETE: (id: string) => `/loan-applications/${id}`,
    STATUS: (id: string) => `/loan-applications/${id}/status`,
    ASSESS: (id: string) => `/loan-applications/${id}/assess`,
  }
};

// ⭐ ERROR MESSAGES - Монгол хэлээр Backend response-тай тохирсон ⭐
export const ERROR_MESSAGES = {
  // Network errors
  NETWORK_ERROR: 'Сүлжээний алдаа гарлаа',
  TIMEOUT_ERROR: 'Хүсэлт хугацаа хэтэрлээ', 
  CONNECTION_ERROR: 'Backend серверт холбогдохгүй байна',
  CORS_ERROR: 'CORS тохиргооны алдаа',
  
  // HTTP status errors
  UNAUTHORIZED: 'Нэвтрэх шаардлагатай',
  FORBIDDEN: 'Хандах эрх байхгүй',
  NOT_FOUND: 'Өгөгдөл олдсонгүй',
  SERVER_ERROR: 'Серверийн алдаа гарлаа',
  BAD_REQUEST: 'Хүсэлт буруу байна',
  
  // Validation errors
  VALIDATION_ERROR: 'Өгөгдлийн алдаа',
  VALIDATION_FAILED: 'Validation алдаатай',
  
  // Auth specific errors
  LOGIN_FAILED: 'Нэвтрэхэд алдаа гарлаа',
  LOGOUT_FAILED: 'Гарахад алдаа гарлаа',
  TOKEN_EXPIRED: 'Token хүчингүй болсон',
  TOKEN_INVALID: 'Token буруу байна',
  PASSWORD_INCORRECT: 'Нууц үг буруу байна',
  USERNAME_NOT_FOUND: 'Хэрэглэгч олдсонгүй',
  
  // Generic errors
  UNKNOWN_ERROR: 'Тодорхойгүй алдаа гарлаа',
  SERVICE_UNAVAILABLE: 'Үйлчилгээ боломжгүй байна'
};

// ⭐ SUCCESS MESSAGES - Монгол хэлээр ⭐
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Амжилттай нэвтэрлээ',
  LOGOUT_SUCCESS: 'Амжилттай гарлаа',
  PASSWORD_CHANGED: 'Нууц үг амжилттай солигдлоо',
  DATA_SAVED: 'Өгөгдөл амжилттай хадгалагдлаа',
  DATA_UPDATED: 'Өгөгдөл амжилттай шинэчлэгдлээ',
  DATA_DELETED: 'Өгөгдөл амжилттай устгагдлаа'
};

// ⭐ DEVELOPMENT URLs - Backend endpoints-д хялбар хандах ⭐
export const DEV_URLS = {
  // Backend URLs
  BACKEND_BASE: 'http://localhost:8080/los',
  API_BASE: 'http://localhost:8080/los/api/v1',
  
  // Frontend URLs  
  FRONTEND_BASE: 'http://localhost:3001',
  
  // Health check URLs
  HEALTH_CHECK: 'http://localhost:8080/los/api/v1/auth/health',
  GENERAL_HEALTH: 'http://localhost:8080/los/actuator/health',
  
  // Development tools
  H2_CONSOLE: 'http://localhost:8080/los/h2-console',
  SWAGGER_UI: 'http://localhost:8080/los/swagger-ui.html',
  API_DOCS: 'http://localhost:8080/los/v3/api-docs',
  
  // Auth test URLs
  AUTH_TEST: 'http://localhost:8080/los/api/v1/auth/test',
  AUTH_TEST_USERS: 'http://localhost:8080/los/api/v1/auth/test-users',
  AUTH_DEBUG: 'http://localhost:8080/los/api/v1/auth/debug-json'
};

// ⭐ REQUEST CONFIG TEMPLATES ⭐
export const REQUEST_CONFIGS = {
  // Default config for all requests
  DEFAULT: {
    timeout: API_CONFIG.TIMEOUT,
    headers: API_CONFIG.HEADERS,
  },
  
  // Auth requests config
  AUTH: {
    timeout: API_CONFIG.TIMEOUT,
    headers: {
      ...API_CONFIG.HEADERS,
      // No Authorization header for auth requests
    },
  },
  
  // File upload config
  UPLOAD: {
    timeout: 30000, // 30 seconds for file uploads
    headers: {
      'Content-Type': 'multipart/form-data',
      'Accept': 'application/json;charset=UTF-8',
    },
  },
  
  // Download config
  DOWNLOAD: {
    timeout: 60000, // 60 seconds for downloads
    responseType: 'blob' as const,
  }
};

// ⭐ VALIDATION RULES - Frontend validation үүнээс Backend-тай тохирсон ⭐
export const VALIDATION_RULES = {
  USERNAME: {
    MIN_LENGTH: 3,
    MAX_LENGTH: 50,
    PATTERN: /^[a-zA-Z0-9._@-]+$/,
    ERROR_MESSAGES: {
      REQUIRED: 'Хэрэглэгчийн нэр заавал оруулна уу',
      MIN_LENGTH: 'Хэрэглэгчийн нэр хамгийн багадаа 3 тэмдэгт байх ёстой',
      MAX_LENGTH: 'Хэрэглэгчийн нэр 50 тэмдэгтээс их байж болохгүй',
      PATTERN: 'Хэрэглэгчийн нэр зөвхөн үсэг, тоо, цэг, дэд зураас, @ тэмдэг агуулах боломжтой'
    }
  },
  PASSWORD: {
    MIN_LENGTH: 6,
    MAX_LENGTH: 100,
    ERROR_MESSAGES: {
      REQUIRED: 'Нууц үг заавал оруулна уу',
      MIN_LENGTH: 'Нууц үг хамгийн багадаа 6 тэмдэгт байх ёстой',
      MAX_LENGTH: 'Нууц үг 100 тэмдэгтээс их байж болохгүй'
    }
  },
  EMAIL: {
    PATTERN: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
    ERROR_MESSAGES: {
      PATTERN: 'Имэйл хаягийн формат буруу байна'
    }
  }
};

// ⭐ HTTP STATUS CODES ⭐
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  METHOD_NOT_ALLOWED: 405,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503
} as const;

// ⭐ LOCAL STORAGE KEYS ⭐
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'los_auth_token',
  REFRESH_TOKEN: 'los_refresh_token', 
  USER_INFO: 'los_user_info',
  REMEMBER_ME: 'los_remember_me',
  LAST_LOGIN: 'los_last_login',
  SETTINGS: 'los_user_settings'
} as const;

// ⭐ ENVIRONMENT CONFIG ⭐
export const ENV_CONFIG = {
  isDevelopment: process.env.NODE_ENV === 'development',
  isProduction: process.env.NODE_ENV === 'production',
  isTest: process.env.NODE_ENV === 'test',
  
  // API URLs based on environment
  getApiUrl: () => {
    if (process.env.REACT_APP_API_URL) {
      return process.env.REACT_APP_API_URL;
    }
    
    // Default URLs based on environment
    switch (process.env.NODE_ENV) {
      case 'development':
        return 'http://localhost:8080/los/api/v1';
      case 'production':
        return '/api/v1'; // Same domain in production
      default:
        return 'http://localhost:8080/los/api/v1';
    }
  },
  
  // Debug mode
  enableDebugLogging: process.env.NODE_ENV === 'development',
  enableNetworkLogging: process.env.NODE_ENV === 'development',
};

// ⭐ DEFAULT CONFIG EXPORT ⭐
export default {
  API_CONFIG,
  ENDPOINTS,
  ERROR_MESSAGES,
  SUCCESS_MESSAGES,
  DEV_URLS,
  REQUEST_CONFIGS,
  VALIDATION_RULES,
  HTTP_STATUS,
  STORAGE_KEYS,
  ENV_CONFIG
};