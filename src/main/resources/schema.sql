CREATE TABLE IF NOT EXISTS users_erp (
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS suppliers_erp (
  supplier_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplier_name VARCHAR(100) NOT NULL,
  contact_person VARCHAR(100),
  phone VARCHAR(30),
  email VARCHAR(150),
  address VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS products_erp (
  product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_name VARCHAR(100) NOT NULL,
  category VARCHAR(100),
  description TEXT,
  price DECIMAL(12,2) NOT NULL DEFAULT 0,
  reorder_level INT NOT NULL DEFAULT 0,
  supplier_id BIGINT,
  CONSTRAINT fk_products_supplier FOREIGN KEY (supplier_id)
    REFERENCES suppliers_erp(supplier_id) ON DELETE SET NULL,
  CONSTRAINT chk_products_price CHECK (price >= 0),
  CONSTRAINT chk_products_reorder CHECK (reorder_level >= 0)
);

CREATE TABLE IF NOT EXISTS inventory_erp (
  inventory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL UNIQUE,
  quantity INT NOT NULL DEFAULT 0,
  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_inventory_product FOREIGN KEY (product_id)
    REFERENCES products_erp(product_id) ON DELETE CASCADE,
  CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0)
);

CREATE TABLE IF NOT EXISTS transactions_erp (
  transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  transaction_type VARCHAR(30) NOT NULL,
  quantity INT NOT NULL,
  transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_transactions_product FOREIGN KEY (product_id)
    REFERENCES products_erp(product_id) ON DELETE CASCADE
);
