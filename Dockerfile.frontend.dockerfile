# ================================================================
# 🖥️ LOS Frontend Dockerfile (React + Vite + Nginx)
# Build: docker build -f Dockerfile.frontend.dockerfile -t los-frontend .
# ================================================================

# ---------- Build stage ----------
FROM node:18-alpine AS builder

WORKDIR /app

COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --silent

COPY frontend/tsconfig.json frontend/tsconfig.node.json frontend/vite.config.ts ./
COPY frontend/index.html ./
COPY frontend/src ./src

ARG VITE_API_URL=/los/api/v1
ENV VITE_API_URL=$VITE_API_URL
ENV NODE_ENV=production

RUN npm run build

# ---------- Runtime stage ----------
FROM nginx:alpine

RUN apk add --no-cache curl

COPY docker/nginx/frontend.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

RUN chown -R nginx:nginx /usr/share/nginx/html

EXPOSE 3001

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:3001/health || exit 1

CMD ["nginx", "-g", "daemon off;"]

LABEL maintainer="LOS Development Team"
LABEL description="Loan Origination System Frontend Application"
