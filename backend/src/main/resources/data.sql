-- LOS системд анхны өгөгдөл оруулах SQL скрипт

-- 1. Roles (Дүрүүд) үүсгэх
INSERT INTO roles (id, name, display_name, display_name_mn, description, is_system_role, is_default, level_order, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440025', 'SUPER_ADMIN', 'Super Administrator', 'Супер админ', 'Full system access', true, false, 10, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440026', 'ADMIN', 'Administrator', 'Админ', 'System administration access', true, false, 9, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440027', 'LOAN_OFFICER', 'Loan Officer', 'Зээлийн мэргэжилтэн', 'Loan processing and management', true, true, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440028', 'DOCUMENT_REVIEWER', 'Document Reviewer', 'Баримт хянанагч', 'Document verification and review', true, false, 4, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440029', 'CUSTOMER_SERVICE', 'Customer Service', 'Харилцагчийн үйлчилгээ', 'Customer support and basic operations', true, false, 3, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440030', 'AUDITOR', 'Auditor', 'Аудитор', 'Audit and compliance monitoring', true, false, 6, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440031', 'MANAGER', 'Manager', 'Менежер', 'Management level access', true, false, 7, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440032', 'VIEWER', 'Viewer', 'Харагч', 'Read-only access', true, false, 1, CURRENT_TIMESTAMP);

-- 2. Admin хэрэглэгч үүсгэх
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (id, username, email, password_hash, first_name, last_name, phone, is_active, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'admin', 'admin@los.mn', '$2a$10$N.zmLY2.6QwCjOGG9SYyUO3VPRVqTsqfH8qYnj8h3gV8G3Dqw4M4q', 'Админ', 'Хэрэглэгч', '+97611111111', true, CURRENT_TIMESTAMP);

-- 3. Loan Officer хэрэглэгч үүсгэх
-- Password: loan123
INSERT INTO users (id, username, email, password_hash, first_name, last_name, phone, is_active, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440002', 'loan_officer', 'loan@los.mn', '$2a$10$E7WnBdl6eRDj4QF6h6/yROZJ2dOgXLBOeL9QU2gU7YLu7T2g1FQ8y', 'Зээлийн', 'Ажилтан', '+97622222222', true, CURRENT_TIMESTAMP);

-- 4. Manager хэрэглэгч үүсгэх  
-- Password: manager123
INSERT INTO users (id, username, email, password_hash, first_name, last_name, phone, is_active, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440003', 'manager', 'manager@los.mn', '$2a$10$K3fL5d8jN6oQ8xV4r9hVyO.L8jK9pR4tE1sG7aX2nQ5oP8mL7cD3f', 'Менежер', 'Хариуцлагатай', '+97633333333', true, CURRENT_TIMESTAMP);

-- 5. User-Role холбоос үүсгэх
INSERT INTO user_roles (id, user_id, role_id, assigned_at) VALUES 
('550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440025', CURRENT_TIMESTAMP), -- Admin -> SUPER_ADMIN
('550e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440027', CURRENT_TIMESTAMP), -- Loan Officer -> LOAN_OFFICER
('550e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440031', CURRENT_TIMESTAMP); -- Manager -> MANAGER

-- 6. Жишээ харилцагчид үүсгэх
INSERT INTO customers (id, customer_type, first_name, last_name, register_number, phone, email, address, date_of_birth, created_at, is_active) VALUES 
('550e8400-e29b-41d4-a716-446655440007', 'INDIVIDUAL', 'Бат', 'Болд', 'УБ12345678', '+97699999999', 'bat@gmail.com', 'Улаанбаатар хот', '1985-05-15', CURRENT_TIMESTAMP, true),
('550e8400-e29b-41d4-a716-446655440008', 'INDIVIDUAL', 'Цэцэг', 'Сүх', 'УБ87654321', '+97688888888', 'tsetseg@gmail.com', 'Улаанбаатар хот', '1990-08-20', CURRENT_TIMESTAMP, true),
('550e8400-e29b-41d4-a716-446655440009', 'BUSINESS', 'Ариун', 'Компани', 'КР11111111', '+97677777777', 'ariuncompany@gmail.com', 'Улаанбаатар хот, 1-р хороо', '2010-01-01', CURRENT_TIMESTAMP, true);

-- 7. Зээлийн бүтээгдэхүүн үүсгэх
INSERT INTO loan_products (id, name, min_amount, max_amount, min_term_months, max_term_months, base_rate, description, is_active, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440010', 'Хэрэглээний зээл', 100000, 5000000, 6, 60, 12.0, 'Хувийн хэрэглээний зээл', true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440011', 'Ипотекийн зээл', 5000000, 200000000, 120, 360, 9.5, 'Орон сууцны зээл', true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440012', 'Бизнес зээл', 500000, 50000000, 12, 120, 15.0, 'Бизнесийн зээл', true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440013', 'Автомашины зээл', 1000000, 30000000, 12, 84, 13.5, 'Автомашины зээл', true, CURRENT_TIMESTAMP);

-- 8. Жишээ зээлийн хүсэлт үүсгэх
INSERT INTO loan_applications (id, customer_id, loan_product_id, application_number, requested_amount, requested_term_months, declared_income, purpose, status, created_at, created_by) VALUES 
('550e8400-e29b-41d4-a716-446655440014', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440010', 'APP001', 2000000, 24, 800000, 'Гэр засвар', 'PENDING', CURRENT_TIMESTAMP, '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440015', '550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440011', 'APP002', 50000000, 240, 1500000, 'Орон сууц худалдан авах', 'UNDER_REVIEW', CURRENT_TIMESTAMP, '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440016', '550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440012', 'APP003', 10000000, 36, 3000000, 'Бизнес өргөтгөх', 'APPROVED', CURRENT_TIMESTAMP, '550e8400-e29b-41d4-a716-446655440002');

-- 9. Баримт бичгийн төрлүүд
INSERT INTO document_types (id, name, description, is_required, is_active, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440017', 'ID_CARD', 'Иргэний үнэмлэх', true, true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440018', 'INCOME_CERTIFICATE', 'Орлогын гэрчилгээ', true, true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440019', 'BANK_STATEMENT', 'Банкны хуулга', true, true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440020', 'COLLATERAL_DOCUMENT', 'Барьцааны баримт', false, true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440021', 'BUSINESS_LICENSE', 'Бизнес лиценз', false, true, CURRENT_TIMESTAMP);

-- 10. Системийн тохиргооны параметрүүд
INSERT INTO system_settings (id, setting_key, setting_value, data_type, category, description, is_active, created_at) VALUES 
('550e8400-e29b-41d4-a716-446655440022', 'MAX_LOAN_AMOUNT', '200000000', 'INTEGER', 'LOANS', 'Хамгийн их зээлийн дүн', true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440023', 'MIN_CREDIT_SCORE', '500', 'INTEGER', 'LOANS', 'Хамгийн бага зээлийн оноо', true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440024', 'DEFAULT_INTEREST_RATE', '12.0', 'DECIMAL', 'LOANS', 'Анхдагч хүүгийн хувь', true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440025', 'MAX_LOAN_TERM', '360', 'INTEGER', 'LOANS', 'Хамгийн урт хугацаа (сар)', true, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440026', 'SYSTEM_NAME', 'LOS - Зээлийн Санал Өгөх Систем', 'STRING', 'GENERAL', 'Системийн нэр', true, CURRENT_TIMESTAMP);

-- 11. Permissions
INSERT INTO permissions (id, name, display_name, display_name_mn, resource, action, category, is_system_permission, priority, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440008', 'CUSTOMER_CREATE', 'Create Customer', 'Харилцагч үүсгэх', 'customer', 'CREATE', 'CUSTOMER_MANAGEMENT', true, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440009', 'CUSTOMER_READ', 'View Customer', 'Харилцагч харах', 'customer', 'READ', 'CUSTOMER_MANAGEMENT', true, 3, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440010', 'CUSTOMER_UPDATE', 'Update Customer', 'Харилцагч засах', 'customer', 'UPDATE', 'CUSTOMER_MANAGEMENT', true, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440011', 'CUSTOMER_DELETE', 'Delete Customer', 'Харилцагч устгах', 'customer', 'DELETE', 'CUSTOMER_MANAGEMENT', true, 8, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440012', 'LOAN_CREATE', 'Create Loan Application', 'Зээлийн хүсэлт үүсгэх', 'loan_application', 'CREATE', 'LOAN_PROCESSING', true, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440013', 'LOAN_READ', 'View Loan Application', 'Зээлийн хүсэлт харах', 'loan_application', 'READ', 'LOAN_PROCESSING', true, 3, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440014', 'LOAN_UPDATE', 'Update Loan Application', 'Зээлийн хүсэлт засах', 'loan_application', 'UPDATE', 'LOAN_PROCESSING', true, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440015', 'LOAN_APPROVE', 'Approve Loan', 'Зээл зөвшөөрөх', 'loan_application', 'APPROVE', 'LOAN_PROCESSING', true, 9, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440016', 'LOAN_REJECT', 'Reject Loan', 'Зээл татгалзах', 'loan_application', 'REJECT', 'LOAN_PROCESSING', true, 8, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440017', 'DOCUMENT_CREATE', 'Upload Document', 'Баримт илгээх', 'document', 'CREATE', 'DOCUMENT_MANAGEMENT', true, 3, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440018', 'DOCUMENT_READ', 'View Document', 'Баримт харах', 'document', 'READ', 'DOCUMENT_MANAGEMENT', true, 2, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440019', 'DOCUMENT_UPDATE', 'Update Document', 'Баримт засах', 'document', 'UPDATE', 'DOCUMENT_MANAGEMENT', true, 5, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440020', 'DOCUMENT_DELETE', 'Delete Document', 'Баримт устгах', 'document', 'DELETE', 'DOCUMENT_MANAGEMENT', true, 7, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440021', 'DOCUMENT_VERIFY', 'Verify Document', 'Баримт баталгаажуулах', 'document', 'APPROVE', 'DOCUMENT_MANAGEMENT', true, 8, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440022', 'USER_MANAGE', 'Manage Users', 'Хэрэглэгч удирдах', 'user', 'UPDATE', 'USER_MANAGEMENT', true, 9, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440023', 'ROLE_MANAGE', 'Manage Roles', 'Дүр удирдах', 'role', 'UPDATE', 'ROLE_MANAGEMENT', true, 10, CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440024', 'SYSTEM_ADMIN', 'System Administration', 'Системийн удирдлага', 'system', 'UPDATE', 'SYSTEM_ADMINISTRATION', true, 10, CURRENT_TIMESTAMP);

COMMIT;