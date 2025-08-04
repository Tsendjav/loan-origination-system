# Multi-stage build for React application
FROM node:18-alpine as builder

# Set working directory
WORKDIR /app

# Add package manager and build tools
RUN apk add --no-cache git

# Copy package files
COPY frontend/package*.json ./
COPY frontend/yarn.lock* ./

# Install dependencies
RUN npm ci --only=production --silent

# Copy source code
COPY frontend/src ./src
COPY frontend/public ./public
COPY frontend/tsconfig.json ./
COPY frontend/vite.config.ts ./
COPY frontend/index.html ./
COPY frontend/.env* ./

# Build arguments for environment variables
ARG REACT_APP_API_URL=http://localhost:8080/los/api/v1
ARG REACT_APP_ENV=production
ARG REACT_APP_VERSION=1.0.0

# Set environment variables for build
ENV REACT_APP_API_URL=$REACT_APP_API_URL
ENV REACT_APP_ENV=$REACT_APP_ENV
ENV REACT_APP_VERSION=$REACT_APP_VERSION
ENV NODE_ENV=production

# Build the application
RUN npm run build

# Production stage with Nginx
FROM nginx:alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Copy custom nginx configuration
COPY docker/nginx/frontend.conf /etc/nginx/conf.d/default.conf

# Copy built application from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx configuration for SPA routing
RUN echo 'server { \
    listen 3001; \
    server_name localhost; \
    root /usr/share/nginx/html; \
    index index.html; \
    \
    # Enable gzip compression \
    gzip on; \
    gzip_vary on; \
    gzip_min_length 1024; \
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/json application/xml+rss application/atom+xml image/svg+xml; \
    \
    # Security headers \
    add_header X-Frame-Options "SAMEORIGIN" always; \
    add_header X-Content-Type-Options "nosniff" always; \
    add_header X-XSS-Protection "1; mode=block" always; \
    add_header Referrer-Policy "no-referrer-when-downgrade" always; \
    add_header Content-Security-Policy "default-src '\''self'\'' http: https: data: blob: '\''unsafe-inline'\''" always; \
    \
    # Handle SPA routing \
    location / { \
        try_files $uri $uri/ /index.html; \
        \
        # Cache static assets \
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ { \
            expires 1y; \
            add_header Cache-Control "public, immutable"; \
        } \
    } \
    \
    # API proxy (optional - for development) \
    location /api/ { \
        proxy_pass http://backend:8080/los/api/; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; \
        proxy_set_header X-Forwarded-Proto $scheme; \
    } \
    \
    # Health check endpoint \
    location /health { \
        access_log off; \
        return 200 "healthy\\n"; \
        add_header Content-Type text/plain; \
    } \
    \
    # Security - deny access to dotfiles \
    location ~ /\\. { \
        deny all; \
    } \
}' > /etc/nginx/conf.d/default.conf

# Create nginx user and set permissions
RUN addgroup -g 101 -S nginx \
    && adduser -S -D -H -u 101 -h /var/cache/nginx -s /sbin/nologin -G nginx -g nginx nginx \
    && chown -R nginx:nginx /usr/share/nginx/html \
    && chown -R nginx:nginx /var/cache/nginx \
    && chown -R nginx:nginx /var/log/nginx \
    && chown -R nginx:nginx /etc/nginx/conf.d \
    && touch /var/run/nginx.pid \
    && chown -R nginx:nginx /var/run/nginx.pid

# Switch to non-root user
USER nginx

# Expose port
EXPOSE 3001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:3001/health || exit 1

# Environment variables
ENV NGINX_HOST=localhost
ENV NGINX_PORT=3001

# Labels for better container management
LABEL maintainer="Tsendjav <tsendjav@example.com>"
LABEL version="1.0"
LABEL description="Loan Origination System Frontend Application"
LABEL org.opencontainers.image.source="https://github.com/Tsendjav/loan-origination-system"

# Start nginx
CMD ["nginx", "-g", "daemon off;"]