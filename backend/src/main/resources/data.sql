-- LOS системд админ эрх үүсгэх SQL script

-- 1. Roles (Дүрүүд) үүсгэх
INSERT INTO roles (id, name, description, created_at) VALUES 
(1, 'ADMIN', 'Системийн администратор', NOW()),
(2, 'LOAN_OFFICER', 'Зээлийн ажилтан', NOW()),
(3, 'CUSTOMER_SERVICE', 'Харилцагчийн үйлчилгээ', NOW()),
(4, 'MANAGER', 'Менежер', NOW()),
(5, 'VIEWER', 'Харах эрхтэй', NOW());

-- 2. Admin хэрэглэгч үүсгэх
-- Password: admin123 (BCrypt encoded)
INSERT INTO users (id, username, email, password, first_name, last_name, phone, is_active, created_at) VALUES 
(1, 'admin', 'admin@los.mn', '$2a$10$N.zmLY2.6QwCjOGG9SYyUO3VPRVqTsqfH8qYnj8h3gV8G3Dqw4M4q', 'Админ', 'Хэрэглэгч', '+97611111111', true, NOW());

-- 3. Loan Officer хэрэглэгч үүсгэх
-- Password: loan123
INSERT INTO users (id, username, email, password, first_name, last_name, phone, is_active, created_at) VALUES 
(2, 'loan_officer', 'loan@los.mn', '$2a$10$E7WnBdl6eRDj4QF6h6/yROZJ2dOgXLBOeL9QU2gU7YLu7T2g1FQ8y', 'Зээлийн', 'Ажилтан', '+97622222222', true, NOW());

-- 4. Manager хэрэглэгч үүсгэх  
-- Password: manager123
INSERT INTO users (id, username, email, password, first_name, last_name, phone, is_active, created_at) VALUES 
(3, 'manager', 'manager@los.mn', '$2a$10$K3fL5d8jN6oQ8xV4r9hVyO.L8jK9pR4tE1sG7aX2nQ5oP8mL7cD3f', 'Менежер', 'Хариуцлагатай', '+97633333333', true, NOW());

-- 5. User-Role холбоос үүсгэх
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- Admin -> ADMIN role
(2, 2), -- Loan Officer -> LOAN_OFFICER role  
(3, 4); -- Manager -> MANAGER role

-- 6. Жишээ харилцагчид үүсгэх
INSERT INTO customers (id, customer_type, first_name, last_name, register_number, phone, email, address, birth_date, created_at, is_active) VALUES 
(1, 'INDIVIDUAL', 'Бат', 'Болд', 'УБ12345678', '+97699999999', 'bat@gmail.com', 'Улаанбаатар хот', '1985-05-15', NOW(), true),
(2, 'INDIVIDUAL', 'Цэцэг', 'Сүх', 'УБ87654321', '+97688888888', 'tsetseg@gmail.com', 'Улаанбаатар хот', '1990-08-20', NOW(), true),
(3, 'BUSINESS', 'Ариун', 'Компани', 'КР11111111', '+97677777777', 'ariuncompany@gmail.com', 'Улаанбаатар хот, 1-р хороо', '2010-01-01', NOW(), true);

-- 7. Зээлийн бүтээгдэхүүн үүсгэх
INSERT INTO loan_products (id, name, min_amount, max_amount, min_term_months, max_term_months, base_rate, description, is_active, created_at) VALUES 
(1, 'Хэрэглээний зээл', 100000, 5000000, 6, 60, 12.0, 'Хувийн хэрэглээний зээл', true, NOW()),
(2, 'Ипотекийн зээл', 5000000, 200000000, 120, 360, 9.5, 'Орон сууцны зээл', true, NOW()),
(3, 'Бизнес зээл', 500000, 50000000, 12, 120, 15.0, 'Бизнесийн зээл', true, NOW()),
(4, 'Автомашины зээл', 1000000, 30000000, 12, 84, 13.5, 'Автомашины зээл', true, NOW());

-- 8. Жишээ зээлийн хүсэлт үүсгэх
INSERT INTO loan_applications (id, customer_id, loan_product_id, requested_amount, requested_term_months, declared_income, purpose, status, created_at, created_by) VALUES 
(1, 1, 1, 2000000, 24, 800000, 'Гэр засвар', 'PENDING', NOW(), 2),
(2, 2, 2, 50000000, 240, 1500000, 'Орон сууц худалдан авах', 'UNDER_REVIEW', NOW(), 2),
(3, 3, 3, 10000000, 36, 3000000, 'Бизнес өргөтгөх', 'APPROVED', NOW(), 2);

-- 9. Баримт бичгийн төрлүүд
INSERT INTO document_types (id, name, description, is_required, is_active) VALUES 
(1, 'ID_CARD', 'Иргэний үнэмлэх', true, true),
(2, 'INCOME_CERTIFICATE', 'Орлогын гэрчилгээ', true, true),
(3, 'BANK_STATEMENT', 'Банкны хуулга', true, true),
(4, 'COLLATERAL_DOCUMENT', 'Барьцааны баримт', false, true),
(5, 'BUSINESS_LICENSE', 'Бизнес лиценз', false, true);

-- 10. Системийн тохиргооны параметрүүд
INSERT INTO system_settings (setting_key, setting_value, description, is_active) VALUES 
('MAX_LOAN_AMOUNT', '200000000', 'Хамгийн их зээлийн дүн', true),
('MIN_CREDIT_SCORE', '500', 'Хамгийн бага зээлийн оноо', true),
('DEFAULT_INTEREST_RATE', '12.0', 'Анхдагч хүүгийн хувь', true),
('MAX_LOAN_TERM', '360', 'Хамгийн урт хугацаа (сар)', true),
('SYSTEM_NAME', 'LOS - Зээлийн Санал Өгөх Систем', 'Системийн нэр', true);

-- Sequence-ууд reset хийх (PostgreSQL)
-- ALTER SEQUENCE users_id_seq RESTART WITH 4;
-- ALTER SEQUENCE customers_id_seq RESTART WITH 4;
-- ALTER SEQUENCE loan_applications_id_seq RESTART WITH 4;
-- ALTER SEQUENCE loan_products_id_seq RESTART WITH 5;

COMMIT;