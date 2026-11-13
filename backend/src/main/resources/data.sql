-- =====================================================================================
-- LOAN ORIGINATION SYSTEM INITIAL DATA - ЭЦСИЙН ХУВИЛБАР
-- =====================================================================================
-- Created: 2025-07-27
-- Version: 3.0
-- Description: Complete initial data for Loan Origination System
-- Author: LOS Development Team
-- =====================================================================================

-- =====================================================================================
-- 1. PERMISSIONS - Системийн зөвшөөрлүүд
-- =====================================================================================

INSERT INTO permissions (id, name, display_name, display_name_mn, description, resource, action, category, is_system_permission, priority, created_at) VALUES
-- User Management Permissions
('11111111-1111-1111-1111-111111111101', 'USER_CREATE', 'Create User', 'Хэрэглэгч үүсгэх', 'Шинэ хэрэглэгч үүсгэх эрх', 'USER', 'CREATE', 'USER_MANAGEMENT', TRUE, 8, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111102', 'USER_READ', 'View User', 'Хэрэглэгч харах', 'Хэрэглэгчийн мэдээлэл харах эрх', 'USER', 'READ', 'USER_MANAGEMENT', TRUE, 3, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111103', 'USER_UPDATE', 'Update User', 'Хэрэглэгч засах', 'Хэрэглэгчийн мэдээлэл засах эрх', 'USER', 'UPDATE', 'USER_MANAGEMENT', TRUE, 6, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111104', 'USER_DELETE', 'Delete User', 'Хэрэглэгч устгах', 'Хэрэглэгч устгах эрх', 'USER', 'DELETE', 'USER_MANAGEMENT', TRUE, 9, CURRENT_TIMESTAMP),

-- Role Management Permissions
('11111111-1111-1111-1111-111111111201', 'ROLE_CREATE', 'Create Role', 'Дүр үүсгэх', 'Шинэ дүр үүсгэх эрх', 'ROLE', 'CREATE', 'ROLE_MANAGEMENT', TRUE, 8, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111202', 'ROLE_READ', 'View Role', 'Дүр харах', 'Дүрийн мэдээлэл харах эрх', 'ROLE', 'READ', 'ROLE_MANAGEMENT', TRUE, 3, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111203', 'ROLE_UPDATE', 'Update Role', 'Дүр засах', 'Дүрийн мэдээлэл засах эрх', 'ROLE', 'UPDATE', 'ROLE_MANAGEMENT', TRUE, 6, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111204', 'ROLE_DELETE', 'Delete Role', 'Дүр устгах', 'Дүр устгах эрх', 'ROLE', 'DELETE', 'ROLE_MANAGEMENT', TRUE, 9, CURRENT_TIMESTAMP),

-- Customer Management Permissions
('11111111-1111-1111-1111-111111111301', 'CUSTOMER_CREATE', 'Create Customer', 'Харилцагч үүсгэх', 'Шинэ харилцагч үүсгэх эрх', 'CUSTOMER', 'CREATE', 'CUSTOMER_MANAGEMENT', TRUE, 5, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111302', 'CUSTOMER_READ', 'View Customer', 'Харилцагч харах', 'Харилцагчийн мэдээлэл харах эрх', 'CUSTOMER', 'READ', 'CUSTOMER_MANAGEMENT', TRUE, 2, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111303', 'CUSTOMER_UPDATE', 'Update Customer', 'Харилцагч засах', 'Харилцагчийн мэдээлэл засах эрх', 'CUSTOMER', 'UPDATE', 'CUSTOMER_MANAGEMENT', TRUE, 5, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111304', 'CUSTOMER_DELETE', 'Delete Customer', 'Харилцагч устгах', 'Харилцагч устгах эрх', 'CUSTOMER', 'DELETE', 'CUSTOMER_MANAGEMENT', TRUE, 8, CURRENT_TIMESTAMP),

-- Loan Management Permissions
('11111111-1111-1111-1111-111111111401', 'LOAN_CREATE', 'Create Loan Application', 'Зээлийн хүсэлт үүсгэх', 'Шинэ зээлийн хүсэлт үүсгэх эрх', 'LOAN', 'CREATE', 'LOAN_MANAGEMENT', TRUE, 5, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111402', 'LOAN_READ', 'View Loan Application', 'Зээлийн хүсэлт харах', 'Зээлийн хүсэлт харах эрх', 'LOAN', 'READ', 'LOAN_MANAGEMENT', TRUE, 2, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111403', 'LOAN_UPDATE', 'Update Loan Application', 'Зээлийн хүсэлт засах', 'Зээлийн хүсэлт засах эрх', 'LOAN', 'UPDATE', 'LOAN_MANAGEMENT', TRUE, 5, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111404', 'LOAN_APPROVE', 'Approve Loan', 'Зээл зөвшөөрөх', 'Зээлийн хүсэлт зөвшөөрөх эрх', 'LOAN', 'APPROVE', 'LOAN_MANAGEMENT', TRUE, 9, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111405', 'LOAN_REJECT', 'Reject Loan', 'Зээл татгалзах', 'Зээлийн хүсэлт татгалзах эрх', 'LOAN', 'REJECT', 'LOAN_MANAGEMENT', TRUE, 7, CURRENT_TIMESTAMP),

-- Document Management Permissions
('11111111-1111-1111-1111-111111111501', 'DOCUMENT_CREATE', 'Upload Document', 'Баримт илгээх', 'Баримт илгээх эрх', 'DOCUMENT', 'CREATE', 'DOCUMENT_MANAGEMENT', TRUE, 3, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111502', 'DOCUMENT_READ', 'View Document', 'Баримт харах', 'Баримт харах эрх', 'DOCUMENT', 'READ', 'DOCUMENT_MANAGEMENT', TRUE, 2, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111503', 'DOCUMENT_UPDATE', 'Update Document', 'Баримт засах', 'Баримт засах эрх', 'DOCUMENT', 'UPDATE', 'DOCUMENT_MANAGEMENT', TRUE, 5, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111504', 'DOCUMENT_DELETE', 'Delete Document', 'Баримт устгах', 'Баримт устгах эрх', 'DOCUMENT', 'DELETE', 'DOCUMENT_MANAGEMENT', TRUE, 7, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111505', 'DOCUMENT_VERIFY', 'Verify Document', 'Баримт баталгаажуулах', 'Баримт баталгаажуулах эрх', 'DOCUMENT', 'APPROVE', 'DOCUMENT_MANAGEMENT', TRUE, 8, CURRENT_TIMESTAMP),

-- System Administration Permissions
('11111111-1111-1111-1111-111111111601', 'SYSTEM_CONFIG', 'System Configuration', 'Системийн тохиргоо', 'Системийн тохиргоо өөрчлөх эрх', 'SYSTEM', 'CONFIG', 'SYSTEM_ADMINISTRATION', TRUE, 10, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111602', 'AUDIT_READ', 'View Audit Logs', 'Аудит харах', 'Системийн аудит харах эрх', 'AUDIT', 'READ', 'AUDIT', TRUE, 8, CURRENT_TIMESTAMP),

-- Report Permissions
('11111111-1111-1111-1111-111111111701', 'REPORT_READ', 'View Reports', 'Тайлан харах', 'Тайлангууд харах эрх', 'REPORT', 'READ', 'REPORTING', TRUE, 4, CURRENT_TIMESTAMP),
('11111111-1111-1111-1111-111111111702', 'REPORT_EXPORT', 'Export Reports', 'Тайлан экспорт', 'Тайлан экспорт хийх эрх', 'REPORT', 'EXPORT', 'REPORTING', TRUE, 5, CURRENT_TIMESTAMP);

-- =====================================================================================
-- 2. ROLES - Системийн дүрүүд
-- =====================================================================================

INSERT INTO roles (id, name, display_name, display_name_mn, description, code, status, type, priority, level_order, is_system_role, is_default, created_at) VALUES
-- Super Admin Role
('22222222-2222-2222-2222-222222222201', 'SUPER_ADMIN', 'Super Administrator', 'Супер Админ', 'Системийн бүх эрхтэй супер администратор', 'SA', 'ACTIVE', 'SYSTEM', 100, 10, TRUE, FALSE, CURRENT_TIMESTAMP),

-- Admin Role
('22222222-2222-2222-2222-222222222202', 'ADMIN', 'Administrator', 'Админ', 'Системийн администратор', 'ADM', 'ACTIVE', 'SYSTEM', 90, 9, TRUE, FALSE, CURRENT_TIMESTAMP),

-- Manager Role
('22222222-2222-2222-2222-222222222203', 'MANAGER', 'Manager', 'Менежер', 'Салбарын менежер, зээлийн шийдвэр гаргах эрхтэй', 'MGR', 'ACTIVE', 'BUSINESS', 80, 8, TRUE, FALSE, CURRENT_TIMESTAMP),

-- Loan Officer Role
('22222222-2222-2222-2222-222222222204', 'LOAN_OFFICER', 'Loan Officer', 'Зээлийн ажилтан', 'Зээлийн хүсэлт боловсруулах ажилтан', 'LO', 'ACTIVE', 'BUSINESS', 70, 7, TRUE, TRUE, CURRENT_TIMESTAMP),

-- Document Reviewer Role
('22222222-2222-2222-2222-222222222205', 'DOCUMENT_REVIEWER', 'Document Reviewer', 'Баримт хянагч', 'Баримт бичиг хянаж баталгаажуулах ажилтан', 'DR', 'ACTIVE', 'BUSINESS', 60, 6, TRUE, FALSE, CURRENT_TIMESTAMP),

-- Customer Service Role
('22222222-2222-2222-2222-222222222206', 'CUSTOMER_SERVICE', 'Customer Service', 'Харилцагчийн үйлчилгээ', 'Харилцагчийн үйлчилгээний ажилтан', 'CS', 'ACTIVE', 'BUSINESS', 50, 5, TRUE, FALSE, CURRENT_TIMESTAMP),

-- Auditor Role
('22222222-2222-2222-2222-222222222207', 'AUDITOR', 'Auditor', 'Аудитор', 'Системийн аудит хийх мэргэжилтэн', 'AUD', 'ACTIVE', 'FUNCTIONAL', 40, 4, TRUE, FALSE, CURRENT_TIMESTAMP),

-- Viewer Role
('22222222-2222-2222-2222-222222222208', 'VIEWER', 'Viewer', 'Харагч', 'Зөвхөн харах эрхтэй хэрэглэгч', 'VW', 'ACTIVE', 'BUSINESS', 30, 3, TRUE, FALSE, CURRENT_TIMESTAMP);

-- =====================================================================================
-- 3. USERS - Системийн хэрэглэгчид (BCrypt password: admin123, loan123, manager123)
-- =====================================================================================

INSERT INTO users (id, username, email, password_hash, first_name, last_name, phone, employee_id, department, position, status, is_email_verified, is_active, language, timezone, created_at) VALUES
-- Super Admin User
('33333333-3333-3333-3333-333333333301', 'admin', 'admin@los.mn', '$2a$12$93Gf88edihEYEYq4q1KMeOJfA9Fg6PgJLnJ.eOh.LARgU3JlPANii', 'Админ', 'Хэрэглэгч', '+97699887766', 'EMP001', 'IT хэлтэс', 'Системийн админ', 'ACTIVE', TRUE, TRUE, 'mn', 'Asia/Ulaanbaatar', CURRENT_TIMESTAMP),

-- Manager User
('33333333-3333-3333-3333-333333333302', 'manager', 'manager@los.mn', '$2a$12$YqUOOHsPdlEb8gUPnrnZBOQdhctrJtsJrcZSK03p7vkTd/QRJwL3q', 'Бат', 'Болд', '+97698887766', 'EMP002', 'Удирдлагын хэлтэс', 'Салбарын менежер', 'ACTIVE', TRUE, TRUE, 'mn', 'Asia/Ulaanbaatar', CURRENT_TIMESTAMP),

-- Loan Officer User
('33333333-3333-3333-3333-333333333303', 'loan_officer', 'loan@los.mn', '$2a$12$tTu8OMZhj35Fn91SWvSvwOkJ/WgN8l.hgF8KhVW0UsZRAluC3LKwK', 'Цэрэн', 'Дорж', '+97697887766', 'EMP003', 'Зээлийн хэлтэс', 'Зээлийн мэргэжилтэн', 'ACTIVE', TRUE, TRUE, 'mn', 'Asia/Ulaanbaatar', CURRENT_TIMESTAMP),

-- Document Reviewer User
('33333333-3333-3333-3333-333333333304', 'reviewer', 'reviewer@los.mn', '$2a$12$93Gf88edihEYEYq4q1KMeOJfA9Fg6PgJLnJ.eOh.LARgU3JlPANii', 'Оюунaa', 'Гантулга', '+97696887766', 'EMP004', 'Баримт хэлтэс', 'Баримт хянагч', 'ACTIVE', TRUE, TRUE, 'mn', 'Asia/Ulaanbaatar', CURRENT_TIMESTAMP),

-- Customer Service User
('33333333-3333-3333-3333-333333333305', 'customer_service', 'service@los.mn', '$2a$12$93Gf88edihEYEYq4q1KMeOJfA9Fg6PgJLnJ.eOh.LARgU3JlPANii', 'Сайхан', 'Оюун', '+97695887766', 'EMP005', 'Харилцагчийн үйлчилгээ', 'Харилцагчийн мэргэжилтэн', 'ACTIVE', TRUE, TRUE, 'mn', 'Asia/Ulaanbaatar', CURRENT_TIMESTAMP);

-- =====================================================================================
-- 4. USER-ROLE ASSIGNMENTS
-- =====================================================================================

INSERT INTO user_roles (user_id, role_id, assigned_at, assigned_by) VALUES
-- Admin -> SUPER_ADMIN
('33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222201', CURRENT_TIMESTAMP, 'system'),
-- Manager -> MANAGER
('33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222203', CURRENT_TIMESTAMP, 'system'),
-- Loan Officer -> LOAN_OFFICER
('33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222204', CURRENT_TIMESTAMP, 'system'),
-- Reviewer -> DOCUMENT_REVIEWER
('33333333-3333-3333-3333-333333333304', '22222222-2222-2222-2222-222222222205', CURRENT_TIMESTAMP, 'system'),
-- Customer Service -> CUSTOMER_SERVICE
('33333333-3333-3333-3333-333333333305', '22222222-2222-2222-2222-222222222206', CURRENT_TIMESTAMP, 'system');

-- =====================================================================================
-- 5. ROLE-PERMISSION ASSIGNMENTS
-- =====================================================================================

-- SUPER_ADMIN - бүх эрх
INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by) 
SELECT '22222222-2222-2222-2222-222222222201', id, CURRENT_TIMESTAMP, 'system' FROM permissions;

-- MANAGER - менежерийн эрхүүд
INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by) VALUES
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111102', CURRENT_TIMESTAMP, 'system'), -- USER_READ
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111103', CURRENT_TIMESTAMP, 'system'), -- USER_UPDATE
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111202', CURRENT_TIMESTAMP, 'system'), -- ROLE_READ
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111301', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_CREATE
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111302', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_READ
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111303', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_UPDATE
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111401', CURRENT_TIMESTAMP, 'system'), -- LOAN_CREATE
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111402', CURRENT_TIMESTAMP, 'system'), -- LOAN_READ
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111403', CURRENT_TIMESTAMP, 'system'), -- LOAN_UPDATE
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111404', CURRENT_TIMESTAMP, 'system'), -- LOAN_APPROVE
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111405', CURRENT_TIMESTAMP, 'system'), -- LOAN_REJECT
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111502', CURRENT_TIMESTAMP, 'system'), -- DOCUMENT_READ
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111505', CURRENT_TIMESTAMP, 'system'), -- DOCUMENT_VERIFY
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111602', CURRENT_TIMESTAMP, 'system'), -- AUDIT_READ
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111701', CURRENT_TIMESTAMP, 'system'), -- REPORT_READ
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111702', CURRENT_TIMESTAMP, 'system'); -- REPORT_EXPORT

-- LOAN_OFFICER - зээлийн ажилтны эрхүүд
INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by) VALUES
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111301', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_CREATE
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111302', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_READ
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111303', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_UPDATE
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111401', CURRENT_TIMESTAMP, 'system'), -- LOAN_CREATE
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111402', CURRENT_TIMESTAMP, 'system'), -- LOAN_READ
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111403', CURRENT_TIMESTAMP, 'system'), -- LOAN_UPDATE
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111501', CURRENT_TIMESTAMP, 'system'), -- DOCUMENT_CREATE
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111502', CURRENT_TIMESTAMP, 'system'), -- DOCUMENT_READ
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111503', CURRENT_TIMESTAMP, 'system'), -- DOCUMENT_UPDATE
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111701', CURRENT_TIMESTAMP, 'system'); -- REPORT_READ

-- DOCUMENT_REVIEWER - баримт хянагчийн эрхүүд
INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by) VALUES
('22222222-2222-2222-2222-222222222205', '11111111-1111-1111-1111-111111111302', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_READ
('22222222-2222-2222-2222-222222222205', '11111111-1111-1111-1111-111111111402', CURRENT_TIMESTAMP, 'system'), -- LOAN_READ
('22222222-2222-2222-2222-222222222205', '11111111-1111-1111-1111-111111111502', CURRENT_TIMESTAMP, 'system'), -- DOCUMENT_READ
('22222222-2222-2222-2222-222222222205', '11111111-1111-1111-1111-111111111503', CURRENT_TIMESTAMP, 'system'), -- DOCUMENT_UPDATE
('22222222-2222-2222-2222-222222222205', '11111111-1111-1111-1111-111111111505', CURRENT_TIMESTAMP, 'system'); -- DOCUMENT_VERIFY

-- CUSTOMER_SERVICE - харилцагчийн үйлчилгээний эрхүүд
INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by) VALUES
('22222222-2222-2222-2222-222222222206', '11111111-1111-1111-1111-111111111302', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_READ
('22222222-2222-2222-2222-222222222206', '11111111-1111-1111-1111-111111111303', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_UPDATE
('22222222-2222-2222-2222-222222222206', '11111111-1111-1111-1111-111111111402', CURRENT_TIMESTAMP, 'system'), -- LOAN_READ
('22222222-2222-2222-2222-222222222206', '11111111-1111-1111-1111-111111111502', CURRENT_TIMESTAMP, 'system'); -- DOCUMENT_READ

-- AUDITOR - аудиторын эрхүүд
INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by) VALUES
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111102', CURRENT_TIMESTAMP, 'system'), -- USER_READ
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111202', CURRENT_TIMESTAMP, 'system'), -- ROLE_READ
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111302', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_READ
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111402', CURRENT_TIMESTAMP, 'system'), -- LOAN_READ
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111502', CURRENT_TIMESTAMP, 'system'), -- DOCUMENT_READ
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111602', CURRENT_TIMESTAMP, 'system'), -- AUDIT_READ
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111701', CURRENT_TIMESTAMP, 'system'), -- REPORT_READ
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111702', CURRENT_TIMESTAMP, 'system'); -- REPORT_EXPORT

-- VIEWER - харагчийн эрхүүд
INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by) VALUES
('22222222-2222-2222-2222-222222222208', '11111111-1111-1111-1111-111111111302', CURRENT_TIMESTAMP, 'system'), -- CUSTOMER_READ
('22222222-2222-2222-2222-222222222208', '11111111-1111-1111-1111-111111111402', CURRENT_TIMESTAMP, 'system'), -- LOAN_READ
('22222222-2222-2222-2222-222222222208', '11111111-1111-1111-1111-111111111502', CURRENT_TIMESTAMP, 'system'), -- DOCUMENT_READ
('22222222-2222-2222-2222-222222222208', '11111111-1111-1111-1111-111111111701', CURRENT_TIMESTAMP, 'system'); -- REPORT_READ

-- =====================================================================================
-- 6. DOCUMENT TYPES - Баримт бичгийн төрлүүд
-- =====================================================================================

INSERT INTO document_types (id, name, description, category, file_types, max_file_size, is_required, retention_days, created_at) VALUES
-- Хувийн баримтууд
('44444444-4444-4444-4444-444444444401', 'ID_CARD', 'Иргэний үнэмлэх', 'PERSONAL', '["image/jpeg", "image/png", "application/pdf"]', 5242880, TRUE, 2555, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444402', 'PASSPORT', 'Гадаад паспорт', 'PERSONAL', '["image/jpeg", "image/png", "application/pdf"]', 5242880, FALSE, 2555, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444403', 'BIRTH_CERTIFICATE', 'Төрсний гэрчилгээ', 'PERSONAL', '["image/jpeg", "image/png", "application/pdf"]', 5242880, FALSE, 2555, CURRENT_TIMESTAMP),

-- Орлогын баримтууд
('44444444-4444-4444-4444-444444444404', 'INCOME_STATEMENT', 'Орлогын тодорхойлолт', 'FINANCIAL', '["application/pdf", "image/jpeg", "image/png"]', 10485760, TRUE, 1095, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444405', 'SALARY_CERTIFICATE', 'Цалингийн тодорхойлолт', 'FINANCIAL', '["application/pdf", "image/jpeg", "image/png"]', 10485760, TRUE, 1095, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444406', 'BANK_STATEMENT', 'Банкны хуулга', 'FINANCIAL', '["application/pdf", "application/vnd.ms-excel"]', 20971520, TRUE, 1095, CURRENT_TIMESTAMP),

-- Барьцааны баримтууд
('44444444-4444-4444-4444-444444444407', 'PROPERTY_DEED', 'Өмчийн гэрчилгээ', 'COLLATERAL', '["application/pdf", "image/jpeg", "image/png"]', 10485760, FALSE, 2555, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444408', 'VEHICLE_REGISTRATION', 'Тээврийн хэрэгслийн улсын дугаар', 'COLLATERAL', '["application/pdf", "image/jpeg", "image/png"]', 5242880, FALSE, 2555, CURRENT_TIMESTAMP),

-- Бизнесийн баримтууд
('44444444-4444-4444-4444-444444444409', 'BUSINESS_LICENSE', 'Бизнес лиценз', 'BUSINESS', '["application/pdf", "image/jpeg", "image/png"]', 10485760, FALSE, 2555, CURRENT_TIMESTAMP),
('44444444-4444-4444-4444-444444444410', 'TAX_CERTIFICATE', 'Татварын тодорхойлолт', 'BUSINESS', '["application/pdf", "image/jpeg", "image/png"]', 10485760, FALSE, 1095, CURRENT_TIMESTAMP);

-- =====================================================================================
-- 7. LOAN PRODUCTS - Зээлийн бүтээгдэхүүнүүд
-- =====================================================================================

INSERT INTO loan_products (id, name, product_name, loan_type, min_amount, max_amount, min_term_months, max_term_months, base_rate, default_interest_rate, min_interest_rate, max_interest_rate, processing_fee_rate, is_featured, description, is_active, created_at, created_by) VALUES
-- Хувийн хэрэглээний зээл
('55555555-5555-5555-5555-555555555501', 'Хувийн хэрэглээний зээл', 'Personal Loan Standard', 'PERSONAL', 100000.00, 10000000.00, 6, 60, 12.0000, 15.0000, 12.0000, 18.0000, 1.0000, TRUE, 'Хувийн хэрэгцээнд зориулсан зээл. Баталгаа шаардахгүй.', TRUE, CURRENT_TIMESTAMP, 'system'),

-- Ипотекийн зээл
('55555555-5555-5555-5555-555555555502', 'Ипотекийн зээл', 'Mortgage Loan', 'MORTGAGE', 10000000.00, 500000000.00, 120, 360, 9.5000, 11.5000, 9.0000, 14.0000, 0.5000, TRUE, 'Орон сууц худалдан авахад зориулсан урт хугацааны зээл.', TRUE, CURRENT_TIMESTAMP, 'system'),

-- Бизнес зээл
('55555555-5555-5555-5555-555555555503', 'Бизнес зээл', 'Business Loan', 'BUSINESS', 1000000.00, 100000000.00, 12, 120, 15.0000, 18.0000, 14.0000, 22.0000, 2.0000, TRUE, 'Бизнесийн үйл ажиллагааг өргөжүүлэхэд зориулсан зээл.', TRUE, CURRENT_TIMESTAMP, 'system'),

-- Автомашины зээл
('55555555-5555-5555-5555-555555555504', 'Автомашины зээл', 'Car Loan', 'CAR', 2000000.00, 50000000.00, 12, 84, 13.5000, 16.0000, 12.5000, 19.0000, 1.5000, FALSE, 'Автомашин худалдан авахад зориулсан зээл.', TRUE, CURRENT_TIMESTAMP, 'system'),

-- Боловсролын зээл
('55555555-5555-5555-5555-555555555505', 'Боловсролын зээл', 'Education Loan', 'EDUCATION', 500000.00, 20000000.00, 12, 120, 10.0000, 12.0000, 8.0000, 15.0000, 0.5000, FALSE, 'Боловсрол эзэмшихэд зориулсан хямд хүүтэй зээл.', TRUE, CURRENT_TIMESTAMP, 'system');

-- =====================================================================================
-- 8. CUSTOMERS - Жишээ харилцагчид
-- =====================================================================================

INSERT INTO customers (id, customer_type, register_number, first_name, last_name, date_of_birth, gender, marital_status, phone, email, address, city, province, postal_code, monthly_income, employer_name, job_title, work_experience_years, kyc_status, is_active, created_at) VALUES
-- Хувь хүн харилцагчид
('66666666-6666-6666-6666-666666666601', 'INDIVIDUAL', 'УБ12345678', 'Бат', 'Болд', '1985-05-15', 'MALE', 'MARRIED', '+97699999999', 'bat.bold@gmail.com', 'СБД 8-р хороо, 15-р хороолол', 'Улаанбаатар', 'Улаанбаатар', '14200', 1200000.00, 'Монгол банк', 'Ахлах мэргэжилтэн', 8, 'COMPLETED', TRUE, CURRENT_TIMESTAMP),

('66666666-6666-6666-6666-666666666602', 'INDIVIDUAL', 'УБ87654321', 'Цэцэг', 'Сүх', '1990-08-20', 'FEMALE', 'SINGLE', '+97688888888', 'tsetseg.suh@gmail.com', 'БЗД 1-р хороо, 3-р хороолол', 'Улаанбаатар', 'Улаанбаатар', '14240', 2000000.00, 'Хаан банк', 'Салбарын менежер', 10, 'COMPLETED', TRUE, CURRENT_TIMESTAMP),

('66666666-6666-6666-6666-666666666603', 'INDIVIDUAL', 'УБ11223344', 'Энхбат', 'Төмөр', '1988-12-10', 'MALE', 'MARRIED', '+97677777777', 'enhbat.tumor@gmail.com', 'ХУД 12-р хороо, 5-р хороолол', 'Улаанбаатар', 'Улаанбаатар', '14250', 800000.00, 'Тээвэр ХХК', 'Жолооч', 5, 'IN_PROGRESS', TRUE, CURRENT_TIMESTAMP);

-- Бизнес харилцагчид
INSERT INTO customers (id, customer_type, register_number, company_name, business_type, establishment_date, tax_number, business_registration_number, phone, email, address, city, province, postal_code, annual_revenue, kyc_status, is_active, created_at) VALUES
('66666666-6666-6666-6666-666666666604', 'BUSINESS', 'КР11111111', 'Ариун ХХК', 'Барилгын материал', '2010-01-01', 'TT12345678', 'BR11111111', '+97666666666', 'info@ariun.mn', 'СБД 4-р хороо, Их тойруу', 'Улаанбаатар', 'Улаанбаатар', '14220', 120000000.00, 'COMPLETED', TRUE, CURRENT_TIMESTAMP),

('66666666-6666-6666-6666-666666666605', 'BUSINESS', 'КР22222222', 'Мандал ХХК', 'Худалдаа үйлчилгээ', '2015-06-15', 'TT87654321', 'BR22222222', '+97655555555', 'contact@mandal.mn', 'ЧД 3-р хороо, Зах', 'Улаанбаатар', 'Улаанбаатар', '14230', 80000000.00, 'IN_PROGRESS', TRUE, CURRENT_TIMESTAMP);

-- =====================================================================================
-- 9. LOAN APPLICATIONS - Жишээ зээлийн хүсэлтүүд
-- =====================================================================================

INSERT INTO loan_applications (id, customer_id, loan_product_id, application_number, loan_type, requested_amount, requested_term_months, declared_income, purpose, status, priority, assigned_to, is_active, created_at, created_by) VALUES
-- Бат Болдын хувийн зээл
('77777777-7777-7777-7777-777777777701', '66666666-6666-6666-6666-666666666601', '55555555-5555-5555-5555-555555555501', 'APP2025001', 'PERSONAL', 3000000.00, 24, 1200000.00, 'Гэр засвар', 'UNDER_REVIEW', 3, 'loan_officer', TRUE, CURRENT_TIMESTAMP, 'loan_officer'),

-- Цэцэг Сүхийн ипотекийн зээл
('77777777-7777-7777-7777-777777777702', '66666666-6666-6666-6666-666666666602', '55555555-5555-5555-5555-555555555502', 'APP2025002', 'MORTGAGE', 80000000.00, 240, 2000000.00, 'Орон сууц худалдан авах', 'PENDING_DOCUMENTS', 2, 'loan_officer', TRUE, CURRENT_TIMESTAMP, 'loan_officer'),

-- Ариун ХХК-ийн бизнес зээл
('77777777-7777-7777-7777-777777777703', '66666666-6666-6666-6666-666666666604', '55555555-5555-5555-5555-555555555503', 'APP2025003', 'BUSINESS', 25000000.00, 36, 10000000.00, 'Бизнес өргөтгөх', 'APPROVED', 1, 'manager', TRUE, CURRENT_TIMESTAMP, 'loan_officer'),

-- Энхбат Төмөрийн автомашины зээл
('77777777-7777-7777-7777-777777777704', '66666666-6666-6666-6666-666666666603', '55555555-5555-5555-5555-555555555504', 'APP2025004', 'CAR', 15000000.00, 60, 800000.00, 'Автомашин худалдан авах', 'SUBMITTED', 3, 'loan_officer', TRUE, CURRENT_TIMESTAMP, 'customer_service');

-- =====================================================================================
-- 10. SYSTEM SETTINGS - Системийн тохиргоо
-- =====================================================================================

INSERT INTO system_settings (id, setting_key, setting_value, data_type, category, description, is_runtime_editable, created_at) VALUES
-- Зээлийн тохиргоо
('88888888-8888-8888-8888-888888888801', 'MAX_LOAN_AMOUNT', '500000000', 'INTEGER', 'LOANS', 'Хамгийн их зээлийн дүн (төгрөг)', TRUE, CURRENT_TIMESTAMP),
('88888888-8888-8888-8888-888888888802', 'MIN_CREDIT_SCORE', '500', 'INTEGER', 'LOANS', 'Хамгийн бага зээлийн оноо', TRUE, CURRENT_TIMESTAMP),
('88888888-8888-8888-8888-888888888803', 'DEFAULT_INTEREST_RATE', '15.0', 'DECIMAL', 'LOANS', 'Анхдагч хүүгийн хувь', TRUE, CURRENT_TIMESTAMP),
('88888888-8888-8888-8888-888888888804', 'MAX_LOAN_TERM', '360', 'INTEGER', 'LOANS', 'Хамгийн урт хугацаа (сар)', TRUE, CURRENT_TIMESTAMP),

-- Аюулгүй байдлын тохиргоо
('88888888-8888-8888-8888-888888888805', 'MAX_LOGIN_ATTEMPTS', '5', 'INTEGER', 'SECURITY', 'Хамгийн олон оролдлого', TRUE, CURRENT_TIMESTAMP),
('88888888-8888-8888-8888-888888888806', 'LOCK_DURATION_MINUTES', '30', 'INTEGER', 'SECURITY', 'Түгжих хугацаа (минут)', TRUE, CURRENT_TIMESTAMP),
('88888888-8888-8888-8888-888888888807', 'PASSWORD_EXPIRY_DAYS', '90', 'INTEGER', 'SECURITY', 'Нууц үг дуусах хугацаа (өдөр)', TRUE, CURRENT_TIMESTAMP),

-- Систем ерөнхий
('88888888-8888-8888-8888-888888888808', 'SYSTEM_NAME', 'LOS - Зээлийн Санал Өгөх Систем', 'STRING', 'GENERAL', 'Системийн нэр', FALSE, CURRENT_TIMESTAMP),
('88888888-8888-8888-8888-888888888809', 'COMPANY_NAME', 'Монгол банк', 'STRING', 'GENERAL', 'Компанийн нэр', FALSE, CURRENT_TIMESTAMP),
('88888888-8888-8888-8888-888888888810', 'SUPPORT_EMAIL', 'support@los.mn', 'STRING', 'GENERAL', 'Дэмжлэгийн имэйл', TRUE, CURRENT_TIMESTAMP);

-- =====================================================================================
-- 11. SYSTEM CONFIGS - Системийн дэлгэрэнгүй тохиргоо
-- =====================================================================================

INSERT INTO system_configs (id, config_key, config_value, default_value, value_type, category, description, environment, is_active, is_runtime_editable, sort_order, created_at, created_by) VALUES
-- JWT тохиргоо
('jwt.expiration', 'JWT_EXPIRATION_TIME', '86400', '86400', 'INTEGER', 'JWT', 'JWT токений хүчинтэй байх хугацаа (секунд)', 'ALL', TRUE, TRUE, 1, CURRENT_TIMESTAMP, 'system'),
('jwt.secret', 'JWT_SECRET_KEY', 'mySecretKey2025!@#', 'defaultSecret', 'STRING', 'JWT', 'JWT шифрлэлтийн түлхүүр', 'ALL', TRUE, FALSE, 2, CURRENT_TIMESTAMP, 'system'),
('jwt.refresh.expiration', 'JWT_REFRESH_EXPIRATION', '604800', '604800', 'INTEGER', 'JWT', 'Refresh токений хүчинтэй байх хугацаа (секунд)', 'ALL', TRUE, TRUE, 3, CURRENT_TIMESTAMP, 'system'),

-- Өгөгдлийн сан тохиргоо
('db.connection.pool.size', 'DB_CONNECTION_POOL_SIZE', '20', '10', 'INTEGER', 'DATABASE', 'Өгөгдлийн сангийн холболтын pool хэмжээ', 'ALL', TRUE, TRUE, 10, CURRENT_TIMESTAMP, 'system'),
('db.connection.timeout', 'DB_CONNECTION_TIMEOUT', '30000', '30000', 'INTEGER', 'DATABASE', 'Өгөгдлийн сангийн холболтын timeout (миллисекунд)', 'ALL', TRUE, TRUE, 11, CURRENT_TIMESTAMP, 'system'),

-- Файлын тохиргоо
('file.upload.max.size', 'FILE_UPLOAD_MAX_SIZE', '10485760', '10485760', 'INTEGER', 'FILE_UPLOAD', 'Хамгийн их файлын хэмжээ (байт)', 'ALL', TRUE, TRUE, 20, CURRENT_TIMESTAMP, 'system'),
('file.upload.allowed.types', 'FILE_UPLOAD_ALLOWED_TYPES', 'pdf,jpg,jpeg,png,doc,docx,xls,xlsx', 'pdf,jpg,jpeg,png', 'STRING', 'FILE_UPLOAD', 'Зөвшөөрөгдсөн файлын төрлүүд', 'ALL', TRUE, TRUE, 21, CURRENT_TIMESTAMP, 'system'),

-- Имэйл тохиргоо
('email.smtp.host', 'EMAIL_SMTP_HOST', 'smtp.gmail.com', 'localhost', 'STRING', 'EMAIL', 'SMTP сервер', 'ALL', TRUE, TRUE, 30, CURRENT_TIMESTAMP, 'system'),
('email.smtp.port', 'EMAIL_SMTP_PORT', '587', '587', 'INTEGER', 'EMAIL', 'SMTP порт', 'ALL', TRUE, TRUE, 31, CURRENT_TIMESTAMP, 'system'),
('email.from', 'EMAIL_FROM_ADDRESS', 'noreply@los.mn', 'noreply@example.com', 'STRING', 'EMAIL', 'Илгээгчийн имэйл хаяг', 'ALL', TRUE, TRUE, 32, CURRENT_TIMESTAMP, 'system');

-- =====================================================================================
-- COMMIT TRANSACTION
-- =====================================================================================
COMMIT;

-- =====================================================================================
-- INITIAL DATA SETUP COMPLETE
-- =====================================================================================
-- 🔑 НЭВТРЭХ МЭДЭЭЛЭЛ:
--    👤 admin / admin123 (Super Admin эрх - бүх системийн эрх)
--    👤 manager / manager123 (Менежер эрх - зээл зөвшөөрөх эрх)
--    👤 loan_officer / loan123 (Зээлийн мэргэжилтэн - зээл боловсруулах эрх)
--    👤 reviewer / admin123 (Баримт хянагч - баримт баталгаажуулах эрх)
--    👤 customer_service / admin123 (Харилцагчийн үйлчилгээ - харилцагч харах эрх)
-- 
-- 📊 ҮҮСГЭСЭН ӨГӨГДӨЛ:
--    - Permissions: 26 зөвшөөрөл
--    - Roles: 8 дүр  
--    - Users: 5 хэрэглэгч
--    - Document Types: 10 төрөл
--    - Loan Products: 5 бүтээгдэхүүн
--    - Customers: 5 харилцагч (3 хувь хүн, 2 бизнес)
--    - Loan Applications: 4 хүсэлт
--    - System Settings: 10 тохиргоо
--    - System Configs: 10 дэлгэрэнгүй тохиргоо
-- =====================================================================================