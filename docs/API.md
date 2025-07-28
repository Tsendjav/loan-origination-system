# LOS API Documentation

## Overview

The Loan Origination System (LOS) provides a comprehensive RESTful API for managing the complete loan application lifecycle. This API enables secure, efficient processing of loan applications from initial customer registration through final disbursement.

## Base URL

```
Production: https://api.los.company.com/los/api/v1
Staging: https://staging-api.los.company.com/los/api/v1
Development: http://localhost:8080/los/api/v1
```

## Authentication

The API uses JSON Web Tokens (JWT) for authentication. Include the token in the Authorization header:

```bash
Authorization: Bearer <your-jwt-token>
```

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "your-username",
  "password": "your-password"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "user": {
      "id": 1,
      "username": "admin",
      "firstName": "Admin",
      "lastName": "User",
      "roles": ["ADMIN"]
    }
  },
  "timestamp": "2025-07-28T10:30:00Z"
}
```

## Error Handling

The API returns standard HTTP status codes and structured error responses:

```json
{
  "success": false,
  "error": "VALIDATION_ERROR",
  "message": "Invalid input data",
  "timestamp": "2025-07-28T10:30:00Z",
  "path": "/api/v1/customers",
  "details": {
    "field": "email",
    "message": "Email format is invalid"
  }
}
```

### Status Codes

- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **400 Bad Request** - Invalid request data
- **401 Unauthorized** - Authentication required
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **409 Conflict** - Resource already exists
- **422 Unprocessable Entity** - Validation error
- **500 Internal Server Error** - Server error

## API Endpoints

### Authentication Endpoints

#### Login
```http
POST /auth/login
```

#### Logout
```http
POST /auth/logout
```

#### Refresh Token
```http
POST /auth/refresh
```

#### Get Current User Profile
```http
GET /auth/profile
```

---

### Customer Management

#### Get All Customers
```http
GET /customers?page=0&size=20&sort=lastName&direction=ASC
```

**Query Parameters:**
- `page` (int): Page number (0-based)
- `size` (int): Page size (max 100)
- `sort` (string): Sort field
- `direction` (string): ASC or DESC
- `query` (string): Search query
- `status` (string): Customer status filter
- `kycStatus` (string): KYC status filter

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "firstName": "Батбаяр",
        "lastName": "Болд",
        "email": "batbayar@email.com",
        "phone": "99119911",
        "dateOfBirth": "1990-01-15",
        "socialSecurityNumber": "УБ90011500",
        "customerType": "INDIVIDUAL",
        "status": "ACTIVE",
        "kycStatus": "COMPLETED",
        "riskLevel": "LOW",
        "registrationDate": "2025-01-15T10:30:00Z",
        "address": {
          "street": "1-р хороо, 15-р байр",
          "city": "Улаанбаатар",
          "state": "Улаанбаатар",
          "zipCode": "14200",
          "country": "Mongolia"
        }
      }
    ],
    "totalElements": 150,
    "totalPages": 8,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

#### Get Customer by ID
```http
GET /customers/{id}
```

#### Create Customer
```http
POST /customers
Content-Type: application/json

{
  "firstName": "Сарангэрэл",
  "lastName": "Батбаяр",
  "email": "sarangerel@email.com",
  "phone": "88228822",
  "dateOfBirth": "1992-03-20",
  "socialSecurityNumber": "УБ92032000",
  "customerType": "INDIVIDUAL",
  "preferredLanguage": "mn",
  "address": {
    "street": "2-р хороо, 25-р байр",
    "city": "Улаанбаатар",
    "state": "Улаанбаатар",
    "zipCode": "14210",
    "country": "Mongolia",
    "addressType": "PRIMARY"
  },
  "employmentInfo": {
    "employerName": "Tech Company LLC",
    "jobTitle": "Software Engineer",
    "employmentType": "FULL_TIME",
    "monthlyIncome": 2500000,
    "employmentStartDate": "2020-01-15"
  },
  "communicationPreferences": {
    "emailNotifications": true,
    "smsNotifications": true,
    "phoneNotifications": false,
    "marketingConsent": false,
    "preferredContactTime": "EVENING"
  }
}
```

#### Update Customer
```http
PUT /customers/{id}
```

#### Delete Customer
```http
DELETE /customers/{id}
```

#### Validate Customer Data
```http
POST /customers/validate
Content-Type: application/json

{
  "email": "test@email.com",
  "phone": "99119911",
  "socialSecurityNumber": "УБ90011500"
}
```

---

### Loan Application Management

#### Get All Loan Applications
```http
GET /loan-applications?page=0&size=20&status=PENDING_APPROVAL
```

**Query Parameters:**
- `page`, `size`, `sort`, `direction`: Pagination
- `customerId` (int): Filter by customer
- `status` (string): Application status
- `loanType` (string): Loan type
- `minAmount`, `maxAmount` (number): Amount range
- `applicationDateFrom`, `applicationDateTo` (string): Date range

#### Get Loan Application by ID
```http
GET /loan-applications/{id}
```

#### Create Loan Application
```http
POST /loan-applications
Content-Type: application/json

{
  "customerId": 1,
  "loanProductId": 2,
  "requestedAmount": 10000000,
  "loanTerm": 24,
  "purpose": "HOME_IMPROVEMENT",
  "notes": "Kitchen renovation project",
  "collateral": [
    {
      "type": "REAL_ESTATE",
      "description": "Apartment in UB center",
      "estimatedValue": 150000000,
      "condition": "GOOD"
    }
  ]
}
```

#### Update Loan Application
```http
PUT /loan-applications/{id}
```

#### Submit Loan Application
```http
POST /loan-applications/submit
Content-Type: application/json

{
  "applicationId": 1
}
```

#### Approve Loan Application
```http
POST /loan-applications/{id}/approve
Content-Type: application/json

{
  "approvedAmount": 9500000,
  "interestRate": 12.5,
  "loanTerm": 24,
  "conditions": [
    "Property insurance required",
    "Income verification within 30 days"
  ],
  "notes": "Approved with standard conditions"
}
```

#### Reject Loan Application
```http
POST /loan-applications/{id}/reject
Content-Type: application/json

{
  "reason": "Insufficient income verification",
  "notes": "Customer needs to provide additional income documentation"
}
```

#### Calculate Loan Payment
```http
POST /loan-applications/calculate
Content-Type: application/json

{
  "loanAmount": 10000000,
  "interestRate": 12.0,
  "loanTerm": 24,
  "paymentFrequency": "MONTHLY"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "monthlyPayment": 470736,
    "totalInterest": 2297664,
    "totalAmount": 12297664,
    "paymentSchedule": [
      {
        "paymentNumber": 1,
        "paymentDate": "2025-08-28",
        "principalAmount": 370736,
        "interestAmount": 100000,
        "totalPayment": 470736,
        "remainingBalance": 9629264
      }
    ]
  }
}
```

---

### Document Management

#### Upload Document
```http
POST /documents/upload
Content-Type: multipart/form-data

file: [binary file data]
customerId: 1
loanApplicationId: 1 (optional)
documentType: INCOME_PROOF
description: "Monthly salary certificate"
```

#### Get Document by ID
```http
GET /documents/{id}
```

#### Download Document
```http
GET /documents/{id}/download
```

#### Get Documents by Application
```http
GET /documents/application/{applicationId}
```

#### Delete Document
```http
DELETE /documents/{id}
```

---

### Loan Product Management

#### Get All Loan Products
```http
GET /loan-products
```

#### Get Active Loan Products
```http
GET /loan-products/active
```

#### Get Loan Product by ID
```http
GET /loan-products/{id}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Personal Loan Standard",
    "description": "Standard personal loan for individual customers",
    "productType": "PERSONAL",
    "minAmount": 500000,
    "maxAmount": 50000000,
    "minTerm": 6,
    "maxTerm": 60,
    "baseInterestRate": 12.0,
    "isActive": true,
    "eligibilityCriteria": {
      "minAge": 21,
      "maxAge": 65,
      "minIncome": 800000,
      "minCreditScore": 650,
      "maxDebtToIncomeRatio": 0.4,
      "requiredEmploymentType": ["FULL_TIME", "PART_TIME"],
      "citizenshipRequired": true
    },
    "requiredDocuments": [
      "IDENTITY_PROOF",
      "INCOME_PROOF",
      "EMPLOYMENT_LETTER"
    ],
    "processingFee": 50000,
    "earlyRepaymentPenalty": 0.02
  }
}
```

---

### User Management

#### Get All Users
```http
GET /users?page=0&size=20
```

#### Get User by ID
```http
GET /users/{id}
```

#### Create User
```http
POST /users
Content-Type: application/json

{
  "username": "loan_officer_1",
  "password": "SecurePass123!",
  "firstName": "Батбаяр",
  "lastName": "Лхагвасүрэн",
  "email": "batbayar.l@company.com",
  "roles": ["LOAN_OFFICER"],
  "isActive": true
}
```

#### Update User
```http
PUT /users/{id}
```

#### Delete User
```http
DELETE /users/{id}
```

---

### System & Health

#### Health Check
```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963170816,
        "free": 91943018496,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

#### System Information
```http
GET /actuator/info
```

#### Metrics
```http
GET /actuator/metrics
```

---

## Rate Limiting

The API implements rate limiting to ensure fair usage:

- **Anonymous requests**: 100 requests per hour
- **Authenticated requests**: 1000 requests per hour
- **Admin requests**: 5000 requests per hour

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1643723400
```

## Pagination

All list endpoints support pagination with the following parameters:

- `page`: Page number (0-based, default: 0)
- `size`: Page size (1-100, default: 20)
- `sort`: Sort field (default varies by endpoint)
- `direction`: Sort direction - ASC or DESC (default: ASC)

## Data Types

### Customer Status
- `ACTIVE` - Active customer
- `INACTIVE` - Inactive customer
- `SUSPENDED` - Suspended customer
- `PENDING_VERIFICATION` - Awaiting verification

### KYC Status
- `NOT_STARTED` - KYC not started
- `IN_PROGRESS` - KYC in progress
- `COMPLETED` - KYC completed
- `REJECTED` - KYC rejected
- `EXPIRED` - KYC expired

### Loan Status
- `DRAFT` - Draft application
- `SUBMITTED` - Submitted application
- `UNDER_REVIEW` - Under review
- `PENDING_DOCUMENTS` - Awaiting documents
- `CREDIT_CHECK` - Credit check in progress
- `RISK_ASSESSMENT` - Risk assessment
- `PENDING_APPROVAL` - Awaiting approval
- `APPROVED` - Approved
- `REJECTED` - Rejected
- `DISBURSED` - Funds disbursed
- `ACTIVE` - Active loan
- `CLOSED` - Loan closed
- `DEFAULTED` - Loan in default

### Document Types
- `IDENTITY_PROOF` - Identity verification
- `INCOME_PROOF` - Income verification
- `ADDRESS_PROOF` - Address verification
- `BANK_STATEMENTS` - Bank statements
- `EMPLOYMENT_LETTER` - Employment letter
- `TAX_RETURNS` - Tax returns
- `PROPERTY_DOCUMENTS` - Property documents
- `COLLATERAL_DOCUMENTS` - Collateral documents
- `OTHER` - Other documents

## SDKs and Libraries

### JavaScript/TypeScript
```bash
npm install @company/los-api-client
```

```javascript
import { LOSApiClient } from '@company/los-api-client';

const client = new LOSApiClient({
  baseURL: 'https://api.los.company.com/los/api/v1',
  apiKey: 'your-api-key'
});

// Get customers
const customers = await client.customers.getAll({
  page: 0,
  size: 20,
  status: 'ACTIVE'
});
```

### Python
```bash
pip install los-api-client
```

```python
from los_api_client import LOSClient

client = LOSClient(
    base_url='https://api.los.company.com/los/api/v1',
    api_key='your-api-key'
)

# Get customers
customers = client.customers.get_all(
    page=0,
    size=20,
    status='ACTIVE'
)
```

## Examples

### Complete Loan Application Flow

1. **Create Customer**
```bash
curl -X POST https://api.los.company.com/los/api/v1/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Батбаяр","lastName":"Болд",...}'
```

2. **Create Loan Application**
```bash
curl -X POST https://api.los.company.com/los/api/v1/loan-applications \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"loanProductId":2,...}'
```

3. **Upload Documents**
```bash
curl -X POST https://api.los.company.com/los/api/v1/documents/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@income_proof.pdf" \
  -F "customerId=1" \
  -F "documentType=INCOME_PROOF"
```

4. **Submit Application**
```bash
curl -X POST https://api.los.company.com/los/api/v1/loan-applications/submit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"applicationId":1}'
```

5. **Approve Application**
```bash
curl -X POST https://api.los.company.com/los/api/v1/loan-applications/1/approve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"approvedAmount":9500000,"interestRate":12.5,...}'
```

## Support

For API support and questions:

- **Email**: api-support@company.com
- **Documentation**: https://docs.los.company.com
- **Status Page**: https://status.los.company.com
- **GitHub Issues**: https://github.com/company/los-api/issues

## Changelog

### v1.0.0 (2025-07-28)
- Initial API release
- Customer management endpoints
- Loan application processing
- Document upload and management
- User authentication and authorization
- Health monitoring and metrics