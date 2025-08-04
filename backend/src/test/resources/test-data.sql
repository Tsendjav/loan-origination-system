-- =====================================================================================
-- TEST DATA SQL - ТЕСТИЙН ӨГӨГДӨЛ
-- =====================================================================================
-- Description: Test data for Loan Origination System unit and integration tests
-- Author: LOS Development Team
-- Version: 1.1
-- Date: 2025-08-03
-- =====================================================================================

-- Disable foreign key checks for data insertion
SET REFERENTIAL_INTEGRITY FALSE;

-- =====================================================================================
-- 1. ROLES - Эрхийн түвшинүүд
-- =====================================================================================
INSERT INTO roles (id, name, display_name, display_name_mn, description, code, status, type, priority, is_system_role, is_default, created_at, updated_at, is_deleted, is_active) VALUES
('role-001', 'ADMIN', 'Administrator', 'Администратор', 'System administrator with full access', 'ADMIN', 'ACTIVE', 'SYSTEM', 1, true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('role-002', 'LOAN_OFFICER', 'Loan Officer', 'Зээлийн мэргэжилтэн', 'Loan application processing officer', 'LOAN_OFFICER', 'ACTIVE', 'BUSINESS', 2, false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('role-003', 'MANAGER', 'Manager', 'Менежер', 'Department manager with approval rights', 'MANAGER', 'ACTIVE', 'BUSINESS', 3, false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('role-004', 'CUSTOMER_SERVICE', 'Customer Service', 'Харилцагчийн үйлчилгээ', 'Customer service representative', 'CUSTOMER_SERVICE', 'ACTIVE', 'BUSINESS', 4, false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true);

-- =====================================================================================
-- 2. PERMISSIONS - Зөвшөөрөл
-- =====================================================================================
INSERT INTO permissions (id, name, display_name, display_name_mn, description, resource, action, category, scope, is_system_permission, priority, created_at, updated_at, is_deleted, is_active) VALUES
('perm-001', 'customer:view', 'View Customers', 'Харилцагч үзэх', 'View customer information', 'customer', 'READ', 'CUSTOMER_MANAGEMENT', 'GLOBAL', false, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('perm-002', 'customer:create', 'Create Customers', 'Харилцагч үүсгэх', 'Create new customers', 'customer', 'CREATE', 'CUSTOMER_MANAGEMENT', 'GLOBAL', false, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('perm-003', 'customer:update', 'Update Customers', 'Харилцагч засах', 'Update customer information', 'customer', 'UPDATE', 'CUSTOMER_MANAGEMENT', 'GLOBAL', false, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('perm-004', 'customer:delete', 'Delete Customers', 'Харилцагч устгах', 'Delete customers', 'customer', 'DELETE', 'CUSTOMER_MANAGEMENT', 'GLOBAL', false, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('perm-005', 'customer:kyc', 'KYC Management', 'KYC удирдлага', 'Manage customer KYC status', 'customer', 'UPDATE', 'CUSTOMER_MANAGEMENT', 'GLOBAL', false, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('perm-006', 'loan:view', 'View Loans', 'Зээл үзэх', 'View loan applications', 'loan', 'READ', 'LOAN_MANAGEMENT', 'GLOBAL', false, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('perm-007', 'loan:create', 'Create Loans', 'Зээл үүсгэх', 'Create loan applications', 'loan', 'CREATE', 'LOAN_MANAGEMENT', 'GLOBAL', false, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('perm-008', 'loan:update', 'Update Loans', 'Зээл засах', 'Update loan applications', 'loan', 'UPDATE', 'LOAN_MANAGEMENT', 'GLOBAL', false, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('perm-009', 'loan:approve', 'Approve Loans', 'Зээл зөвшөөрөх', 'Approve or reject loan applications', 'loan', 'APPROVE', 'LOAN_MANAGEMENT', 'GLOBAL', false, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('perm-010', 'system:admin', 'System Administration', 'Систем удирдлага', 'Full system administration', 'system', 'CONFIG', 'SYSTEM_MANAGEMENT', 'GLOBAL', true, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true);

-- =====================================================================================
-- 3. ROLE-PERMISSION MAPPINGS
-- =====================================================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by, is_active) VALUES
-- Admin роль - бүх зөвшөөрөл
('role-001', 'perm-001', CURRENT_TIMESTAMP, 'system', true),
('role-001', 'perm-002', CURRENT_TIMESTAMP, 'system', true),
('role-001', 'perm-003', CURRENT_TIMESTAMP, 'system', true),
('role-001', 'perm-004', CURRENT_TIMESTAMP, 'system', true),
('role-001', 'perm-005', CURRENT_TIMESTAMP, 'system', true),
('role-001', 'perm-006', CURRENT_TIMESTAMP, 'system', true),
('role-001', 'perm-007', CURRENT_TIMESTAMP, 'system', true),
('role-001', 'perm-008', CURRENT_TIMESTAMP, 'system', true),
('role-001', 'perm-009', CURRENT_TIMESTAMP, 'system', true),
('role-001', 'perm-010', CURRENT_TIMESTAMP, 'system', true),

-- Loan Officer роль
('role-002', 'perm-001', CURRENT_TIMESTAMP, 'system', true),
('role-002', 'perm-002', CURRENT_TIMESTAMP, 'system', true),
('role-002', 'perm-003', CURRENT_TIMESTAMP, 'system', true),
('role-002', 'perm-005', CURRENT_TIMESTAMP, 'system', true),
('role-002', 'perm-006', CURRENT_TIMESTAMP, 'system', true),
('role-002', 'perm-007', CURRENT_TIMESTAMP, 'system', true),
('role-002', 'perm-008', CURRENT_TIMESTAMP, 'system', true),

-- Manager роль
('role-003', 'perm-001', CURRENT_TIMESTAMP, 'system', true),
('role-003', 'perm-003', CURRENT_TIMESTAMP, 'system', true),
('role-003', 'perm-005', CURRENT_TIMESTAMP, 'system', true),
('role-003', 'perm-006', CURRENT_TIMESTAMP, 'system', true),
('role-003', 'perm-008', CURRENT_TIMESTAMP, 'system', true),
('role-003', 'perm-009', CURRENT_TIMESTAMP, 'system', true),

-- Customer Service роль
('role-004', 'perm-001', CURRENT_TIMESTAMP, 'system', true),
('role-004', 'perm-002', CURRENT_TIMESTAMP, 'system', true),
('role-004', 'perm-003', CURRENT_TIMESTAMP, 'system', true),
('role-004', 'perm-006', CURRENT_TIMESTAMP, 'system', true);

-- =====================================================================================
-- 4. USERS - Хэрэглэгчид
-- =====================================================================================
INSERT INTO users (id, username, email, password_hash, first_name, last_name, phone, employee_id, department, position, status, is_email_verified, is_locked, failed_login_attempts, language, timezone, two_factor_enabled, created_at, updated_at, is_deleted, is_active) VALUES
('user-001', 'admin', 'admin@los.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2x7mq3TcCbMaS', 'Admin', 'User', '99999999', 'EMP001', 'IT', 'System Administrator', 'ACTIVE', true, false, 0, 'en', 'Asia/Ulaanbaatar', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('user-002', 'loan_officer', 'loan@los.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2x7mq3TcCbMaS', 'Батбаяр', 'Болд', '99119911', 'EMP002', 'LOANS', 'Loan Officer', 'ACTIVE', true, false, 0, 'mn', 'Asia/Ulaanbaatar', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('user-003', 'manager', 'manager@los.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2x7mq3TcCbMaS', 'Сарангэрэл', 'Баатар', '88228822', 'EMP003', 'LOANS', 'Department Manager', 'ACTIVE', true, false, 0, 'mn', 'Asia/Ulaanbaatar', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('user-004', 'customer_service', 'cs@los.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2x7mq3TcCbMaS', 'Оюунчимэг', 'Түвшин', '77337733', 'EMP004', 'CUSTOMER_SERVICE', 'Customer Service Rep', 'ACTIVE', true, false, 0, 'mn', 'Asia/Ulaanbaatar', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true);

-- =====================================================================================
-- 5. USER-ROLE MAPPINGS
-- =====================================================================================
INSERT INTO user_roles (user_id, role_id, assigned_at, assigned_by, is_active) VALUES
('user-001', 'role-001', CURRENT_TIMESTAMP, 'system', true),
('user-002', 'role-002', CURRENT_TIMESTAMP, 'system', true),
('user-003', 'role-003', CURRENT_TIMESTAMP, 'system', true),
('user-004', 'role-004', CURRENT_TIMESTAMP, 'system', true);

-- =====================================================================================
-- 6. DOCUMENT TYPES - Баримт бичгийн төрөл
-- =====================================================================================
INSERT INTO document_types (id, name, description, category, file_types, max_file_size, is_required, retention_days, created_at, updated_at, is_deleted, is_active) VALUES
('doc-type-001', 'NATIONAL_ID', 'Иргэний үнэмлэх', 'IDENTITY', '["image/jpeg","image/png","application/pdf"]', 10485760, true, 2555, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('doc-type-002', 'INCOME_STATEMENT', 'Орлогын справка', 'FINANCIAL', '["application/pdf","image/jpeg"]', 10485760, true, 2555, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('doc-type-003', 'BANK_STATEMENT', 'Банкны хуулга', 'FINANCIAL', '["application/pdf"]', 10485760, false, 2555, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('doc-type-004', 'EMPLOYMENT_LETTER', 'Ажлын байрны тодорхойлолт', 'EMPLOYMENT', '["application/pdf","image/jpeg"]', 10485760, false, 2555, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true);

-- =====================================================================================
-- 7. LOAN PRODUCTS - Зээлийн бүтээгдэхүүнүүд
-- =====================================================================================
INSERT INTO loan_products (id, name, product_name, loan_type, min_amount, max_amount, min_term_months, max_term_months, base_rate, default_interest_rate, min_interest_rate, max_interest_rate, processing_fee, min_credit_score, min_income, requires_collateral, requires_guarantor, approval_required, auto_approval_limit, display_order, is_featured, description, created_at, updated_at, is_deleted, is_active) VALUES
('product-001', 'Personal Loan Standard', 'Хувийн зээл', 'PERSONAL', 500000.00, 50000000.00, 6, 60, 0.1550, 0.1550, 0.1200, 0.2500, 50000.00, 650, 1000000.00, false, false, true, 5000000.00, 1, true, 'Стандарт хувийн зээл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('product-002', 'Business Loan SME', 'Жижиг бизнесийн зээл', 'BUSINESS', 2000000.00, 500000000.00, 12, 120, 0.1350, 0.1350, 0.1000, 0.2000, 100000.00, 600, 5000000.00, true, true, true, 20000000.00, 2, true, 'Жижиг болон дунд бизнесийн зээл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('product-003', 'Mortgage Home Loan', 'Орон сууцны зээл', 'MORTGAGE', 50000000.00, 2000000000.00, 60, 360, 0.0950, 0.0950, 0.0800, 0.1500, 200000.00, 700, 3000000.00, true, false, true, 100000000.00, 3, true, 'Орон сууцны зээл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('product-004', 'Car Loan', 'Автомашины зээл', 'CAR', 5000000.00, 200000000.00, 12, 84, 0.1250, 0.1250, 0.1000, 0.1800, 75000.00, 650, 2000000.00, true, false, true, 30000000.00, 4, false, 'Автомашины зээл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true);

-- =====================================================================================
-- 8. CUSTOMERS - Харилцагчид
-- =====================================================================================
INSERT INTO customers (id, customer_type, register_number, first_name, last_name, date_of_birth, gender, marital_status, phone, email, address, city, province, country, monthly_income, kyc_status, risk_rating, assigned_to, created_at, updated_at, is_deleted, is_active) VALUES
('customer-001', 'INDIVIDUAL', 'УБ90011500', 'Батбаяр', 'Болд', '1990-01-15', 'MALE', 'MARRIED', '99119911', 'batbayar@test.com', 'ХУД 3-р хороо, 25-р байр', 'Улаанбаатар', 'Улаанбаатар', 'Mongolia', 2500000.00, 'COMPLETED', 'LOW', 'user-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('customer-002', 'INDIVIDUAL', 'УБ92032000', 'Сарангэрэл', 'Батбаяр', '1992-03-20', 'FEMALE', 'SINGLE', '88228822', 'sarangerel@test.com', 'СБД 5-р хороо, 15-р байр', 'Улаанбаатар', 'Улаанбаатар', 'Mongolia', 1800000.00, 'IN_PROGRESS', 'MEDIUM', 'user-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('customer-003', 'BUSINESS', 'УБ85121201', 'ТехноСофт ХХК', 'Технологийн компани', '1985-12-12', 'OTHER', 'SINGLE', '70707070', 'info@technosoft.mn', 'БГД 1-р хороо, Central Tower', 'Улаанбаатар', 'Улаанбаатар', 'Mongolia', 50000000.00, 'COMPLETED', 'LOW', 'user-003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('customer-004', 'INDIVIDUAL', 'УБ88091100', 'Энхбаяр', 'Дорж', '1988-09-11', 'MALE', 'MARRIED', '95959595', 'enhbayar@test.com', 'ЧД 7-р хороо, 8-р байр', 'Улаанбаатар', 'Улаанбаатар', 'Mongolia', 3200000.00, 'COMPLETED', 'LOW', 'user-002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true);

-- =====================================================================================
-- 9. LOAN APPLICATIONS - Зээлийн хүсэлтүүд
-- =====================================================================================
INSERT INTO loan_applications (id, customer_id, loan_product_id, application_number, loan_type, requested_amount, requested_term_months, purpose, status, priority, assigned_to, declared_income, credit_score, submitted_at, created_at, updated_at, is_deleted, is_active) VALUES
('app-001', 'customer-001', 'product-001', 'LN202508030001', 'PERSONAL', 15000000.00, 36, 'Хувийн хэрэгцээ', 'SUBMITTED', 3, 'user-002', 2500000.00, 720, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('app-002', 'customer-002', 'product-001', 'LN202508030002', 'PERSONAL', 8000000.00, 24, 'Сургуулийн төлбөр', 'UNDER_REVIEW', 2, 'user-002', 1800000.00, 680, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('app-003', 'customer-003', 'product-002', 'LN202508030003', 'BUSINESS', 80000000.00, 60, 'Тоног төхөөрөмж худалдан авах', 'PENDING', 1, 'user-003', 50000000.00, 750, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('app-004', 'customer-004', 'product-003', 'LN202508030004', 'MORTGAGE', 150000000.00, 240, 'Орон сууц худалдан авах', 'DRAFT', 3, 'user-002', 3200000.00, 740, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true);

-- =====================================================================================
-- 10. DOCUMENTS - Баримт бичиг
-- =====================================================================================
INSERT INTO documents (id, customer_id, loan_application_id, document_type_id, file_name, original_filename, stored_filename, file_path, content_type, file_size, description, verification_status, uploaded_at, created_at, updated_at, is_deleted, is_active) VALUES
('doc-001', 'customer-001', 'app-001', 'doc-type-001', 'national_id_001.pdf', 'batbayar_id.pdf', 'stored_001.pdf', '/uploads/documents/stored_001.pdf', 'application/pdf', 2048576, 'Батбаярын иргэний үнэмлэх', 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('doc-002', 'customer-001', 'app-001', 'doc-type-002', 'income_001.pdf', 'batbayar_income.pdf', 'stored_002.pdf', '/uploads/documents/stored_002.pdf', 'application/pdf', 1536000, 'Орлогын справка', 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true),
('doc-003', 'customer-002', 'app-002', 'doc-type-001', 'national_id_002.pdf', 'sarangerel_id.pdf', 'stored_003.pdf', '/uploads/documents/stored_003.pdf', 'application/pdf', 1874944, 'Сарангэрэлийн иргэний үнэмлэх', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, true);

-- =====================================================================================
-- 11. SYSTEM SETTINGS - Системийн тохиргоо
-- =====================================================================================
INSERT INTO system_settings (id, setting_key, setting_value, data_type, category, description, is_encrypted, is_runtime_editable, created_at, updated_at) VALUES
('setting-001', 'max_loan_amount', '1000000000', 'DECIMAL', 'LOAN_LIMITS', 'Зээлийн хамгийн их дүн', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('setting-002', 'min_loan_amount', '100000', 'DECIMAL', 'LOAN_LIMITS', 'Зээлийн хамгийн бага дүн', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('setting-003', 'default_interest_rate', '15.5', 'DECIMAL', 'INTEREST_RATES', 'Анхдагч хүүгийн хувь', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('setting-004', 'auto_approval_enabled', 'true', 'BOOLEAN', 'AUTOMATION', 'Автомат зөвшөөрөл идэвхжүүлэх', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('setting-005', 'notification_email_enabled', 'false', 'BOOLEAN', 'NOTIFICATIONS', 'И-мэйл мэдэгдэл идэвхжүүлэх', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================================================
-- 12. SYSTEM CONFIGS - Системийн тохиргоо (extended)
-- =====================================================================================
INSERT INTO system_configs (id, config_key, config_value, default_value, value_type, category, description, environment, is_active, is_runtime_editable, sort_order, version, created_at, updated_at) VALUES
('config-001', 'app.name', 'Loan Origination System', 'LOS', 'STRING', 'APPLICATION', 'Application name', 'ALL', true, false, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('config-002', 'app.version', '1.0.0', '1.0.0', 'STRING', 'APPLICATION', 'Application version', 'ALL', true, false, 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('config-003', 'jwt.expiration', '86400000', '86400000', 'INTEGER', 'SECURITY', 'JWT token expiration time (ms)', 'ALL', true, true, 10, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('config-004', 'file.upload.max.size', '10485760', '10485760', 'INTEGER', 'FILE_UPLOAD', 'Maximum file upload size (bytes)', 'ALL', true, true, 20, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================================================
-- 13. ACTIVITY LOGS - Үйл ажиллагааны лог (сэмпл)
-- =====================================================================================
INSERT INTO activity_logs (id, user_id, activity_type, entity_type, entity_id, description, ip_address, user_agent, session_id, severity, source, created_at) VALUES
('log-001', 'user-001', 'LOGIN', 'USER', 'user-001', 'User logged in successfully', '127.0.0.1', 'Mozilla/5.0 (Test)', 'session-001', 'INFO', 'WEB', CURRENT_TIMESTAMP),
('log-002', 'user-002', 'CREATE', 'CUSTOMER', 'customer-001', 'Created new customer', '127.0.0.1', 'Mozilla/5.0 (Test)', 'session-002', 'INFO', 'WEB', CURRENT_TIMESTAMP),
('log-003', 'user-002', 'CREATE', 'LOAN_APPLICATION', 'app-001', 'Created new loan application', '127.0.0.1', 'Mozilla/5.0 (Test)', 'session-002', 'INFO', 'WEB', CURRENT_TIMESTAMP);

-- Re-enable foreign key checks
SET REFERENTIAL_INTEGRITY TRUE;

-- =====================================================================================
-- TEST DATA INSERTION COMPLETE
-- =====================================================================================
-- Total Records Inserted:
-- - Roles: 4
-- - Permissions: 10 
-- - Users: 4
-- - Customers: 4
-- - Loan Products: 4
-- - Document Types: 4
-- - Loan Applications: 4
-- - Documents: 3
-- - System Settings: 5
-- - System Configs: 4
-- - Activity Logs: 3
-- - Role-Permission mappings: 20
-- - User-Role mappings: 4
-- =====================================================================================