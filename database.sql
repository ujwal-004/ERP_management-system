CREATE DATABASE IF NOT EXISTS ERP_Management_System;

USE ERP_Management_System;

CREATE TABLE Users_ERP (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(255),
    role VARCHAR(50)
);

CREATE TABLE Products_ERP (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    price DECIMAL(10,2),
    reorder_level INT
);

CREATE TABLE Suppliers_ERP (
    supplier_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT
);

CREATE TABLE Inventory_ERP (
    inventory_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT,
    quantity INT NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(product_id) REFERENCES Products_ERP(product_id)
);

CREATE TABLE Transactions_ERP (
    transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT,
    transaction_type VARCHAR(50),
    quantity INT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(product_id) REFERENCES Products_ERP(product_id)
);

CREATE TABLE Alerts_ERP (
    alert_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT,
    alert_message VARCHAR(255),
    alert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(product_id) REFERENCES Products_ERP(product_id)
);

ALTER TABLE Products_ERP ADD supplier_id BIGINT;
ALTER TABLE Products_ERP ADD CONSTRAINT fk_supplier FOREIGN KEY (supplier_id) REFERENCES Suppliers_ERP(supplier_id);
