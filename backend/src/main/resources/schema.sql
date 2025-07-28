-- =====================================================================================
-- LOAN ORIGINATION SYSTEM DATABASE SCHEMA - ЭЦСИЙН ХУВИЛБАР (ЗАСВАРЛАСАН)
-- =====================================================================================
-- Created: 2025-07-27
-- Version: 3.0
-- Database: H2 Database
-- Description: Complete database schema for Loan Origination System with improvements
-- Author: LOS Development Team
-- =====================================================================================

-- Performance optimization
SET DB_CLOSE_DELAY -1;
SET REFERENTIAL_INTEGRITY FALSE;

-- =====================================================================================
-- DROP EXISTING TABLES (Зөв дараалалтайгаар)
-- =====================================================================================
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS activity_logs CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS documents CASCADE;
DROP TABLE IF EXISTS loan_applications CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS loan_products CASCADE;
DROP TABLE IF EXISTS document_types CASCADE;
DROP TABLE IF EXISTS system_settings CASCADE;
DROP TABLE IF EXISTS system_configs CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- =====================================================================================
-- CORE TABLES
-- =====================================================================================

-- 1. ROLES TABLE
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    display_name_mn VARCHAR(150),
    description VARCHAR(500),
    code VARCHAR(50) UNIQUE,
    
    -- Role Properties
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DEPRECATED')),
    type VARCHAR(20) NOT NULL DEFAULT 'BUSINESS' CHECK (type IN ('SYSTEM', 'BUSINESS', 'FUNCTIONAL', 'TEMPORARY')),
    priority INTEGER DEFAULT 50 CHECK (priority >= 1 AND priority <= 100),
    level_order INTEGER DEFAULT 1 CHECK (level_order >= 1),
    is_system_role BOOLEAN DEFAULT FALSE,
    is_default BOOLEAN DEFAULT FALSE,
    parent_role_id VARCHAR(36),
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Foreign Key
    FOREIGN KEY (parent_role_id) REFERENCES roles(id)
);

-- 2. PERMISSIONS TABLE
CREATE TABLE permissions (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    display_name_mn VARCHAR(150),
    description VARCHAR(500),
    
    -- Permission Details
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(30) NOT NULL CHECK (action IN ('CREATE', 'READ', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'EXPORT', 'IMPORT', 'CONFIG')),
    category VARCHAR(50) NOT NULL,
    scope VARCHAR(30) DEFAULT 'GLOBAL' CHECK (scope IN ('GLOBAL', 'DEPARTMENT', 'TEAM', 'PERSONAL')),
    
    -- System Properties
    is_system_permission BOOLEAN NOT NULL DEFAULT FALSE,
    priority INTEGER NOT NULL DEFAULT 5 CHECK (priority >= 1 AND priority <= 10),
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. CUSTOMERS TABLE
CREATE TABLE customers (
    id VARCHAR(36) PRIMARY KEY,
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'BUSINESS')),
    register_number VARCHAR(20) UNIQUE NOT NULL,
    
    -- Individual Customer Fields
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    middle_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    marital_status VARCHAR(20) CHECK (marital_status IN ('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED')),
    
    -- Business Customer Fields
    company_name VARCHAR(200),
    business_type VARCHAR(100),
    establishment_date DATE,
    tax_number VARCHAR(50),
    business_registration_number VARCHAR(50),
    annual_revenue DECIMAL(18,2),
    
    -- Contact Information
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    province VARCHAR(100),
    zip_code VARCHAR(20),
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'Mongolia',
    
    -- Employment/Business Information
    employer_name VARCHAR(200),
    job_title VARCHAR(100),
    work_phone VARCHAR(20),
    work_address TEXT,
    monthly_income DECIMAL(15,2),
    employment_start_date DATE,
    work_experience_years INTEGER CHECK (work_experience_years >= 0),
    
    -- Banking Information
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    
    -- KYC and Risk
    kyc_status VARCHAR(20) DEFAULT 'PENDING' CHECK (kyc_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'FAILED')),
    kyc_completed_at TIMESTAMP,
    kyc_verified_by VARCHAR(100),
    risk_rating VARCHAR(20) DEFAULT 'LOW' CHECK (risk_rating IN ('LOW', 'MEDIUM', 'HIGH')),
    
    -- Internal Fields
    assigned_to VARCHAR(100),
    notes TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4. LOAN PRODUCTS TABLE
CREATE TABLE loan_products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    product_name VARCHAR(200),
    loan_type VARCHAR(50) NOT NULL CHECK (loan_type IN ('PERSONAL', 'BUSINESS', 'MORTGAGE', 'CAR', 'EDUCATION', 'MEDICAL', 'CONSUMER')),
    
    -- Amount and Term Limits
    min_amount DECIMAL(18,2) NOT NULL CHECK (min_amount > 0),
    max_amount DECIMAL(18,2) NOT NULL CHECK (max_amount > 0),
    min_term_months INTEGER NOT NULL CHECK (min_term_months >= 1),
    max_term_months INTEGER NOT NULL CHECK (max_term_months >= 1),
    
    -- Interest Rates and Fees
    base_rate DECIMAL(7,4),
    default_interest_rate DECIMAL(7,4),
    min_interest_rate DECIMAL(7,4),
    max_interest_rate DECIMAL(7,4),
    processing_fee DECIMAL(15,2),
    processing_fee_rate DECIMAL(7,4),
    early_payment_penalty DECIMAL(15,2),
    early_payment_penalty_rate DECIMAL(7,4),
    late_payment_penalty DECIMAL(15,2),
    late_payment_penalty_rate DECIMAL(7,4),
    
    -- Requirements
    min_credit_score INTEGER CHECK (min_credit_score >= 300 AND min_credit_score <= 850),
    min_income DECIMAL(15,2),
    max_debt_ratio DECIMAL(7,4),
    requires_collateral BOOLEAN DEFAULT FALSE,
    requires_guarantor BOOLEAN DEFAULT FALSE,
    
    -- Approval Settings
    approval_required BOOLEAN DEFAULT TRUE,
    auto_approval_limit DECIMAL(18,2),
    
    -- Display and Marketing
    display_order INTEGER DEFAULT 0 CHECK (display_order >= 0),
    is_featured BOOLEAN DEFAULT FALSE,
    marketing_message VARCHAR(1000),
    
    -- Content
    description TEXT,
    required_documents TEXT,
    special_conditions TEXT,
    terms_and_conditions TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Constraints
    CONSTRAINT chk_loan_product_amounts CHECK (max_amount >= min_amount),
    CONSTRAINT chk_loan_product_terms CHECK (max_term_months >= min_term_months)
);

-- 5. DOCUMENT TYPES TABLE
CREATE TABLE document_types (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50),
    file_types VARCHAR(200), -- JSON array of allowed file types
    max_file_size BIGINT DEFAULT 10485760, -- 10MB default
    is_required BOOLEAN DEFAULT FALSE,
    retention_days INTEGER DEFAULT 2555, -- 7 years
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 6. USERS TABLE
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    
    -- Personal Information
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    
    -- Employment Information
    employee_id VARCHAR(50) UNIQUE,
    department VARCHAR(100),
    position VARCHAR(100),
    manager_id VARCHAR(36),
    
    -- Account Status and Security
    status VARCHAR(30) DEFAULT 'PENDING_ACTIVATION' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'LOCKED', 'EXPIRED', 'PENDING_APPROVAL', 'PENDING_ACTIVATION')),
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INTEGER DEFAULT 0 CHECK (failed_login_attempts >= 0),
    max_failed_attempts INTEGER DEFAULT 5,
    lock_duration_minutes INTEGER DEFAULT 30,
    
    -- Important Timestamps
    last_login_at TIMESTAMP,
    password_expires_at TIMESTAMP,
    password_changed_at TIMESTAMP,
    locked_until TIMESTAMP,
    
    -- User Preferences
    language VARCHAR(10) DEFAULT 'mn',
    timezone VARCHAR(50) DEFAULT 'Asia/Ulaanbaatar',
    profile_picture_url VARCHAR(500),
    
    -- Two-Factor Authentication
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    backup_codes TEXT, -- JSON array of backup codes
    
    -- Session Management
    current_session_id VARCHAR(255),
    session_expires_at TIMESTAMP,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Foreign Key
    FOREIGN KEY (manager_id) REFERENCES users(id)
);

-- 7. LOAN APPLICATIONS TABLE
CREATE TABLE loan_applications (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    loan_product_id VARCHAR(36) NOT NULL,
    application_number VARCHAR(50) UNIQUE NOT NULL,
    loan_type VARCHAR(30) NOT NULL CHECK (loan_type IN ('PERSONAL', 'BUSINESS', 'MORTGAGE', 'CAR', 'EDUCATION', 'MEDICAL', 'CONSUMER')),
    
    -- Loan Details
    requested_amount DECIMAL(18,2) NOT NULL CHECK (requested_amount > 0),
    requested_term_months INTEGER NOT NULL CHECK (requested_term_months >= 1 AND requested_term_months <= 360),
    purpose TEXT,
    
    -- Approved Details
    approved_amount DECIMAL(18,2),
    approved_term_months INTEGER CHECK (approved_term_months >= 1),
    approved_rate DECIMAL(7,4),
    monthly_payment DECIMAL(15,2),
    interest_rate DECIMAL(7,4),
    total_payment DECIMAL(18,2),
    description VARCHAR(1000),
    
    -- Financial Information
    declared_income DECIMAL(15,2),
    debt_to_income_ratio DECIMAL(7,4),
    credit_score INTEGER CHECK (credit_score >= 300 AND credit_score <= 850),
    
    -- Status and Workflow
    status VARCHAR(30) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'PENDING', 'PENDING_DOCUMENTS', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED', 'DISBURSED', 'EXPIRED')),
    current_step VARCHAR(100),
    assigned_to VARCHAR(100),
    priority INTEGER DEFAULT 3 CHECK (priority >= 1 AND priority <= 5),
    
    -- Decision Information
    decision_reason TEXT,
    decision_date TIMESTAMP,
    approved_by VARCHAR(100),
    approved_date TIMESTAMP,
    rejected_by VARCHAR(100),
    rejected_date TIMESTAMP,
    
    -- Processing Timestamps
    submitted_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    disbursed_at TIMESTAMP,
    expires_at TIMESTAMP,
    
    -- Review Information
    reviewed_by VARCHAR(100),
    rejection_reason TEXT,
    reviewer_notes TEXT,
    
    -- Disbursement
    disbursed_amount DECIMAL(18,2),
    disbursed_date TIMESTAMP,
    disbursed_by VARCHAR(100),
    
    -- Risk Assessment
    risk_score DECIMAL(5,2),
    risk_factors TEXT,
    
    -- Additional Fields
    requires_collateral BOOLEAN DEFAULT FALSE,
    requires_guarantor BOOLEAN DEFAULT FALSE,
    expected_disbursement_date DATE,
    processing_fee DECIMAL(15,2),
    other_charges DECIMAL(15,2),
    contract_terms TEXT,
    special_conditions TEXT,
    
    -- SLA and Important Dates
    due_date TIMESTAMP,
    sla_deadline TIMESTAMP,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Foreign Keys
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (loan_product_id) REFERENCES loan_products(id)
);

-- 8. DOCUMENTS TABLE
CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    loan_application_id VARCHAR(36),
    document_type_id VARCHAR(36) NOT NULL,
    
    -- Document Information
    file_name VARCHAR(500) NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    stored_filename VARCHAR(500) NOT NULL,
    file_path TEXT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size >= 1),
    checksum VARCHAR(256),
    
    -- Document Metadata
    description TEXT,
    tags TEXT, -- JSON array
    version_number INTEGER DEFAULT 1 CHECK (version_number >= 1),
    previous_document_id VARCHAR(36),
    
    -- Verification
    verification_status VARCHAR(30) DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'EXPIRED', 'RESUBMIT_REQUIRED', 'ON_HOLD')),
    verified_by VARCHAR(100),
    verified_at TIMESTAMP,
    verification_notes TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    
    -- Expiry and Requirements
    expiry_date DATE,
    is_required BOOLEAN DEFAULT FALSE,
    
    -- AI/OCR Processing
    processing_status VARCHAR(50),
    processing_error TEXT,
    ocr_text TEXT,
    extracted_data TEXT, -- JSON
    ai_confidence_score DECIMAL(5,4),
    
    -- Upload Information
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(100),
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Foreign Keys
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id),
    FOREIGN KEY (document_type_id) REFERENCES document_types(id),
    FOREIGN KEY (previous_document_id) REFERENCES documents(id)
);

-- =====================================================================================
-- LOGGING AND AUDIT TABLES
-- =====================================================================================

-- 9. ACTIVITY LOGS TABLE
CREATE TABLE activity_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    activity_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(36),
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(255),
    details TEXT, -- JSON
    severity VARCHAR(20) DEFAULT 'INFO' CHECK (severity IN ('DEBUG', 'INFO', 'WARN', 'ERROR', 'CRITICAL')),
    source VARCHAR(50) DEFAULT 'WEB',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 10. AUDIT LOGS TABLE
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    record_id VARCHAR(36) NOT NULL,
    action VARCHAR(20) NOT NULL CHECK (action IN ('INSERT', 'UPDATE', 'DELETE', 'SELECT')),
    old_values TEXT, -- JSON
    new_values TEXT, -- JSON
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    session_id VARCHAR(255),
    change_reason VARCHAR(500)
);

-- =====================================================================================
-- JUNCTION TABLES (Many-to-Many Relationships)
-- =====================================================================================

-- 11. USER-ROLE JUNCTION TABLE
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    expires_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 12. ROLE-PERMISSION JUNCTION TABLE
CREATE TABLE role_permissions (
    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- =====================================================================================
-- CONFIGURATION TABLES
-- =====================================================================================

-- 13. SYSTEM SETTINGS TABLE
CREATE TABLE system_settings (
    id VARCHAR(36) PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    data_type VARCHAR(20) DEFAULT 'STRING' CHECK (data_type IN ('STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'JSON', 'DATE', 'DATETIME')),
    category VARCHAR(100),
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_runtime_editable BOOLEAN DEFAULT TRUE,
    validation_pattern VARCHAR(200),
    default_value TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- 14. SYSTEM CONFIGS TABLE
CREATE TABLE system_configs (
    id VARCHAR(255) PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value VARCHAR(2000) NOT NULL,
    default_value VARCHAR(2000),
    value_type VARCHAR(20) NOT NULL CHECK (value_type IN ('STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'JSON')),
    category VARCHAR(50),
    description VARCHAR(500),
    environment VARCHAR(20) DEFAULT 'ALL',
    validation_pattern VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    is_runtime_editable BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    version BIGINT DEFAULT 0,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- =====================================================================================
-- PERFORMANCE INDEXES
-- =====================================================================================

-- Roles indexes
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_code ON roles(code);
CREATE INDEX idx_roles_type ON roles(type);
CREATE INDEX idx_roles_status ON roles(status);
CREATE INDEX idx_roles_active ON roles(is_active);

-- Permissions indexes
CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_action ON permissions(action);
CREATE INDEX idx_permissions_category ON permissions(category);
CREATE INDEX idx_permissions_resource_action ON permissions(resource, action);

-- Customers indexes
CREATE INDEX idx_customers_register_number ON customers(register_number);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_type ON customers(customer_type);
CREATE INDEX idx_customers_kyc_status ON customers(kyc_status);
CREATE INDEX idx_customers_assigned_to ON customers(assigned_to);

-- Users indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_employee_id ON users(employee_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_department ON users(department);
CREATE INDEX idx_users_last_login ON users(last_login_at);

-- Loan Applications indexes
CREATE INDEX idx_loan_apps_customer_id ON loan_applications(customer_id);
CREATE INDEX idx_loan_apps_product_id ON loan_applications(loan_product_id);
CREATE INDEX idx_loan_apps_status ON loan_applications(status);
CREATE INDEX idx_loan_apps_number ON loan_applications(application_number);
CREATE INDEX idx_loan_apps_assigned_to ON loan_applications(assigned_to);
CREATE INDEX idx_loan_apps_created_at ON loan_applications(created_at);
CREATE INDEX idx_loan_apps_priority ON loan_applications(priority);

-- Documents indexes
CREATE INDEX idx_documents_customer_id ON documents(customer_id);
CREATE INDEX idx_documents_loan_app_id ON documents(loan_application_id);
CREATE INDEX idx_documents_type_id ON documents(document_type_id);
CREATE INDEX idx_documents_verification_status ON documents(verification_status);
CREATE INDEX idx_documents_uploaded_at ON documents(uploaded_at);

-- Activity Logs indexes
CREATE INDEX idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_type ON activity_logs(activity_type);
CREATE INDEX idx_activity_logs_entity ON activity_logs(entity_type, entity_id);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at);

-- Audit Logs indexes
CREATE INDEX idx_audit_logs_table_record ON audit_logs(table_name, record_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_changed_by ON audit_logs(changed_by);
CREATE INDEX idx_audit_logs_changed_at ON audit_logs(changed_at);

-- Junction table indexes
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- System tables indexes
CREATE INDEX idx_system_settings_category ON system_settings(category);
CREATE INDEX idx_system_configs_category ON system_configs(category);
CREATE INDEX idx_system_configs_active ON system_configs(is_active);

-- =====================================================================================
-- TRIGGERS AND FUNCTIONS (H2 Compatible)
-- =====================================================================================

-- Note: H2 doesn't support complex triggers like PostgreSQL
-- Updated_at triggers should be handled in application layer

-- =====================================================================================
-- ENABLE FOREIGN KEY CONSTRAINTS
-- =====================================================================================
SET REFERENTIAL_INTEGRITY TRUE;

-- =====================================================================================
-- SCHEMA CREATION COMPLETE
-- =====================================================================================
-- Total Tables: 14
-- Junction Tables: 2  
-- Total Indexes: 35+
-- Security: RBAC with comprehensive permissions
-- Audit: Complete activity and change logging
-- Performance: Optimized indexes for common queries
-- =====================================================================================