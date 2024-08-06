CREATE DATABASE user_management;

USE user_management;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_full_name ON users (full_name);

ALTER TABLE users ADD FULLTEXT INDEX ft_full_name (full_name);

DESCRIBE users;

SHOW INDEX FROM users;

ALTER TABLE users ADD INDEX idx_full_name_prefix (full_name(10));