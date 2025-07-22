-- =====================================================================================
-- LOAN ORIGINATION SYSTEM DATABASE SCHEMA
-- =====================================================================================
-- Created: 2025-07-22
-- Description: Database schema for Loan Origination System
-- =====================================================================================

-- Enable UUID extension for PostgreSQL
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================================================
-- ENUM TYPES
-- =====================================================================================

-- Customer Type Enum
CREATE TYPE customer_type AS ENUM ('INDIVIDUAL', 'BUSINESS');

-- Document Type Enum  
CREATE TYPE document_type AS ENUM (
    'NATIONAL_ID', 'PASSPORT', 'DRIVERS_LICENSE', 'SOCIAL_SECURITY_CARD',
    'INCOME_STATEMENT', 'BANK_STATEMENT', 'TAX_RETURN', 'EMPLOYMENT_VERIFICATION',
    'BUSINESS_LICENSE', 'TAX_STATEMENT', 'FINANCIAL_STATEMENT',
    'PROPERTY_DEED', 'VEHICLE_TITLE', 'INSURANCE_POLICY',
    'UTILITY_BILL', 'LEASE_AGREEMENT', 'MORTGAGE_STATEMENT',
    'OTHER'
);

-- Document Verification Status Enum
CREATE TYPE verification_status AS ENUM (
    'PENDING', 'IN_REVIEW', 'APPROVED', 'REJECTED', 
    'EXPIRED', 'RESUBMIT_REQUIRED', 'ON_HOLD'
);

-- Loan Status Enum
CREATE TYPE loan_status AS ENUM (
    'DRAFT', 'SUBMITTED', 'DOCUMENT_REVIEW', 'CREDIT_CHECK', 
    'RISK_ASSESSMENT', 'MANAGER_REVIEW', 'APPROVED', 'REJECTED', 
    'DISBURSED', 'CANCELLED', 'PENDING_INFO'
);

-- Loan Type Enum
CREATE TYPE loan_type AS ENUM (
    'PERSONAL', 'MORTGAGE', 'AUTO', 'BUSINESS', 'STUDENT', 'OTHER'
);

-- Permission Action Enum
CREATE TYPE permission_action AS ENUM (
    'CREATE', 'READ', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 
    'ASSIGN', 'EXPORT', 'IMPORT', 'SEARCH', 'PRINT', 'DOWNLOAD', 
    'UPLOAD', 'PROCESS', 'REVIEW', 'AUDIT'
);

-- Permission Category Enum
CREATE TYPE permission_category AS ENUM (
    'CUSTOMER_MANAGEMENT', 'LOAN_PROCESSING', 'DOCUMENT_MANAGEMENT', 
    'USER_MANAGEMENT', 'ROLE_MANAGEMENT', 'REPORTING', 
    'SYSTEM_ADMINISTRATION', 'FINANCIAL_OPERATIONS', 'COMPLIANCE', 'AUDIT'
);

-- =====================================================================================
-- CORE TABLES
-- =====================================================================================

-- Customers Table
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    customer_type customer_type NOT NULL,
    register_number VARCHAR(20) UNIQUE NOT NULL,
    
    -- Individual Customer Fields
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    middle_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(10),
    marital_status VARCHAR(20),
    
    -- Business Customer Fields
    company_name VARCHAR(200),
    business_type VARCHAR(100),
    establishment_date DATE,
    tax_number VARCHAR(50),
    
    -- Contact Information
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'Mongolia',
    
    -- Employment/Business Information
    employer_name VARCHAR(200),
    job_title VARCHAR(100),
    work_phone VARCHAR(20),
    work_address TEXT,
    monthly_income DECIMAL(15,2),
    employment_start_date DATE,
    
    -- Banking Information
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    
    -- KYC Status
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    kyc_completed_at TIMESTAMP,
    risk_rating VARCHAR(20) DEFAULT 'LOW',
    
    -- Internal Fields
    assigned_to VARCHAR(100),
    notes TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Loan Applications Table
CREATE TABLE loan_applications (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    application_number VARCHAR(50) UNIQUE NOT NULL,
    
    -- Loan Details
    loan_type loan_type NOT NULL,
    requested_amount DECIMAL(15,2) NOT NULL,
    requested_term_months INTEGER NOT NULL,
    purpose TEXT,
    
    -- Approved Details
    approved_amount DECIMAL(15,2),
    approved_term_months INTEGER,
    approved_rate DECIMAL(5,4),
    monthly_payment DECIMAL(15,2),
    
    -- Financial Information
    declared_income DECIMAL(15,2),
    debt_to_income_ratio DECIMAL(5,4),
    credit_score INTEGER,
    
    -- Status and Workflow
    status loan_status DEFAULT 'DRAFT',
    current_step VARCHAR(100),
    assigned_to VARCHAR(100),
    priority INTEGER DEFAULT 3,
    
    -- Decision Information
    decision_reason TEXT,
    decision_date TIMESTAMP,
    approved_by VARCHAR(100),
    approved_date TIMESTAMP,
    rejected_by VARCHAR(100),
    rejected_date TIMESTAMP,
    
    -- Disbursement
    disbursed_amount DECIMAL(15,2),
    disbursed_date TIMESTAMP,
    disbursed_by VARCHAR(100),
    
    -- Risk Assessment
    risk_score DECIMAL(5,2),
    risk_factors TEXT,
    
    -- Important Dates
    submitted_date TIMESTAMP,
    due_date TIMESTAMP,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Documents Table
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    loan_application_id UUID REFERENCES loan_applications(id),
    
    -- Document Information
    document_type document_type NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    stored_filename VARCHAR(500) NOT NULL,
    file_path TEXT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    checksum VARCHAR(256),
    
    -- Document Metadata
    description TEXT,
    tags VARCHAR(1000),
    version_number INTEGER DEFAULT 1,
    previous_document_id UUID REFERENCES documents(id),
    
    -- Verification
    verification_status verification_status DEFAULT 'PENDING',
    verified_by VARCHAR(100),
    verified_at TIMESTAMP,
    verification_notes TEXT,
    
    -- Expiry
    expiry_date DATE,
    
    -- Requirements
    is_required BOOLEAN DEFAULT FALSE,
    
    -- Processing
    processing_status VARCHAR(50),
    processing_error TEXT,
    ocr_text TEXT,
    extracted_data TEXT,
    ai_confidence_score DECIMAL(5,4),
    
    -- Upload Information
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(100),
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
);

-- =====================================================================================
-- RBAC (Role-Based Access Control) TABLES
-- =====================================================================================

-- Permissions Table
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    display_name_mn VARCHAR(100),
    description VARCHAR(500),
    
    -- Permission Details
    resource VARCHAR(50) NOT NULL,
    action permission_action NOT NULL,
    category permission_category NOT NULL,
    scope VARCHAR(20),
    
    -- System Properties
    is_system_permission BOOLEAN DEFAULT FALSE,
    priority INTEGER DEFAULT 5,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Roles Table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    display_name_mn VARCHAR(100),
    description VARCHAR(500),
    
    -- Role Properties
    is_system_role BOOLEAN DEFAULT FALSE,
    is_default BOOLEAN DEFAULT FALSE,
    level_order INTEGER DEFAULT 1,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Users Table (System Users)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    
    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    
    -- Employment Information
    employee_id VARCHAR(50),
    department VARCHAR(100),
    position VARCHAR(100),
    manager_id UUID REFERENCES users(id),
    
    -- Account Status
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INTEGER DEFAULT 0,
    last_login_at TIMESTAMP,
    password_expires_at TIMESTAMP,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
);

-- =====================================================================================
-- JUNCTION TABLES (Many-to-Many Relationships)
-- =====================================================================================

-- Role-Permission Junction Table
CREATE TABLE role_permissions (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted_by VARCHAR(100),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(role_id, permission_id)
);

-- User-Role Junction Table
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_by VARCHAR(100),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    
    UNIQUE(user_id, role_id)
);

-- =====================================================================================
-- AUDIT AND LOGGING TABLES
-- =====================================================================================

-- Audit Log Table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    table_name VARCHAR(100) NOT NULL,
    record_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT
);

-- Activity Log Table
CREATE TABLE activity_logs (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    user_id UUID REFERENCES users(id),
    activity_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    description TEXT,
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================================================
-- CONFIGURATION AND SETTINGS TABLES
-- =====================================================================================

-- System Settings Table
CREATE TABLE system_settings (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    data_type VARCHAR(20) DEFAULT 'STRING', -- STRING, INTEGER, DECIMAL, BOOLEAN, JSON
    category VARCHAR(100),
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- Loan Configuration Table
CREATE TABLE loan_configurations (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    loan_type loan_type NOT NULL,
    customer_type customer_type NOT NULL,
    
    -- Limits
    min_amount DECIMAL(15,2) NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,
    min_term_months INTEGER NOT NULL,
    max_term_months INTEGER NOT NULL,
    
    -- Rates
    base_interest_rate DECIMAL(5,4) NOT NULL,
    processing_fee_rate DECIMAL(5,4) DEFAULT 0,
    
    -- Requirements
    required_documents document_type[],
    required_credit_score INTEGER,
    max_debt_to_income_ratio DECIMAL(5,4),
    
    -- Workflow
    auto_approval_threshold DECIMAL(15,2),
    requires_collateral BOOLEAN DEFAULT FALSE,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    effective_from DATE NOT NULL,
    effective_to DATE,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- =====================================================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================================================

-- Customer Indexes
CREATE INDEX idx_customers_register_number ON customers(register_number);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_type ON customers(customer_type);
CREATE INDEX idx_customers_kyc_status ON customers(kyc_status);
CREATE INDEX idx_customers_is_active ON customers(is_active);

-- Loan Application Indexes
CREATE INDEX idx_loan_applications_customer_id ON loan_applications(customer_id);
CREATE INDEX idx_loan_applications_number ON loan_applications(application_number);
CREATE INDEX idx_loan_applications_status ON loan_applications(status);
CREATE INDEX idx_loan_applications_type ON loan_applications(loan_type);
CREATE INDEX idx_loan_applications_assigned_to ON loan_applications(assigned_to);
CREATE INDEX idx_loan_applications_submitted_date ON loan_applications(submitted_date);
CREATE INDEX idx_loan_applications_due_date ON loan_applications(due_date);

-- Document Indexes
CREATE INDEX idx_documents_customer_id ON documents(customer_id);
CREATE INDEX idx_documents_loan_application_id ON documents(loan_application_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_verification_status ON documents(verification_status);
CREATE INDEX idx_documents_uploaded_at ON documents(uploaded_at);
CREATE INDEX idx_documents_verified_by ON documents(verified_by);
CREATE INDEX idx_documents_checksum ON documents(checksum);

-- Permission Indexes
CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_action ON permissions(action);
CREATE INDEX idx_permissions_category ON permissions(category);

-- Role Indexes
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_is_active ON roles(is_active);

-- User Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_employee_id ON users(employee_id);
CREATE INDEX idx_users_is_active ON users(is_active);

-- Junction Table Indexes
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Audit Indexes
CREATE INDEX idx_audit_logs_table_name ON audit_logs(table_name);
CREATE INDEX idx_audit_logs_record_id ON audit_logs(record_id);
CREATE INDEX idx_audit_logs_changed_at ON audit_logs(changed_at);
CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at);

-- =====================================================================================
-- TRIGGERS FOR AUTOMATIC TIMESTAMP UPDATES
-- =====================================================================================

-- Function to update timestamp
-- CREATE OR REPLACE FUNCTION update_updated_at_column()
-- RETURNS TRIGGER AS $$
-- BEGIN
--    NEW.updated_at = CURRENT_TIMESTAMP;
--    RETURN NEW;
-- END;
-- $$ language 'plpgsql';

-- Triggers for all main tables
CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_loan_applications_updated_at BEFORE UPDATE ON loan_applications 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_documents_updated_at BEFORE UPDATE ON documents 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_permissions_updated_at BEFORE UPDATE ON permissions 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON roles 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================================================
-- INITIAL DATA INSERTION
-- =====================================================================================

-- Insert Default System Settings
INSERT INTO system_settings (setting_key, setting_value, data_type, category, description) VALUES
('system.name', 'Loan Origination System', 'STRING', 'GENERAL', 'System name'),
('system.version', '1.0.0', 'STRING', 'GENERAL', 'System version'),
('system.timezone', 'Asia/Ulaanbaatar', 'STRING', 'GENERAL', 'System timezone'),
('documents.max_file_size', '52428800', 'INTEGER', 'DOCUMENTS', 'Maximum file size in bytes (50MB)'),
('documents.allowed_types', 'pdf,jpg,jpeg,png,doc,docx,xls,xlsx', 'STRING', 'DOCUMENTS', 'Allowed file types'),
('loan.auto_approval_enabled', 'false', 'BOOLEAN', 'LOANS', 'Enable automatic loan approval'),
('notifications.email_enabled', 'true', 'BOOLEAN', 'NOTIFICATIONS', 'Enable email notifications');

-- Insert Default Permissions
INSERT INTO permissions (name, display_name, display_name_mn, resource, action, category, is_system_permission, priority) VALUES
('CUSTOMER_CREATE', 'Create Customer', 'Харилцагч үүсгэх', 'customer', 'CREATE', 'CUSTOMER_MANAGEMENT', true, 5),
('CUSTOMER_READ', 'View Customer', 'Харилцагч харах', 'customer', 'READ', 'CUSTOMER_MANAGEMENT', true, 3),
('CUSTOMER_UPDATE', 'Update Customer', 'Харилцагч засах', 'customer', 'UPDATE', 'CUSTOMER_MANAGEMENT', true, 5),
('CUSTOMER_DELETE', 'Delete Customer', 'Харилцагч устгах', 'customer', 'DELETE', 'CUSTOMER_MANAGEMENT', true, 8),
('LOAN_CREATE', 'Create Loan Application', 'Зээлийн хүсэлт үүсгэх', 'loan_application', 'CREATE', 'LOAN_PROCESSING', true, 5),
('LOAN_READ', 'View Loan Application', 'Зээлийн хүсэлт харах', 'loan_application', 'READ', 'LOAN_PROCESSING', true, 3),
('LOAN_UPDATE', 'Update Loan Application', 'Зээлийн хүсэлт засах', 'loan_application', 'UPDATE', 'LOAN_PROCESSING', true, 5),
('LOAN_APPROVE', 'Approve Loan', 'Зээл зөвшөөрөх', 'loan_application', 'APPROVE', 'LOAN_PROCESSING', true, 9),
('LOAN_REJECT', 'Reject Loan', 'Зээл татгалзах', 'loan_application', 'REJECT', 'LOAN_PROCESSING', true, 8),
('DOCUMENT_CREATE', 'Upload Document', 'Баримт илгээх', 'document', 'CREATE', 'DOCUMENT_MANAGEMENT', true, 3),
('DOCUMENT_READ', 'View Document', 'Баримт харах', 'document', 'READ', 'DOCUMENT_MANAGEMENT', true, 2),
('DOCUMENT_UPDATE', 'Update Document', 'Баримт засах', 'document', 'UPDATE', 'DOCUMENT_MANAGEMENT', true, 5),
('DOCUMENT_DELETE', 'Delete Document', 'Баримт устгах', 'document', 'DELETE', 'DOCUMENT_MANAGEMENT', true, 7),
('DOCUMENT_VERIFY', 'Verify Document', 'Баримт баталгаажуулах', 'document', 'APPROVE', 'DOCUMENT_MANAGEMENT', true, 8),
('USER_MANAGE', 'Manage Users', 'Хэрэглэгч удирдах', 'user', 'UPDATE', 'USER_MANAGEMENT', true, 9),
('ROLE_MANAGE', 'Manage Roles', 'Дүр удирдах', 'role', 'UPDATE', 'ROLE_MANAGEMENT', true, 10),
('SYSTEM_ADMIN', 'System Administration', 'Системийн удирдлага', 'system', 'UPDATE', 'SYSTEM_ADMINISTRATION', true, 10);

-- Insert Default Roles
INSERT INTO roles (name, display_name, display_name_mn, description, is_system_role, is_default, level_order) VALUES
('SUPER_ADMIN', 'Super Administrator', 'Супер админ', 'Full system access', true, false, 10),
('ADMIN', 'Administrator', 'Админ', 'System administration access', true, false, 9),
('LOAN_OFFICER', 'Loan Officer', 'Зээлийн мэргэжилтэн', 'Loan processing and management', true, true, 5),
('DOCUMENT_REVIEWER', 'Document Reviewer', 'Баримт хянагч', 'Document verification and review', true, false, 4),
('CUSTOMER_SERVICE', 'Customer Service', 'Харилцагчийн үйлчилгээ', 'Customer support and basic operations', true, false, 3),
('AUDITOR', 'Auditor', 'Аудитор', 'Audit and compliance monitoring', true, false, 6),
('MANAGER', 'Manager', 'Менежер', 'Management level access', true, false, 7),
('VIEWER', 'Viewer', 'Харагч', 'Read-only access', true, false, 1);

-- =====================================================================================
-- END OF SCHEMA
-- =====================================================================================