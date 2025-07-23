-- =====================================================================================
-- LOAN ORIGINATION SYSTEM DATABASE SCHEMA
-- =====================================================================================
-- Created: 2025-07-22
-- Description: Database schema for Loan Origination System (Modified for H2 Database)
-- =====================================================================================

-- Note: H2 does not support UUID extension or RANDOM_UUID(). Use VARCHAR(36) for UUIDs.
-- Note: ENUM types replaced with VARCHAR for H2 compatibility.

-- =====================================================================================
-- CORE TABLES
-- =====================================================================================

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

-- Loan Products Table
CREATE TABLE loan_products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    min_amount DECIMAL(15,2) NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,
    min_term_months INTEGER NOT NULL,
    max_term_months INTEGER NOT NULL,
    base_rate DECIMAL(5,4) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Loan Applications Table
CREATE TABLE loan_applications (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL REFERENCES customers(id),
    loan_product_id VARCHAR(36) NOT NULL REFERENCES loan_products(id),
    application_number VARCHAR(50) UNIQUE NOT NULL,
    
    -- Loan Details
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
    status VARCHAR(20) DEFAULT 'DRAFT', -- 'DRAFT', 'SUBMITTED', 'PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED'
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

-- Document Types Table
CREATE TABLE document_types (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    is_required BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Documents Table
CREATE TABLE documents (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL REFERENCES customers(id),
    loan_application_id VARCHAR(36) REFERENCES loan_applications(id),
    document_type_id VARCHAR(36) NOT NULL REFERENCES document_types(id),
    
    -- Document Information
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
    previous_document_id VARCHAR(36) REFERENCES documents(id),
    
    -- Verification
    verification_status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'REJECTED'
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
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    display_name_mn VARCHAR(100),
    description VARCHAR(500),
    
    -- Permission Details
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL, -- 'CREATE', 'READ', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT'
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

-- Roles Table
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

-- Users Table (System Users)
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
    manager_id VARCHAR(36) REFERENCES users(id),
    
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
    id VARCHAR(36) PRIMARY KEY,
    role_id VARCHAR(36) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id VARCHAR(36) NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted_by VARCHAR(100),
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(role_id, permission_id)
);

-- User-Role Junction Table
CREATE TABLE user_roles (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(36) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
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
    id VARCHAR(36) PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    record_id VARCHAR(36) NOT NULL,
    action VARCHAR(20) NOT NULL, -- 'INSERT', 'UPDATE', 'DELETE'
    old_values JSON,
    new_values JSON,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- Activity Log Table
CREATE TABLE activity_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) REFERENCES users(id),
    activity_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(36),
    description TEXT,
    details JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

-- Loan Configuration Table
CREATE TABLE loan_configurations (
    id VARCHAR(36) PRIMARY KEY,
   还没

-- Triggers for all main tables
CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers 
    FOR EACH ROW SET NEW.updated_at = CALL update_updated_at_column();

CREATE TRIGGER update_loan_products_updated_at BEFORE UPDATE ON loan_products 
    FOR EACH ROW SET NEW.updated_at = CALL update_updated_at_column();

CREATE TRIGGER update_loan_applications_updated_at BEFORE UPDATE ON loan_applications 
    FOR EACH ROW SET NEW.updated_at = CALL update_updated_at_column();

CREATE TRIGGER update_document_types_updated_at BEFORE UPDATE ON document_types 
    FOR EACH ROW SET NEW.updated_at = CALL update_updated_at_column();

CREATE TRIGGER update_documents_updated_at BEFORE UPDATE ON documents 
    FOR EACH ROW SET NEW.updated_at = CALL update_updated_at_column();

CREATE TRIGGER update_permissions_updated_at BEFORE UPDATE ON permissions 
    FOR EACH ROW SET NEW.updated_at = CALL update_updated_at_column();

CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON roles 
    FOR EACH ROW SET NEW.updated_at = CALL update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users 
    FOR EACH ROW SET NEW.updated_at = CALL update_updated_at_column();

-- =====================================================================================
-- END OF SCHEMA
-- =====================================================================================