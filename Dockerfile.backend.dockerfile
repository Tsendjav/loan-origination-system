# ================================================================
# 🏦 LOS Backend Dockerfile - Production Ready
# ================================================================

# Multi-stage build for optimization
FROM maven:3.9-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better caching)
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY backend/src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# ================================================================
# Production stage
# ================================================================
FROM openjdk:17-jre-slim

# Install necessary packages for production
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    dumb-init \
    && rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Set file permissions
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/los/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Start the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for metadata
LABEL maintainer="LOS Development Team"
LABEL version="1.0.0"
LABEL description="LOS Backend Application"