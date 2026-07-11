-- Create Database if not exists
CREATE DATABASE IF NOT EXISTS food_order_db;
USE food_order_db;

-- 1. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    INDEX idx_user_email (email)
);

-- 3. User Roles Join Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Orders Table (linked to Users)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    user_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_order_user (user_id),
    INDEX idx_order_status (status)
);

-- 5. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    order_id BIGINT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_item_order (order_id)
);

-- 6. Order Status History Table
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_history_order (order_id)
);

-- Seed Initial Roles
INSERT IGNORE INTO roles (id, name) VALUES (1, 'ROLE_USER');
INSERT IGNORE INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');

-- Seed Default Admin User (username: admin@foodorder.com, password: admin123)
-- BCrypt hash for 'admin123': $2a$10$w0M9i6NfM.aZ2C/4jJcpeec33T1K0l3T/kZf.rB5hB2P4gU6zVdWy
INSERT IGNORE INTO users (id, full_name, email, phone_number, password) 
VALUES (1, 'System Admin', 'admin@foodorder.com', '9876543210', '$2a$10$w0M9i6NfM.aZ2C/4jJcpeec33T1K0l3T/kZf.rB5hB2P4gU6zVdWy');

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 2);
