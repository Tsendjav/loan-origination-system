CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT true
);
INSERT INTO users (username, password, role, is_active) VALUES ('admin', '$2a$10$XPFIw...hashed_password', 'SUPER_ADMIN', true);