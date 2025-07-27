# ================================================================
# 🏦 LOS Дутуу Файлууд Үүсгэгч - Template Generator
# LOS-Missing-Files-Creator.ps1
# ================================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$Week = "",
    
    [Parameter(Mandatory=$false)]
    [switch]$CreateAll = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun = $false,
    
    [Parameter(Mandatory=$false)]
    [string]$LogFile = "los-file-creation.log"
)

# UTF-8 дэмжлэг
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

function Write-ColoredText {
    param($Text, $Color = "White")
    Write-Host $Text -ForegroundColor $Color
}

function Write-Log {
    param($Message)
    if ($LogFile) {
        Add-Content -Path $LogFile -Value "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss'): $Message" -Encoding UTF8
    }
}

# Template файлууд
$templates = @{
    "backend/pom.xml" = @'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.los</groupId>
    <artifactId>loan-origination-system</artifactId>
    <version>1.0.0</version>
    <name>Loan Origination System</name>
    <description>Comprehensive Loan Origination System</description>
    
    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.2.0</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
'@

    "backend/src/main/java/com/los/LosApplication.java" = @'
package com.los;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Loan Origination System - Зээлийн Санал Өгөх Систем
 * 
 * @author LOS Development Team
 * @version 1.0.0
 */
@SpringBootApplication
public class LosApplication {

    public static void main(String[] args) {
        SpringApplication.run(LosApplication.class, args);
        System.out.println("\n🏦 LOS - Loan Origination System эхлэж байна...");
        System.out.println("🌐 Backend: http://localhost:8080/los");
        System.out.println("📖 API Docs: http://localhost:8080/los/swagger-ui.html");
        System.out.println("🗄️ H2 Console: http://localhost:8080/los/h2-console");
    }
}
'@

    "backend/src/main/java/com/los/common/BaseEntity.java" = @'
package com.los.common;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Бүх Entity-үүдийн суурь класс
 * Үүсгэсэн огноо, шинэчилсэн огноо зэрэг ерөнхий талбаруудыг агуулна
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
'@

    "backend/src/main/java/com/los/entity/Customer.java" = @'
package com.los.entity;

import com.los.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Харилцагчийн мэдээлэл
 */
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType;

    @NotBlank(message = "Овог нэр заавал оруулна уу")
    @Size(max = 50, message = "Овог нэр 50 тэмдэгтээс ихгүй байна")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Нэр заавал оруулна уу")
    @Size(max = 50, message = "Нэр 50 тэмдэгтээс ихгүй байна")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Регистрийн дугаар заавал оруулна уу")
    @Pattern(regexp = "^[А-Я]{2}\\d{8}$", message = "Регистрийн дугаар буруу байна")
    @Column(name = "register_number", unique = true, nullable = false, length = 10)
    private String registerNumber;

    @Pattern(regexp = "^\\+976[0-9]{8}$", message = "Утасны дугаар буруу байна")
    @Column(name = "phone", length = 15)
    private String phone;

    @Email(message = "И-мэйл хаяг буруу байна")
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "monthly_income")
    private Double monthlyIncome;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CustomerStatus status = CustomerStatus.ACTIVE;

    public enum CustomerType {
        INDIVIDUAL,    // Хувь хүн
        BUSINESS      // Хуулийн этгээд
    }

    public enum CustomerStatus {
        ACTIVE,       // Идэвхтэй
        INACTIVE,     // Идэвхгүй
        BLOCKED       // Блоклогдсон
    }

    // Getters and Setters
    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(Double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
'@

    "backend/src/main/resources/application.yml" = @'
# LOS - Loan Origination System Configuration
spring:
  application:
    name: loan-origination-system
  
  # Database Configuration
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: true
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
  
  # Security Configuration
  security:
    user:
      name: admin
      password: admin123
      roles: ADMIN
  
  # Server Configuration
server:
  port: 8080
  servlet:
    context-path: /los

# JWT Configuration
jwt:
  secret: mySecretKey
  expiration: 7200 # 2 hours in seconds

# Logging Configuration
logging:
  level:
    com.los: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/los-application.log

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

# Application Information
info:
  app:
    name: LOS - Loan Origination System
    description: Comprehensive Loan Origination System
    version: 1.0.0
    developer: LOS Development Team
'@

    "backend/src/main/resources/data.sql" = @'
-- LOS - Loan Origination System - Анхны өгөгдөл

-- Хэрэглэгчдийн дүрүүд (Roles)
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
(1, 'ADMIN', 'Системийн администратор', NOW(), NOW()),
(2, 'LOAN_OFFICER', 'Зээлийн ажилтан', NOW(), NOW()),
(3, 'MANAGER', 'Менежер', NOW(), NOW()),
(4, 'CUSTOMER', 'Харилцагч', NOW(), NOW());

-- Хэрэглэгчид (Users)
INSERT INTO users (id, username, password, email, first_name, last_name, enabled, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'admin@los.mn', 'Админ', 'Хэрэглэгч', true, NOW(), NOW()),
(2, 'loan_officer', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'loan@los.mn', 'Зээлийн', 'Ажилтан', true, NOW(), NOW()),
(3, 'manager', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'manager@los.mn', 'Менежер', 'Хэрэглэгч', true, NOW(), NOW());

-- Хэрэглэгчид болон дүрүүдийн холболт
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin -> ADMIN
(2, 2), -- loan_officer -> LOAN_OFFICER  
(3, 3); -- manager -> MANAGER

-- Жишээ харилцагчид
INSERT INTO customers (id, customer_type, first_name, last_name, register_number, phone, email, address, monthly_income, status, created_at, updated_at) VALUES
(1, 'INDIVIDUAL', 'Бат', 'Болд', 'УБ12345678', '+97611111111', 'bat.bold@email.com', 'Улаанбаатар хот, СБД, 1-р хороо', 1500000.00, 'ACTIVE', NOW(), NOW()),
(2, 'INDIVIDUAL', 'Сүх', 'Цэцэг', 'УБ87654321', '+97622222222', 'sukh.tsetseg@email.com', 'Улаанбаатар хот, ХУД, 5-р хороо', 2000000.00, 'ACTIVE', NOW(), NOW()),
(3, 'BUSINESS', 'ТТД', 'Компани', 'УБ99999999', '+97633333333', 'info@ttd.mn', 'Улаанбаатар хот, ЧД, 10-р хороо', 5000000.00, 'ACTIVE', NOW(), NOW());

-- Зээлийн төрлүүд
INSERT INTO loan_types (id, name, description, min_amount, max_amount, min_term_months, max_term_months, interest_rate, created_at, updated_at) VALUES
(1, 'CONSUMER', 'Хэрэглээний зээл', 100000, 5000000, 6, 60, 12.50, NOW(), NOW()),
(2, 'MORTGAGE', 'Орон сууцны зээл', 10000000, 500000000, 120, 360, 9.50, NOW(), NOW()),
(3, 'BUSINESS', 'Бизнесийн зээл', 500000, 100000000, 12, 120, 15.00, NOW(), NOW()),
(4, 'AUTO', 'Автомашины зээл', 1000000, 50000000, 12, 84, 11.00, NOW(), NOW());

-- Жишээ зээлийн хүсэлтүүд
INSERT INTO loan_applications (id, customer_id, loan_type, requested_amount, requested_term_months, declared_income, purpose, status, created_at, updated_at) VALUES
(1, 1, 'CONSUMER', 1000000, 24, 1500000, 'Гэр ахуйн зардал', 'PENDING', NOW(), NOW()),
(2, 2, 'AUTO', 15000000, 48, 2000000, 'Автомашин худалдан авах', 'APPROVED', NOW(), NOW()),
(3, 3, 'BUSINESS', 25000000, 36, 5000000, 'Бизнес өргөтгөх', 'IN_REVIEW', NOW(), NOW());

-- Жишээ баримт бичгүүд
INSERT INTO documents (id, loan_application_id, document_type, file_name, file_path, file_size, status, created_at, updated_at) VALUES
(1, 1, 'ID_CARD', 'irgens_bichig_1.pdf', '/documents/1/irgens_bichig_1.pdf', 1024000, 'VERIFIED', NOW(), NOW()),
(2, 1, 'INCOME_CERTIFICATE', 'orlogiin_gerchilgee_1.pdf', '/documents/1/orlogiin_gerchilgee_1.pdf', 2048000, 'PENDING', NOW(), NOW()),
(3, 2, 'ID_CARD', 'irgens_bichig_2.pdf', '/documents/2/irgens_bichig_2.pdf', 1536000, 'VERIFIED', NOW(), NOW());

-- Системийн тохиргоо
INSERT INTO system_settings (id, setting_key, setting_value, description, created_at, updated_at) VALUES
(1, 'MAX_LOAN_AMOUNT', '100000000', 'Хамгийн их зээлийн хэмжээ', NOW(), NOW()),
(2, 'MIN_CREDIT_SCORE', '600', 'Хамгийн бага зээлийн үнэлгээ', NOW(), NOW()),
(3, 'DEFAULT_INTEREST_RATE', '12.0', 'Стандарт хүүгийн хувь', NOW(), NOW()),
(4, 'SYSTEM_EMAIL', 'system@los.mn', 'Системийн и-мэйл хаяг', NOW(), NOW());
'@

    "frontend/package.json" = @'
{
  "name": "los-frontend",
  "version": "1.0.0",
  "description": "LOS - Loan Origination System Frontend",
  "type": "module",
  "scripts": {
    "dev": "vite --port 3001",
    "build": "tsc && vite build",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "preview": "vite preview",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.8.1",
    "axios": "^1.3.4",
    "antd": "^5.2.3",
    "@ant-design/icons": "^5.0.1",
    "dayjs": "^1.11.7",
    "recharts": "^2.5.0",
    "react-query": "^3.39.3",
    "zustand": "^4.3.6",
    "react-hook-form": "^7.43.5",
    "@hookform/resolvers": "^2.9.11",
    "yup": "^1.0.2"
  },
  "devDependencies": {
    "@types/react": "^18.0.28",
    "@types/react-dom": "^18.0.11",
    "@typescript-eslint/eslint-plugin": "^5.57.1",
    "@typescript-eslint/parser": "^5.57.1",
    "@vitejs/plugin-react": "^4.0.0",
    "eslint": "^8.38.0",
    "eslint-plugin-react-hooks": "^4.6.0",
    "eslint-plugin-react-refresh": "^0.3.4",
    "typescript": "^5.0.2",
    "vite": "^4.3.2",
    "vitest": "^0.29.8",
    "@testing-library/react": "^14.0.0",
    "@testing-library/jest-dom": "^5.16.5",
    "@testing-library/user-event": "^14.4.3",
    "jsdom": "^21.1.1"
  },
  "keywords": [
    "loan-origination-system",
    "los",
    "react",
    "typescript",
    "vite",
    "banking",
    "finance"
  ],
  "author": "LOS Development Team",
  "license": "MIT"
}
'@

    "frontend/src/main.tsx" = @'
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from 'react-query'
import { ConfigProvider } from 'antd'
import mnMN from 'antd/locale/mn_MN'
import App from './App.tsx'
import './styles/index.css'

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
})

// Ant Design theme configuration
const theme = {
  token: {
    colorPrimary: '#1890ff',
    borderRadius: 6,
    colorSuccess: '#52c41a',
    colorWarning: '#faad14',
    colorError: '#ff4d4f',
  },
  components: {
    Layout: {
      headerBg: '#001529',
      siderBg: '#001529',
    },
    Menu: {
      darkItemBg: '#001529',
      darkSubMenuItemBg: '#000c17',
    },
  },
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <ConfigProvider locale={mnMN} theme={theme}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </ConfigProvider>
    </QueryClientProvider>
  </React.StrictMode>,
)
'@

    "frontend/src/App.tsx" = @'
import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { Layout } from 'antd'
import { AuthProvider } from './contexts/AuthContext'
import MainLayout from './components/layout/MainLayout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import CustomerPage from './pages/CustomerPage'
import LoanApplicationPage from './pages/LoanApplicationPage'
import PrivateRoute from './security/PrivateRoute'
import './styles/index.css'

const { Content } = Layout

const App: React.FC = () => {
  return (
    <AuthProvider>
      <Layout style={{ minHeight: '100vh' }}>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<LoginPage />} />
          
          {/* Private routes */}
          <Route path="/" element={
            <PrivateRoute>
              <MainLayout />
            </PrivateRoute>
          }>
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="customers" element={<CustomerPage />} />
            <Route path="loan-applications" element={<LoanApplicationPage />} />
          </Route>
          
          {/* Catch all route */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Layout>
    </AuthProvider>
  )
}

export default App
'@

    "README.md" = @'
# 🏦 LOS - Loan Origination System

Зээлийн хүсэлтийн цогц систем - Comprehensive Loan Origination System

## 📋 Тойм (Overview)

LOS нь банк, санхүүгийн байгууллагын зээлийн хүсэлтийг эхнээс нь дуустал удирдах цогц систем юм. Энэ систем нь зээл авагчийн мэдээлэл, баримт бичиг, зээлийн үнэлгээ, шийдвэр гаргах зэрэг бүх үйл явцыг автоматжуулсан.

## 🌟 Гол онцлогууд

- 👥 **Харилцагчийн удирдлага** - KYC, мэдээллийн баталгаажуулалт
- 📄 **Зээлийн хүсэлт** - Олон төрлийн зээлийн хүсэлт үүсгэх, удирдах  
- 📋 **Баримт бичиг** - Автомат OCR, баталгаажуулалт
- ⚡ **Автомат шийдвэр** - Credit scoring, risk assessment
- 🔄 **Workflow** - BPMN-д тулгуурласан үйл явцын удирдлага
- 🔗 **Гадаад интеграци** - КТС, НДЕГ, банкны системтэй холболт
- 📊 **Тайлан** - Real-time analytics, dashboard
- 🔐 **Аюулгүй байдал** - JWT, RBAC, audit logging

## 🚀 Хурдан эхлэл (Quick Start)

### Шаардлагатай зүйлс

- **Java 17+**
- **Node.js 18+** 
- **Maven 3.6+**

### 1️⃣ Repository татах

```bash
git clone https://github.com/your-username/loan-origination-system.git
cd loan-origination-system
```

### 2️⃣ Backend эхлүүлэх

```bash
cd backend
./mvnw spring-boot:run
```

### 3️⃣ Frontend эхлүүлэх

```bash
cd frontend
npm install
npm run dev
```

### 4️⃣ Систем рүү нэвтрэх

- **Backend**: http://localhost:8080/los
- **Frontend**: http://localhost:3001
- **API Docs**: http://localhost:8080/los/swagger-ui.html
- **H2 Console**: http://localhost:8080/los/h2-console

#### Нэвтрэх мэдээлэл:
- **Админ**: admin / admin123
- **Зээлийн ажилтан**: loan_officer / loan123
- **Менежер**: manager / manager123

## 🏗️ Технологи стек

### Backend
- Spring Boot 3.2+
- Spring Security + JWT
- Spring Data JPA
- H2 Database (dev) / PostgreSQL (prod)
- Maven

### Frontend  
- React 18 + TypeScript
- Ant Design
- React Router
- React Query
- Vite

## 📁 Төслийн бүтэц

```
loan-origination-system/
├── backend/
│   ├── src/main/java/com/los/
│   │   ├── controller/     # REST API controllers
│   │   ├── service/        # Business logic
│   │   ├── entity/         # JPA entities
│   │   ├── repository/     # Data access
│   │   └── config/         # Configuration
│   └── src/main/resources/
├── frontend/
│   ├── src/
│   │   ├── components/     # React components
│   │   ├── pages/          # Page components
│   │   ├── services/       # API services
│   │   └── types/          # TypeScript types
│   └── package.json
└── README.md
```

## 🧪 Тест ажиллуулах

```bash
# Backend тест
cd backend && ./mvnw test

# Frontend тест
cd frontend && npm test
```

## 📚 Documentation

- [API Documentation](docs/API.md)
- [User Guide](docs/USER_GUIDE.md)
- [Development Guide](docs/DEVELOPMENT.md)

## 🤝 Contributing

1. Fork repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request

## 📝 License

MIT License - see [LICENSE](LICENSE) file for details.

---

**🎉 LOS системд тавтай морил!**
'@

    ".gitignore" = @'
# Compiled class files
*.class

# Log files
*.log

# Maven
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

# Node modules
node_modules/
npm-debug.log*
yarn-debug.log*
yarn-error.log*

# Build outputs
build/
dist/
*.tgz

# IDE files
.idea/
.vscode/
*.swp
*.swo
*~

# OS generated files
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db

# Environment files
.env
.env.local
.env.development.local
.env.test.local
.env.production.local

# Database files
*.db
*.sqlite

# Temporary files
*.tmp
*.temp

# Coverage reports
coverage/
*.lcov

# Logs
logs/
*.log

# Runtime data
pids/
*.pid
*.seed

# Application specific
uploads/
documents/
session-store.db
'@
}

Clear-Host

Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "🛠️ LOS ДУТУУ ФАЙЛУУД ҮҮСГЭГЧ - TEMPLATE GENERATOR" "Yellow"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "📅 Огноо: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "White"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText ""

Write-Log "File creation started"

# Дараалал тодорхойлох
$creationOrder = @(
    # Суурь директорууд
    "backend",
    "backend/src",
    "backend/src/main",
    "backend/src/main/java",
    "backend/src/main/java/com",
    "backend/src/main/java/com/los",
    "backend/src/main/resources",
    "backend/src/test",
    "backend/src/test/java",
    "frontend",
    "frontend/src",
    "docs",
    "scripts",
    
    # Суурь файлууд
    "backend/pom.xml",
    ".gitignore",
    "README.md",
    
    # Java пакежүүд
    "backend/src/main/java/com/los/common",
    "backend/src/main/java/com/los/entity",
    "backend/src/main/java/com/los/repository",
    "backend/src/main/java/com/los/service",
    "backend/src/main/java/com/los/controller",
    "backend/src/main/java/com/los/config",
    
    # Application файлууд
    "backend/src/main/java/com/los/LosApplication.java",
    "backend/src/main/java/com/los/common/BaseEntity.java",
    "backend/src/main/java/com/los/entity/Customer.java",
    "backend/src/main/resources/application.yml",
    "backend/src/main/resources/data.sql",
    
    # Frontend суурь
    "frontend/package.json",
    "frontend/src/main.tsx",
    "frontend/src/App.tsx"
)

function Create-FileWithTemplate {
    param($FilePath)
    
    # Директор үүсгэх
    $dir = Split-Path $FilePath -Parent
    if ($dir -and !(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        if (!$DryRun) {
            Write-ColoredText "   📁 Директор үүсгэв: $dir" "Blue"
        }
    }
    
    # Файл шалгах
    if (Test-Path $FilePath) {
        Write-ColoredText "   ⚠️  Файл аль хэдийн байна: $FilePath" "Yellow"
        return $false
    }
    
    # Template авах
    $template = $templates[$FilePath]
    
    if ($DryRun) {
        Write-ColoredText "   [DRY RUN] Үүсгэх байсан: $FilePath" "Gray"
        return $true
    }
    
    if ($template) {
        # Template ашиглан үүсгэх
        Set-Content -Path $FilePath -Value $template -Encoding UTF8
        Write-ColoredText "   ✅ Template файл үүсгэв: $FilePath" "Green"
        Write-Log "Created template file: $FilePath"
    } else {
        # Хоосон файл үүсгэх
        New-Item -ItemType File -Path $FilePath -Force | Out-Null
        Write-ColoredText "   📄 Хоосон файл үүсгэв: $FilePath" "White"
        Write-Log "Created empty file: $FilePath"
    }
    
    return $true
}

# Анхны шалгалт
if ($DryRun) {
    Write-ColoredText "🔍 DRY RUN MODE - Файлууд үүсгэхгүй, зөвхөн харуулна" "Yellow"
    Write-ColoredText ""
}

$createdCount = 0
$skippedCount = 0

# Долоо хоног сонгосон эсэх шалгах
if ($Week) {
    Write-ColoredText "📅 $Week долоо хоногийн файлуудыг үүсгэж байна..." "Blue"
    Write-ColoredText ""
    
    # TODO: Week-ийн файлуудыг үүсгэх код
} elseif ($CreateAll) {
    Write-ColoredText "📋 БҮХ ДУТУУ ФАЙЛУУДЫГ ҮҮСГЭЖ БАЙНА..." "Blue"
    Write-ColoredText ""
    
    # Дараалалд нь үүсгэх
    foreach ($item in $creationOrder) {
        if ($item -like "*.*") {
            # Файл
            if (Create-FileWithTemplate $item) {
                $createdCount++
            } else {
                $skippedCount++
            }
        } else {
            # Директор
            if (!(Test-Path $item)) {
                if (!$DryRun) {
                    New-Item -ItemType Directory -Path $item -Force | Out-Null
                    Write-ColoredText "   📁 Директор үүсгэв: $item" "Blue"
                } else {
                    Write-ColoredText "   [DRY RUN] Директор үүсгэх байсан: $item" "Gray"
                }
            }
        }
    }
    
    Write-ColoredText ""
    Write-ColoredText "🎯 ДАРААГИЙН АЛХМУУД:" "Green"
    Write-ColoredText "════════════════════" "Green"
    Write-ColoredText "   1. Backend entity файлуудыг (User.java, Role.java) гүйцээх" "White"
    Write-ColoredText "   2. Repository интерфейсүүдыг үүсгэх" "White"
    Write-ColoredText "   3. Service классуудыг implement хийх" "White"
    Write-ColoredText "   4. Controller файлуудыг үүсгэх" "White"
    Write-ColoredText "   5. Frontend компонентуудыг үүсгэх" "White"
    
} else {
    Write-ColoredText "💡 ФАЙЛ ҮҮСГЭХ КОМАНДУУД:" "Blue"
    Write-ColoredText "═══════════════════════" "Blue"
    Write-ColoredText "   Бүх файл үүсгэх:        .\LOS-Missing-Files-Creator.ps1 -CreateAll" "White"
    Write-ColoredText "   Dry run тест:           .\LOS-Missing-Files-Creator.ps1 -CreateAll -DryRun" "White"
    Write-ColoredText "   1-р долоо хоног:        .\LOS-Missing-Files-Creator.ps1 -Week 1" "White"
    Write-ColoredText ""
    Write-ColoredText "📋 ОДООГИЙН TEMPLATES:" "Green"
    Write-ColoredText "═══════════════════════" "Green"
    
    foreach ($template in $templates.Keys) {
        if (Test-Path $template) {
            Write-ColoredText "   ✅ $template" "Green"
        } else {
            Write-ColoredText "   ❌ $template" "Red"
        }
    }
}

Write-ColoredText ""
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"
Write-ColoredText "📊 ҮҮСГЭХ СТАТИСТИК:" "White"
Write-ColoredText "   ✅ Үүсгэсэн файл:       $createdCount" "Green"
Write-ColoredText "   ⚠️  Алгассан файл:       $skippedCount" "Yellow"
Write-ColoredText "   📋 Template байгаа:     $($templates.Count)" "Blue"
Write-ColoredText "═══════════════════════════════════════════════════════════════════" "Cyan"

if ($LogFile) {
    Write-ColoredText "📋 Лог файл: $LogFile" "Gray"
}

Write-Log "File creation completed. Created: $createdCount, Skipped: $skippedCount"

Write-ColoredText ""
Write-ColoredText "🚀 Дараагийн алхам: Backend эхлүүлэх: cd backend && .\mvnw.cmd spring-boot:run" "Yellow"
Write-ColoredText ""