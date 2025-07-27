-- =====================================================================================
-- LOAN ORIGINATION SYSTEM DATABASE SCHEMA - ЭЦСИЙН ХУВИЛБАР (ЗАСВАРЛАСАН)
-- =====================================================================================
-- Created: 2025-07-22
-- Updated: 2025-07-26 (БҮРЭН ЗАСВАРЛАСАН - activity_logs, audit_logs, system_configs нэмэгдсэн)
-- Description: Database schema for Loan Origination System (H2 Database)
-- =====================================================================================

-- Note: H2 Database UUID support and varchar compatibility
-- Note: ENUM types replaced with VARCHAR for H2 compatibility.

-- Foreign key шалгалтыг түр унтраана
SET REFERENTIAL_INTEGRITY FALSE;

-- =====================================================================================
-- CORE TABLES - Хүснэгтүүдийг устгана (хамаарлын дагуу зөв дараалалтайгаар)
-- =====================================================================================
-- ЗӨВТ ДАРААЛАЛ: Dependency-г дагаж устгана
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS activity_logs;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS documents;
DROP TABLE IF EXISTS loan_applications;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS loan_products;
DROP TABLE IF EXISTS document_types;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS system_configs;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;

-- =====================================================================================
-- CORE TABLES - Хүснэгтүүдийг үүсгэнэ
-- =====================================================================================

-- Roles Table (эхлээд dependencies-гүй хүснэгтүүдийг үүсгэнэ)
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY,
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

-- Permissions Table
CREATE TABLE permissions (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    display_name_mn VARCHAR(100),
    description VARCHAR(500),
    
    -- Permission Details
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL, -- 'CREATE', 'read', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT'
    category VARCHAR(50) NOT NULL, -- 'CUSTOMER_MANAGEMENT', 'LOAN_PROCESSING', 'DOCUMENT_MANAGEMENT', etc.
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

-- Customers Table
CREATE TABLE customers (
    id VARCHAR(36) PRIMARY KEY,
    customer_type VARCHAR(20) NOT NULL, -- 'INDIVIDUAL', 'BUSINESS'
    register_number VARCHAR(20) UNIQUE NOT NULL,
    
    -- Individual Customer Fields
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    middle_name VARCHAR(100),
    date_of_birth DATE,
    gender VARCHAR(10), -- 'MALE', 'FEMALE', 'OTHER'
    marital_status VARCHAR(20), -- 'SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED'
    
    -- Business Customer Fields
    company_name VARCHAR(200),
    business_type VARCHAR(100),
    establishment_date DATE,
    tax_number VARCHAR(50),
    business_registration_number VARCHAR(50),
    annual_revenue DECIMAL(15,2),
    
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
    work_experience_years INTEGER,
    
    -- Banking Information
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    
    -- KYC Status
    kyc_status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'FAILED'
    kyc_completed_at TIMESTAMP,
    kyc_verified_by VARCHAR(100),
    risk_rating VARCHAR(20) DEFAULT 'LOW', -- 'LOW', 'MEDIUM', 'HIGH'
    
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

-- Loan Products Table
CREATE TABLE loan_products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    product_name VARCHAR(200),
    loan_type VARCHAR(50) NOT NULL, -- 'PERSONAL','BUSINESS','MORTGAGE','CAR','EDUCATION','MEDICAL'
    min_amount DECIMAL(15,2) NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,
    min_term_months INTEGER NOT NULL CHECK (min_term_months >= 1),
    max_term_months INTEGER NOT NULL CHECK (max_term_months >= 1),
    
    -- Interest Rates and Fees
    base_rate DECIMAL(7,4),
    default_interest_rate DECIMAL(5,4),
    min_interest_rate DECIMAL(5,4),
    max_interest_rate DECIMAL(5,4),
    processing_fee DECIMAL(15,2),
    processing_fee_rate DECIMAL(5,4),
    early_payment_penalty DECIMAL(15,2),
    early_payment_penalty_rate DECIMAL(5,4),
    late_payment_penalty DECIMAL(15,2),
    late_payment_penalty_rate DECIMAL(5,4),
    
    -- Requirements
    min_credit_score INTEGER CHECK (min_credit_score >= 300 AND min_credit_score <= 850),
    min_income DECIMAL(15,2),
    max_debt_ratio DECIMAL(5,4),
    requires_collateral BOOLEAN DEFAULT FALSE,
    requires_guarantor BOOLEAN DEFAULT FALSE,
    
    -- Approval Settings
    approval_required BOOLEAN DEFAULT TRUE,
    auto_approval_limit DECIMAL(15,2),
    
    -- Display and Marketing
    display_order INTEGER CHECK (display_order >= 0),
    is_featured BOOLEAN DEFAULT FALSE,
    marketing_message VARCHAR(1000),
    loan_types VARCHAR(500),
    
    -- Content
    description TEXT,
    required_documents TEXT,
    special_conditions TEXT,
    terms_and_conditions TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Document Types Table
CREATE TABLE document_types (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_required BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE
);

-- Users Table (System Users) - HIBERNATE Entity-тэй тохирсон
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
    employee_id VARCHAR(50),
    department VARCHAR(100),
    position VARCHAR(100),
    manager_id VARCHAR(36),
    
    -- Account Status and Security
    status VARCHAR(30) DEFAULT 'PENDING_ACTIVATION', -- UserStatus enum
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INTEGER DEFAULT 0,
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
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    loan_product_id VARCHAR(36) NOT NULL,
    application_number VARCHAR(50) UNIQUE NOT NULL,
    loan_type VARCHAR(20) NOT NULL, -- 'PERSONAL', 'BUSINESS', 'MORTGAGE', 'CAR_LOAN', 'CONSUMER', 'EDUCATION', 'MEDICAL'
    
    -- Loan Details
    requested_amount DECIMAL(15,2) NOT NULL,
    requested_term_months INTEGER NOT NULL CHECK (requested_term_months >= 1 AND requested_term_months <= 360),
    purpose TEXT,
    
    -- Approved Details
    approved_amount DECIMAL(15,2),
    approved_term_months INTEGER CHECK (approved_term_months >= 1),
    approved_rate DECIMAL(5,4),
    monthly_payment DECIMAL(15,2),
    interest_rate DECIMAL(5,4),
    total_payment DECIMAL(15,2),
    description VARCHAR(1000),
    
    -- Financial Information
    declared_income DECIMAL(15,2),
    debt_to_income_ratio DECIMAL(5,4),
    credit_score INTEGER CHECK (credit_score >= 300 AND credit_score <= 850),
    
    -- Status and Workflow
    status VARCHAR(20) DEFAULT 'DRAFT', -- 'DRAFT','SUBMITTED','PENDING','PENDING_DOCUMENTS','UNDER_REVIEW','APPROVED','REJECTED','CANCELLED','DISBURSED'
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
    
    -- Processing dates
    submitted_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    disbursed_at TIMESTAMP,
    
    -- Review information
    reviewed_by VARCHAR(100),
    rejection_reason TEXT,
    reviewer_notes TEXT,
    
    -- Disbursement
    disbursed_amount DECIMAL(15,2),
    disbursed_date TIMESTAMP,
    disbursed_by VARCHAR(100),
    
    -- Risk Assessment
    risk_score DECIMAL(5,2),
    risk_factors TEXT,
    
    -- Additional fields
    requires_collateral BOOLEAN DEFAULT FALSE,
    requires_guarantor BOOLEAN DEFAULT FALSE,
    expected_disbursement_date DATE,
    processing_fee DECIMAL(15,2),
    other_charges DECIMAL(15,2),
    contract_terms VARCHAR(500),
    special_conditions VARCHAR(500),
    
    -- Important Dates
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
    tags VARCHAR(1000),
    version_number INTEGER DEFAULT 1 CHECK (version_number >= 1),
    previous_document_id VARCHAR(36),
    
    -- Verification
    verification_status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING','IN_REVIEW','APPROVED','REJECTED','EXPIRED','RESUBMIT_REQUIRED','ON_HOLD'
    verified_by VARCHAR(100),
    verified_at TIMESTAMP,
    verification_notes TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    
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
-- LOGGING TABLES - HIBERNATE ENTITY-УУДДАА ТОХИРСОН
-- =====================================================================================

-- Activity Logs Table (Hibernate Entity-д байгаа)
CREATE TABLE activity_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    activity_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(36),
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    session_id VARCHAR(255),
    details TEXT
);

-- Audit Logs Table (Hibernate Entity-д байгаа)
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    record_id VARCHAR(36) NOT NULL,
    action VARCHAR(20) NOT NULL, -- 'INSERT', 'UPDATE', 'DELETE'
    old_values TEXT,
    new_values TEXT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

-- =====================================================================================
-- JUNCTION TABLES (Many-to-Many Relationships)
-- =====================================================================================

-- Role-Permission Junction Table
CREATE TABLE role_permissions (
    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

-- User-Role Junction Table
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- =====================================================================================
-- CONFIGURATION AND SETTINGS TABLES
-- =====================================================================================

-- System Settings Table
CREATE TABLE system_settings (
    id VARCHAR(36) PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    data_type VARCHAR(20) DEFAULT 'STRING', -- 'STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'JSON'
    category VARCHAR(100),
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

-- System Configs Table (Hibernate Entity-д байгаа)
CREATE TABLE system_configs (
    id VARCHAR(255) PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value VARCHAR(1000) NOT NULL,
    default_value VARCHAR(1000),
    value_type VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    description VARCHAR(500),
    environment VARCHAR(20),
    validation_pattern VARCHAR(200),
    is_active BOOLEAN DEFAULT TRUE,
    is_runtime_editable BOOLEAN DEFAULT TRUE,
    sort_order INTEGER,
    version BIGINT DEFAULT 0,
    
    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- =====================================================================================
-- FOREIGN KEY CONSTRAINTS
-- =====================================================================================

-- Users foreign key (self-referencing)
ALTER TABLE users ADD CONSTRAINT fk_user_manager 
    FOREIGN KEY (manager_id) REFERENCES users(id);

-- Loan Applications foreign keys
ALTER TABLE loan_applications ADD CONSTRAINT fk_loan_app_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE loan_applications ADD CONSTRAINT fk_loan_app_product 
    FOREIGN KEY (loan_product_id) REFERENCES loan_products(id);

-- Documents foreign keys
ALTER TABLE documents ADD CONSTRAINT fk_document_customer 
    FOREIGN KEY (customer_id) REFERENCES customers(id);

ALTER TABLE documents ADD CONSTRAINT fk_document_loan_app 
    FOREIGN KEY (loan_application_id) REFERENCES loan_applications(id);

ALTER TABLE documents ADD CONSTRAINT fk_document_type 
    FOREIGN KEY (document_type_id) REFERENCES document_types(id);

-- Activity Logs foreign key
ALTER TABLE activity_logs ADD CONSTRAINT fk_activity_user 
    FOREIGN KEY (user_id) REFERENCES users(id);

-- RBAC foreign keys
ALTER TABLE role_permissions ADD CONSTRAINT FKegdk29eiy7mdtefy5c7eirr6e 
    FOREIGN KEY (permission_id) REFERENCES permissions(id);

ALTER TABLE role_permissions ADD CONSTRAINT FKn5fotdgk8d1xvo8nav9uv3muc 
    FOREIGN KEY (role_id) REFERENCES roles(id);

ALTER TABLE user_roles ADD CONSTRAINT FKhfh9dx7w3ubf1co1vdev94g3f 
    FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE user_roles ADD CONSTRAINT FKh8ciramu9cc9q3qcqiv4ue8a6 
    FOREIGN KEY (role_id) REFERENCES roles(id);

-- =====================================================================================
-- INDICES FOR PERFORMANCE
-- =====================================================================================

-- Activity Logs indices
CREATE INDEX idx_activity_user_id ON activity_logs (user_id);
CREATE INDEX idx_activity_type ON activity_logs (activity_type);
CREATE INDEX idx_activity_entity_type ON activity_logs (entity_type);
CREATE INDEX idx_activity_entity_id ON activity_logs (entity_id);
CREATE INDEX idx_activity_created_at ON activity_logs (created_at);

-- Audit Logs indices  
CREATE INDEX idx_audit_table_name ON audit_logs (table_name);
CREATE INDEX idx_audit_record_id ON audit_logs (record_id);
CREATE INDEX idx_audit_action ON audit_logs (action);
CREATE INDEX idx_audit_changed_by ON audit_logs (changed_by);
CREATE INDEX idx_audit_changed_at ON audit_logs (changed_at);

-- Customers table indices
CREATE INDEX idx_customer_phone ON customers (phone);
CREATE INDEX idx_customer_email ON customers (email);
CREATE INDEX idx_customer_type ON customers (customer_type);
CREATE INDEX idx_customer_province ON customers (province);

-- Documents table indices
CREATE INDEX idx_documents_customer_id ON documents (customer_id);
CREATE INDEX idx_documents_loan_application_id ON documents (loan_application_id);
CREATE INDEX idx_documents_document_type_id ON documents (document_type_id);
CREATE INDEX idx_documents_verification_status ON documents (verification_status);

-- Loan Applications table indices
CREATE INDEX idx_loan_applications_customer_id ON loan_applications (customer_id);
CREATE INDEX idx_loan_applications_status ON loan_applications (status);
CREATE INDEX idx_loan_applications_created_at ON loan_applications (created_at);

-- Loan Products indices
CREATE INDEX idx_loan_product_name ON loan_products (name);
CREATE INDEX idx_loan_product_type ON loan_products (loan_type);

-- Permissions indices
CREATE INDEX idx_permission_resource ON permissions (resource);
CREATE INDEX idx_permission_action ON permissions (action);
CREATE INDEX idx_permission_category ON permissions (category);

-- System Configs indices
CREATE INDEX idx_category ON system_configs (category);
CREATE INDEX idx_active ON system_configs (is_active);
CREATE INDEX idx_category_active ON system_configs (category, is_active);
CREATE INDEX idx_environment ON system_configs (environment);

-- System Settings indices
CREATE INDEX idx_system_setting_category ON system_settings (category);

-- Users indices
CREATE INDEX idx_user_employee_id ON users (employee_id);

-- =====================================================================================
-- END OF SCHEMA - ЭЦСИЙН ХУВИЛБАР
-- =====================================================================================

-- Foreign key шалгалтыг буцааж идэвхжүүлнэ
SET REFERENTIAL_INTEGRITY TRUE;