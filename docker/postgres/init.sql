-- Үндсэн database ба user үүсгэх
CREATE DATABASE los_db;
CREATE USER los_user WITH PASSWORD 'los_password';
GRANT ALL PRIVILEGES ON DATABASE los_db TO los_user;

-- los_db database-д холбогдох
\c los_db

-- Харилцагчийн хүснэгт
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'BUSINESS')),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    register_number VARCHAR(10) UNIQUE,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Зээлийн хүсэлтийн хүснэгт
CREATE TABLE IF NOT EXISTS loan_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    loan_type VARCHAR(50) NOT NULL,
    requested_amount DECIMAL(15,2) NOT NULL,
    requested_term_months INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Жишээ өгөгдөл оруулах
INSERT INTO customers (customer_type, first_name, last_name, register_number, phone, email) 
VALUES 
('INDIVIDUAL', 'Батбаяр', 'Бат', 'УБ12345678', '+97699123456', 'batbayar@example.com'),
('INDIVIDUAL', 'Сарангэрэл', 'Дорж', 'УБ87654321', '+97699987654', 'saraa@example.com')
ON CONFLICT (register_number) DO NOTHING;

-- Эрхүүд олгох
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO los_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO los_user;
