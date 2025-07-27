// frontend/src/services/healthService.ts - САЙЖРУУЛСАН
import { apiClient } from './apiClient';
import { ENDPOINTS, DEV_URLS } from './apiConfig';

export interface HealthStatus {
  status: string;
  timestamp: string;
  service: string;
  version: string;
  'java.version'?: string;
  components?: {
    database?: { status: string; type: string };
    diskSpace?: { status: string };
    [key: string]: any;
  };
}

export interface ConnectionTestResult {
  success: boolean;
  message: string;
  responseTime?: number;
  timestamp: string;
  endpoint?: string;
  details?: any;
}

class HealthService {
  private healthCheckInterval: NodeJS.Timeout | null = null;
  private listeners: Array<(status: HealthStatus | null) => void> = [];

  // Subscribe to health status changes
  subscribe(listener: (status: HealthStatus | null) => void) {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  // Notify all listeners of health status changes
  private notifyListeners(status: HealthStatus | null) {
    this.listeners.forEach(listener => listener(status));
  }

  // ⭐ САЙЖРУУЛСАН: Health status with fallback endpoints
  async getHealthStatus(): Promise<HealthStatus> {
    console.log('🏥 Checking detailed health status...');
    
    try {
      // Try main health endpoint first
      const status = await apiClient.get<HealthStatus>(ENDPOINTS.HEALTH.CHECK);
      console.log('✅ Health status received:', status);
      
      this.notifyListeners(status);
      return status;
    } catch (error) {
      console.warn('⚠️ Main health endpoint failed, trying simple health...');
      
      try {
        // Try simple health as fallback
        const simpleResult = await apiClient.get(ENDPOINTS.HEALTH.SIMPLE);
        
        // Create a mock health status
        const mockStatus: HealthStatus = {
          status: 'UP',
          service: 'LOS Backend',
          version: '1.0.0',
          timestamp: new Date().toISOString()
        };
        
        console.log('✅ Simple health OK, using mock status:', mockStatus);
        this.notifyListeners(mockStatus);
        return mockStatus;
      } catch (simpleError) {
        console.error('❌ All health checks failed:', error, simpleError);
        this.notifyListeners(null);
        throw error;
      }
    }
  }

  // ⭐ САЙЖРУУЛСАН: Simple health check with multiple attempts
  async simpleHealthCheck(): Promise<boolean> {
    const endpoints = [
      ENDPOINTS.HEALTH.SIMPLE,
      ENDPOINTS.HEALTH.CHECK,
      '/' // Home endpoint as last resort
    ];
    
    for (const endpoint of endpoints) {
      try {
        console.log(`🔍 Testing ${endpoint}...`);
        await apiClient.get(endpoint);
        console.log(`✅ ${endpoint} is working`);
        return true;
      } catch (error) {
        console.warn(`⚠️ ${endpoint} failed:`, error);
        continue;
      }
    }
    
    console.error('❌ All simple health endpoints failed');
    return false;
  }

  // ⭐ САЙЖРУУЛСАН: Connection test with comprehensive diagnostics
  async testConnection(): Promise<ConnectionTestResult> {
    const startTime = Date.now();
    
    console.log('🧪 Starting comprehensive connection test...');
    
    try {
      // Use improved apiClient test method
      const testResult = await apiClient.testConnection();
      
      return {
        success: testResult.success,
        message: testResult.message,
        responseTime: testResult.responseTime || (Date.now() - startTime),
        timestamp: new Date().toISOString(),
        endpoint: testResult.endpoint,
        details: testResult
      };
    } catch (error) {
      return {
        success: false,
        message: error instanceof Error ? error.message : 'Холболтын алдаа',
        responseTime: Date.now() - startTime,
        timestamp: new Date().toISOString(),
        details: { error: error instanceof Error ? error.message : 'Unknown error' }
      };
    }
  }

  // ⭐ НЭМЭЛТ: Direct backend URL test
  async testDirectBackendUrl(): Promise<boolean> {
    const backendUrls = [
      DEV_URLS.HEALTH_CHECK,
      `${DEV_URLS.BACKEND_BASE}/api/v1/health/simple`,
      `${DEV_URLS.BACKEND_BASE}/api/v1/`,
      DEV_URLS.BACKEND_BASE
    ];
    
    for (const url of backendUrls) {
      try {
        console.log(`🎯 Testing direct URL: ${url}`);
        const success = await apiClient.testDirectUrl(url);
        if (success) {
          console.log(`✅ Direct URL test passed: ${url}`);
          return true;
        }
      } catch (error) {
        console.warn(`❌ Direct URL failed: ${url}`, error);
      }
    }
    
    return false;
  }

  // Wait for backend with better progress reporting
  async waitForBackend(
    maxRetries = 10,
    delay = 2000,
    onProgress?: (attempt: number, success: boolean, message: string) => void
  ): Promise<boolean> {
    console.log('⏳ Backend хүлээж байна...');
    
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        const testResult = await this.testConnection();
        
        if (onProgress) {
          onProgress(attempt, testResult.success, testResult.message);
        }
        
        if (testResult.success) {
          console.log('✅ Backend амжилттай холбогдлоо!');
          return true;
        }
      } catch (error) {
        const message = `Backend хүлээж байна... (${attempt}/${maxRetries})`;
        console.log(message);
        
        if (onProgress) {
          onProgress(attempt, false, message);
        }
      }
      
      // Wait before next attempt (except on last attempt)
      if (attempt < maxRetries) {
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
    
    console.error('❌ Backend хүлээх хугацаа дууслаа');
    return false;
  }

  // Start health monitoring with better error handling
  startHealthMonitoring(intervalMs = 30000): void {
    if (this.healthCheckInterval) {
      this.stopHealthMonitoring();
    }
    
    console.log(`🔄 Health monitoring эхэлж байна (${intervalMs}ms тутам)`);
    
    // Initial check with delay to let app settle
    setTimeout(() => {
      this.getHealthStatus().catch((error) => {
        console.warn('Initial health check failed:', error);
      });
    }, 1000);
    
    // Set up interval
    this.healthCheckInterval = setInterval(async () => {
      try {
        await this.getHealthStatus();
      } catch (error) {
        console.warn('Periodic health check failed:', error);
      }
    }, intervalMs);
  }

  // Stop health monitoring
  stopHealthMonitoring(): void {
    if (this.healthCheckInterval) {
      console.log('🛑 Health monitoring зогссон');
      clearInterval(this.healthCheckInterval);
      this.healthCheckInterval = null;
    }
  }

  // ⭐ САЙЖРУУЛСАН: API endpoints check with actual backend endpoints
  async checkApiEndpoints(): Promise<Record<string, boolean>> {
    const endpoints = [
      { name: 'health', path: ENDPOINTS.HEALTH.CHECK },
      { name: 'health-simple', path: ENDPOINTS.HEALTH.SIMPLE },
      { name: 'auth', path: ENDPOINTS.AUTH.LOGIN },
      { name: 'documents', path: ENDPOINTS.DOCUMENTS.BASE },
      { name: 'customers', path: ENDPOINTS.CUSTOMERS.BASE },
      { name: 'loans', path: ENDPOINTS.LOANS.BASE },
    ];

    const results: Record<string, boolean> = {};

    for (const endpoint of endpoints) {
      try {
        console.log(`🧪 Testing endpoint: ${endpoint.name} (${endpoint.path})`);
        
        if (endpoint.name === 'auth') {
          // For auth endpoint, expect 401 or 400 (not 404)
          try {
            await apiClient.post(endpoint.path, {});
          } catch (error) {
            const errorMsg = error instanceof Error ? error.message : '';
            if (errorMsg.includes('401') || errorMsg.includes('400') || errorMsg.includes('Unauthorized')) {
              results[endpoint.name] = true; // Endpoint exists, just needs proper auth
              continue;
            }
            throw error;
          }
        } else {
          await apiClient.get(endpoint.path);
        }
        
        results[endpoint.name] = true;
        console.log(`✅ ${endpoint.name} endpoint OK`);
      } catch (error) {
        const errorMsg = error instanceof Error ? error.message : '';
        
        // Don't treat auth-related errors as endpoint failures
        if (errorMsg.includes('401') || errorMsg.includes('403')) {
          results[endpoint.name] = true; // Endpoint exists but requires auth
          console.log(`⚠️ ${endpoint.name} endpoint requires auth (OK)`);
        } else {
          results[endpoint.name] = false;
          console.log(`❌ ${endpoint.name} endpoint failed:`, errorMsg);
        }
      }
    }

    console.log('📊 API endpoints test results:', results);
    return results;
  }

  // ⭐ НЭМЭЛТ: System diagnostics
  async getSystemDiagnostics(): Promise<Record<string, any>> {
    const diagnostics: Record<string, any> = {
      timestamp: new Date().toISOString(),
      frontend: {
        url: DEV_URLS.FRONTEND_BASE,
        userAgent: navigator.userAgent,
        online: navigator.onLine
      },
      backend: {
        configuredUrl: DEV_URLS.BACKEND_BASE,
        healthUrl: DEV_URLS.HEALTH_CHECK
      }
    };

    try {
      // Test backend connection
      const connectionTest = await this.testConnection();
      diagnostics.backend.connectionTest = connectionTest;

      // Test API endpoints
      const endpointTests = await this.checkApiEndpoints();
      diagnostics.backend.endpoints = endpointTests;

      // Try to get health status
      try {
        const healthStatus = await this.getHealthStatus();
        diagnostics.backend.health = healthStatus;
      } catch (error) {
        diagnostics.backend.healthError = error instanceof Error ? error.message : 'Unknown error';
      }

    } catch (error) {
      diagnostics.error = error instanceof Error ? error.message : 'Unknown error';
    }

    return diagnostics;
  }

  // Cleanup when service is destroyed
  destroy(): void {
    this.stopHealthMonitoring();
    this.listeners.length = 0;
  }
}

// Create singleton instance
export const healthService = new HealthService();

// Auto-cleanup on page unload
if (typeof window !== 'undefined') {
  window.addEventListener('beforeunload', () => {
    healthService.destroy();
  });
}