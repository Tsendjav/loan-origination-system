import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import * as path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
      "@components": path.resolve(__dirname, "./src/components"),
      "@services": path.resolve(__dirname, "./src/services"),
      "@types": path.resolve(__dirname, "./src/types"),
      "@utils": path.resolve(__dirname, "./src/utils"),
      "@assets": path.resolve(__dirname, "./src/assets"),
    },
  },
  server: {
    port: 3001,
    host: '0.0.0.0', // Allow external connections
    strictPort: true, // Fail if port is already in use
    
    // ⭐ BACKEND PROXY CONFIGURATION - САЙЖРУУЛСАН ⭐
    proxy: {
      // Proxy for LOS backend API
      "/los": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
        timeout: 30000, // 30 second timeout
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.error('❌ Proxy error:', err);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log(`📤 Proxy request: ${req.method} ${req.url}`);
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log(`📥 Proxy response: ${req.method} ${req.url} - ${proxyRes.statusCode}`);
          });
        }
      },
      
      // Direct API proxy (alternative path)
      "/api": {
        target: "http://localhost:8080/los",
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      },
      
      // Health check proxy
      "/health": {
        target: "http://localhost:8080/los",
        changeOrigin: true,
        secure: false,
      },
      
      // Actuator endpoints proxy  
      "/actuator": {
        target: "http://localhost:8080/los",
        changeOrigin: true,
        secure: false,
      },
      
      // H2 Console proxy (development only)
      "/h2-console": {
        target: "http://localhost:8080/los",
        changeOrigin: true,
        secure: false,
        ws: true, // Enable WebSocket proxying for H2 console
      },
      
      // Swagger UI proxy
      "/swagger-ui": {
        target: "http://localhost:8080/los",
        changeOrigin: true,
        secure: false,
      },
      
      // API Documentation proxy
      "/v3/api-docs": {
        target: "http://localhost:8080/los",
        changeOrigin: true,
        secure: false,
      }
    },
  },
  
  build: {
    outDir: "dist",
    sourcemap: true,
    minify: 'esbuild',
    target: 'es2015',
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'antd-vendor': ['antd', '@ant-design/icons'],
          'utils-vendor': ['axios', 'dayjs', 'lodash']
        }
      }
    },
    // Increase chunk size limit
    chunkSizeWarningLimit: 1000,
  },
  
  // Development optimizations
  optimizeDeps: {
    include: [
      'react',
      'react-dom',
      'react-router-dom', 
      'antd',
      '@ant-design/icons',
      'axios',
      'dayjs',
      'lodash'
    ]
  },
  
  // Environment variables
  define: {
    __APP_VERSION__: JSON.stringify(process.env.npm_package_version || '1.0.0'),
    __BUILD_TIME__: JSON.stringify(new Date().toISOString()),
    __BUILD_ENV__: JSON.stringify(process.env.NODE_ENV || 'development'),
  },
  
  // CSS configuration
  css: {
    preprocessorOptions: {
      less: {
        javascriptEnabled: true,
        modifyVars: {
          // Antd theme customization
          '@primary-color': '#1890ff',
          '@success-color': '#52c41a',
          '@warning-color': '#faad14',
          '@error-color': '#ff4d4f',
          '@font-size-base': '14px',
          '@border-radius-base': '6px',
        },
      },
    },
  },
  
  // Development server options
  esbuild: {
    logOverride: { 'this-is-undefined-in-esm': 'silent' }
  },
  
  // Preview server configuration
  preview: {
    port: 3002,
    strictPort: true,
    host: '0.0.0.0'
  }
})