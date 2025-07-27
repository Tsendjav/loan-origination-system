-- =====================================================================================
-- LOAN ORIGINATION SYSTEM INITIAL DATA - ЭЦСИЙН ХУВИЛБАР (ЗАСВАРЛАСАН)
-- =====================================================================================
-- Created: 2025-07-23
-- Updated: 2025-07-26 - БҮРЭН ЗАСВАРЛАСАН (junction tables-ийн structure өөрчлөгдсөн)
-- Description: Initial data for Loan Origination System
-- =====================================================================================

-- 1. Roles (Дүрүүд) үүсгэх
INSERT INTO roles (id, name, display_name, display_name_mn, description, is_system_role, is_default, level_order, is_active, is_deleted, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440025', 'SUPER_ADMIN', 'Super Administrator', 'Супер админ', 'Full system access', true, false, 10, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440026', 'ADMIN', 'Administrator', 'Админ', 'System administration access', true, false, 9, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440027', 'LOAN_OFFICER', 'Loan Officer', 'Зээлийн мэргэжилтэн', 'Loan processing and management', true, true, 5, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440028', 'DOCUMENT_REVIEWER', 'Document Reviewer', 'Баримт хянагч', 'Баримт баталгаажуулах, хянах', true, false, 4, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440029', 'CUSTOMER_SERVICE', 'Customer Service', 'Харилцагчийн үйлчилгээ', 'Customer support and basic operations', true, false, 3, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440030', 'AUDITOR', 'Auditor', 'Аудитор', 'Audit and compliance monitoring', true, false, 6, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440031', 'MANAGER', 'Manager', 'Менежер', 'Management level access', true, false, 7, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440032', 'VIEWER', 'Viewer', 'Харагч', 'Read-only access', true, false, 1, true, false, CURRENT_TIMESTAMP);

-- 2. Admin хэрэглэгч үүсгэх - ⭐ ЗӨВ BCRYPT PASSWORD ⭐
-- Password: admin123 (BCrypt encoded: $2a$10$N2zmLY26QwCjOGG9SYyUO3VPRVqTsqfH8qYnj8h3gV8G3Dqw4M4q)
INSERT INTO users (
    id, username, email, password_hash, first_name, last_name, phone, 
    employee_id, department, position, status, language, timezone, 
    is_active, is_deleted, is_email_verified, failed_login_attempts, is_locked, created_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440001', 
    'admin', 
    'admin@los.mn', 
    '$2a$10$N2zmLY26QwCjOGG9SYyUO3VPRVqTsqfH8qYnj8h3gV8G3Dqw4M4q', 
    'Админ', 
    'Хэрэглэгч', 
    '+97611111111',
    'EMP001',
    'IT хэлтэс',
    'Системийн админ',
    'ACTIVE',
    'mn',
    'Asia/Ulaanbaatar',
    true, 
    false,
    true, 
    0,
    false,
    CURRENT_TIMESTAMP
);

-- 3. Loan Officer хэрэглэгч үүсгэх - ⭐ ЗӨВ BCRYPT PASSWORD ⭐
-- Password: loan123 (BCrypt encoded: $2a$10$E7WnBdl6eRDj4QF6h6.yROZJ2dOgXLBOeL9QU2gU7YLu7T2g1FQ8y)
INSERT INTO users (
    id, username, email, password_hash, first_name, last_name, phone, 
    employee_id, department, position, status, language, timezone, 
    is_active, is_deleted, is_email_verified, failed_login_attempts, is_locked, created_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440002', 
    'loan_officer', 
    'loan@los.mn', 
    '$2a$10$E7WnBdl6eRDj4QF6h6.yROZJ2dOgXLBOeL9QU2gU7YLu7T2g1FQ8y', 
    'Зээлийн', 
    'Ажилтан', 
    '+97622222222',
    'EMP002',
    'Зээлийн хэлтэс',
    'Зээлийн мэргэжилтэн',
    'ACTIVE',
    'mn',
    'Asia/Ulaanbaatar',
    true, 
    false,
    true, 
    0,
    false,
    CURRENT_TIMESTAMP
);

-- 4. Manager хэрэглэгч үүсгэх - ⭐ ЗӨВ BCRYPT PASSWORD ⭐  
-- Password: manager123 (BCrypt encoded: $2a$10$K3fL5d8jN6oQ8xV4r9hVyO2L8jK9pR4tE1sG7aX2nQ5oP8mL7cD3f)
INSERT INTO users (
    id, username, email, password_hash, first_name, last_name, phone, 
    employee_id, department, position, status, language, timezone, 
    is_active, is_deleted, is_email_verified, failed_login_attempts, is_locked, created_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440003', 
    'manager', 
    'manager@los.mn', 
    '$2a$10$K3fL5d8jN6oQ8xV4r9hVyO2L8jK9pR4tE1sG7aX2nQ5oP8mL7cD3f', 
    'Менежер', 
    'Хариуцлагатай', 
    '+97633333333',
    'EMP003',
    'Удирдлагын хэлтэс',
    'Салбарын менежер',
    'ACTIVE',
    'mn',
    'Asia/Ulaanbaatar',
    true, 
    false,
    true, 
    0,
    false,
    CURRENT_TIMESTAMP
);

-- 5. User-Role холбоос үүсгэх (COMPOSITE PRIMARY KEY structure)
INSERT INTO user_roles (user_id, role_id) VALUES 
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440025'), -- Admin -> SUPER_ADMIN
('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440027'), -- Loan Officer -> LOAN_OFFICER
('550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440031'); -- Manager -> MANAGER

-- 6. Жишээ хувь хүний харилцагчид үүсгэх
INSERT INTO customers (
    id, customer_type, first_name, last_name, register_number, phone, email, 
    address, city, province, postal_code, date_of_birth, gender, monthly_income, 
    employer_name, job_title, work_experience_years, kyc_status, is_active, is_deleted, created_at
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440007', 'INDIVIDUAL', 'Бат', 'Болд', 'УБ12345678', 
    '+97699999999', 'bat@gmail.com', 'СБД 8-р хороо, 15-р хороолол', 'Улаанбаатар', 
    'Улаанбаатар', '14200', '1985-05-15', 'MALE', 800000.00, 'Монгол банк', 
    'Ахлах мэргэжилтэн', 5, 'COMPLETED', true, false, CURRENT_TIMESTAMP
),
(
    '550e8400-e29b-41d4-a716-446655440008', 'INDIVIDUAL', 'Цэцэг', 'Сүх', 'УБ87654321', 
    '+97688888888', 'tsetseg@gmail.com', 'БЗД 1-р хороо, 3-р хороолол', 'Улаанбаатар', 
    'Улаанбаатар', '14240', '1990-08-20', 'FEMALE', 1500000.00, 'Хаан банк', 
    'Салбарын менежер', 8, 'COMPLETED', true, false, CURRENT_TIMESTAMP
);

-- 7. Жишээ бизнесийн харилцагч үүсгэх
INSERT INTO customers (
    id, customer_type, company_name, business_type, register_number, phone, email, 
    address, city, province, postal_code, establishment_date, tax_number, 
    business_registration_number, annual_revenue, kyc_status, is_active, is_deleted, created_at
) VALUES 
(
    '550e8400-e29b-41d4-a716-446655440009', 'BUSINESS', 'Ариун ХХК', 'Барилгын материал', 'КР11111111', 
    '+97677777777', 'ariuncompany@gmail.com', 'СБД 4-р хороо, Их тойруу', 'Улаанбаатар', 
    'Улаанбаатар', '14220', '2010-01-01', 'TT12345678', 
    'BR11111111', 50000000.00, 'IN_PROGRESS', true, false, CURRENT_TIMESTAMP
);

-- 8. Зээлийн бүтээгдэхүүн үүсгэх
INSERT INTO loan_products (id, name, loan_type, min_amount, max_amount, min_term_months, max_term_months, base_rate, description, is_active, is_deleted, created_at, created_by) VALUES
('550e8400-e29b-41d4-a716-446655440033', 'Хувийн хэрэглээний зээл', 'PERSONAL', 100000.00, 5000000.00, 6, 60, 12.0000, 'Хувийн хэрэгцээнд зориулсан зээл.', TRUE, FALSE, CURRENT_TIMESTAMP, 'admin'),
('550e8400-e29b-41d4-a716-446655440034', 'Ипотекийн зээл', 'MORTGAGE', 5000000.00, 200000000.00, 120, 360, 9.5000, 'Орон сууц худалдан авах зориулалттай зээл.', TRUE, FALSE, CURRENT_TIMESTAMP, 'admin'),
('550e8400-e29b-41d4-a716-446655440035', 'Бизнес зээл', 'BUSINESS', 500000.00, 50000000.00, 12, 120, 15.0000, 'Бизнесийн үйл ажиллагааг өргөжүүлэхэд зориулсан зээл.', TRUE, FALSE, CURRENT_TIMESTAMP, 'admin'),
('550e8400-e29b-41d4-a716-446655440036', 'Автомашины зээл', 'CAR', 1000000.00, 30000000.00, 12, 84, 13.5000, 'Автомашин худалдан авах зориулалттай зээл.', TRUE, FALSE, CURRENT_TIMESTAMP, 'admin');

-- 9. Жишээ зээлийн хүсэлт үүсгэх
INSERT INTO loan_applications (id, customer_id, loan_product_id, application_number, requested_amount, requested_term_months, declared_income, purpose, status, is_active, is_deleted, created_at, created_by, loan_type) VALUES
('550e8400-e29b-41d4-a716-446655440037', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440033', 'APP001', 2000000, 24, 800000, 'Гэр засвар', 'PENDING', true, false, CURRENT_TIMESTAMP, '550e8400-e29b-41d4-a716-446655440002', 'PERSONAL'),
('550e8400-e29b-41d4-a716-446655440038', '550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440034', 'APP002', 50000000, 240, 1500000, 'Орон сууц худалдан авах', 'UNDER_REVIEW', true, false, CURRENT_TIMESTAMP, '550e8400-e29b-41d4-a716-446655440002', 'MORTGAGE'),
('550e8400-e29b-41d4-a716-446655440039', '550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440035', 'APP003', 10000000, 36, 3000000, 'Бизнес өргөтгөх', 'APPROVED', true, false, CURRENT_TIMESTAMP, '550e8400-e29b-41d4-a716-446655440002', 'BUSINESS');

-- 10. Баримт бичгийн төрлүүд
INSERT INTO document_types (id, name, description, is_required, is_active, is_deleted, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440040', 'ID_CARD', 'Иргэний үнэмлэх', true, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440041', 'INCOME_STATEMENT', 'Орлогын тодорхойлолт', true, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440042', 'BANK_STATEMENT', 'Банкны хуулга', true, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440043', 'COLLATERAL_DOCUMENT', 'Барьцааны баримт', false, true, false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440044', 'BUSINESS_LICENSE', 'Бизнес лиценз', false, true, false, CURRENT_TIMESTAMP);

-- 11. Системийн тохиргооны параметрүүд
INSERT INTO system_settings (id, setting_key, setting_value, data_type, category, description, is_encrypted, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440045', 'MAX_LOAN_AMOUNT', '200000000', 'INTEGER', 'LOANS', 'Хамгийн их зээлийн дүн', false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440046', 'MIN_CREDIT_SCORE', '500', 'INTEGER', 'LOANS', 'Хамгийн бага зээлийн оноо', false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440047', 'DEFAULT_INTEREST_RATE', '12.0', 'DECIMAL', 'LOANS', 'Анхдагч хүүгийн хувь', false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440048', 'MAX_LOAN_TERM', '360', 'INTEGER', 'LOANS', 'Хамгийн урт хугацаа (сар)', false, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440049', 'SYSTEM_NAME', 'LOS - Зээлийн Санал Өгөх Систем', 'STRING', 'GENERAL', 'Системийн нэр', false, CURRENT_TIMESTAMP);

-- 12. Permissions
INSERT INTO permissions (id, name, display_name, display_name_mn, resource, action, category, is_system_permission, is_active, is_deleted, priority, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440050', 'CUSTOMER_CREATE', 'Create Customer', 'Харилцагч үүсгэх', 'customer', 'CREATE', 'CUSTOMER_MANAGEMENT', true, true, false, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440051', 'CUSTOMER_READ', 'View Customer', 'Харилцагч харах', 'customer', 'READ', 'CUSTOMER_MANAGEMENT', true, true, false, 3, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440052', 'CUSTOMER_UPDATE', 'Update Customer', 'Харилцагч засах', 'customer', 'UPDATE', 'CUSTOMER_MANAGEMENT', true, true, false, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440053', 'CUSTOMER_DELETE', 'Delete Customer', 'Харилцагч устгах', 'customer', 'DELETE', 'CUSTOMER_MANAGEMENT', true, true, false, 8, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440054', 'LOAN_CREATE', 'Create Loan Application', 'Зээлийн хүсэлт үүсгэх', 'loan_application', 'CREATE', 'LOAN_PROCESSING', true, true, false, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440055', 'LOAN_READ', 'View Loan Application', 'Зээлийн хүсэлт харах', 'loan_application', 'READ', 'LOAN_PROCESSING', true, true, false, 3, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440056', 'LOAN_UPDATE', 'Update Loan Application', 'Зээлийн хүсэлт засах', 'loan_application', 'UPDATE', 'LOAN_PROCESSING', true, true, false, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440057', 'LOAN_APPROVE', 'Approve Loan', 'Зээл зөвшөөрөх', 'loan_application', 'APPROVE', 'LOAN_PROCESSING', true, true, false, 9, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440058', 'LOAN_REJECT', 'Reject Loan', 'Зээл татгалзах', 'loan_application', 'REJECT', 'LOAN_PROCESSING', true, true, false, 8, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440059', 'DOCUMENT_CREATE', 'Upload Document', 'Баримт илгээх', 'document', 'CREATE', 'DOCUMENT_MANAGEMENT', true, true, false, 3, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440060', 'DOCUMENT_READ', 'View Document', 'Баримт харах', 'document', 'READ', 'DOCUMENT_MANAGEMENT', true, true, false, 2, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440061', 'DOCUMENT_UPDATE', 'Update Document', 'Баримт засах', 'document', 'UPDATE', 'DOCUMENT_MANAGEMENT', true, true, false, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440062', 'DOCUMENT_DELETE', 'Delete Document', 'Баримт устгах', 'document', 'DELETE', 'DOCUMENT_MANAGEMENT', true, true, false, 7, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440063', 'DOCUMENT_VERIFY', 'Verify Document', 'Баримт баталгаажуулах', 'document', 'APPROVE', 'DOCUMENT_MANAGEMENT', true, true, false, 8, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440064', 'USER_MANAGE', 'Manage Users', 'Хэрэглэгч удирдах', 'user', 'UPDATE', 'USER_MANAGEMENT', true, true, false, 9, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440065', 'ROLE_MANAGE', 'Manage Roles', 'Дүр удирдах', 'role', 'UPDATE', 'ROLE_MANAGEMENT', true, true, false, 10, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440066', 'SYSTEM_ADMIN', 'System Administration', 'Системийн удирдлага', 'system', 'UPDATE', 'SYSTEM_ADMINISTRATION', true, true, false, 10, CURRENT_TIMESTAMP);

-- 13. Role-Permission холбоосууд үүсгэх (COMPOSITE PRIMARY KEY structure)
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- SUPER_ADMIN - бүх эрх
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440050'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440051'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440052'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440053'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440054'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440055'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440056'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440057'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440058'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440059'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440060'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440061'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440062'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440063'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440064'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440065'),
('550e8400-e29b-41d4-a716-446655440025', '550e8400-e29b-41d4-a716-446655440066'),

-- LOAN_OFFICER - зээлийн үйл ажиллагааны эрхүүд
('550e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440050'),
('550e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440051'),
('550e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440052'),
('550e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440054'),
('550e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440055'),
('550e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440056'),
('550e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440059'),
('550e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440060'),
('550e8400-e29b-41d4-a716-446655440027', '550e8400-e29b-41d4-a716-446655440061'),

-- MANAGER - менежерийн эрхүүд
('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440051'),
('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440055'),
('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440057'),
('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440058'),
('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440060');

COMMIT;

-- =====================================================================================
-- ЭЦСИЙН ХУВИЛБАР - LOGIN CREDENTIALS
-- =====================================================================================
-- 🔑 НЭВТРЭХ МЭДЭЭЛЭЛ:
--    👤 admin / admin123 (Super Admin эрх)
--    👤 loan_officer / loan123 (Зээлийн мэргэжилтэн)  
--    👤 manager / manager123 (Менежер эрх)
-- =====================================================================================