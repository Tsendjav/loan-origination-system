# ================================================================
# 🏦 LOS Backend Dockerfile
# Build: docker build -f Dockerfile.backend.dockerfile -t los-backend .
# ================================================================

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# pom.xml-г эхэлж хуулж, dependency-г кэшлэнэ (repo-д mvnw байхгүй тул mvn)
COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline

# Эх код
COPY backend/src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    dumb-init \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/logs /app/uploads/documents \
    && chown -R appuser:appuser /app

USER appuser

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:8080/los/actuator/health || exit 1

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

LABEL maintainer="LOS Development Team"
LABEL description="LOS Backend Application"
