CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

DROP TABLE IF EXISTS order_details;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_category_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    category_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE,
    INDEX idx_product_category (category_id),
    INDEX idx_product_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_date DATETIME NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    user_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_orderdetails_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_orderdetails_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    INDEX idx_orderdetails_order (order_id),
    INDEX idx_orderdetails_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO categories (name, description, deleted) VALUES
('Electronics', 'Smartphones, laptops, tablets, and tech accessories', FALSE),
('Books', 'Fiction, non-fiction, academic, and technical books', FALSE),
('Clothing', 'Men and women apparel, footwear, and accessories', FALSE),
('Home & Kitchen', 'Kitchen appliances, cookware, and home essentials', FALSE);

INSERT INTO products (name, price, stock_quantity, category_id, deleted) VALUES
('Apple MacBook Pro 16"', 2499.00, 15, 1, FALSE),
('Dell XPS 15 Laptop', 1899.50, 25, 1, FALSE),
('Sony WH-1000XM5 Headphones', 399.99, 40, 1, FALSE),
('Clean Code by Robert C. Martin', 45.00, 60, 2, FALSE),
('Designing Data-Intensive Applications', 55.50, 35, 2, FALSE),
('Men Cotton Casual Shirt', 34.99, 80, 3, FALSE),
('Instant Pot Duo 7-in-1', 99.95, 20, 4, FALSE);

INSERT INTO users (username, password, email, role, deleted) VALUES
('admin_user', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@ecommerce.com', 'ADMIN', FALSE),
('alice_smith', '$2a$12$e8Yy8f6eOcvzG/m5gWb64.n2gD2GzXfGjM4hE8t7r4xS3H6aY3Ryy', 'alice@example.com', 'CUSTOMER', FALSE),
('bob_johnson', '$2a$12$e8Yy8f6eOcvzG/m5gWb64.n2gD2GzXfGjM4hE8t7r4xS3H6aY3Ryy', 'bob@example.com', 'CUSTOMER', FALSE);

INSERT INTO orders (order_date, total_amount, user_id, deleted) VALUES
(NOW(), 2544.00, 2, FALSE);

INSERT INTO order_details (order_id, product_id, quantity, unit_price, deleted) VALUES
(1, 1, 1, 2499.00, FALSE),
(1, 4, 1, 45.00, FALSE);
