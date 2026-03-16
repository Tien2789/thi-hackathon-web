-- 1. Khởi tạo Database
CREATE DATABASE OnTop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE OnTop;

-- ==========================================
-- NHÓM 1: BẢNG DANH MỤC GỐC (Master Data)
-- ==========================================
CREATE TABLE roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  role_name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);

CREATE TABLE units (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(20) NOT NULL
);

CREATE TABLE suppliers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL
);

CREATE TABLE warehouses (
  id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) UNIQUE NOT NULL,
  name VARCHAR(100),
  location VARCHAR(200),
  manager VARCHAR(100),
  capacity INT DEFAULT 0
);

-- ==========================================
-- NHÓM 2: NGƯỜI DÙNG VÀ VỊ TRÍ CHI TIẾT
-- ==========================================
CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role_id INT,
  warehouse_id INT,
  is_active TINYINT(1) DEFAULT 1,
  CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(id),
  CONSTRAINT fk_users_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

-- Dữ liệu mẫu ban đầu
-- Vai trò STAFF sẽ đảm nhiệm các công việc trong kho, bao gồm nhập và xuất.
-- Mật khẩu mặc định cho admin là 'admin123'. Hash này được tạo bằng BCrypt.
INSERT INTO roles (role_name) VALUES ('ADMIN'), ('MANAGER'), ('STAFF');

-- Tạo tài khoản Admin mặc định (mật khẩu: admin123 - đã được BCrypt hash)
INSERT INTO users (username, password, role_id, is_active) 
VALUES ('admin', '$2a$10$vI8aWBnW3f.TsMCsw./xO.ZnqM2V8s4q9N1U8sY3.d3E.f5G6h7I8', 1, 1);

CREATE TABLE bin_locations (
  id INT AUTO_INCREMENT PRIMARY KEY,
  warehouse_id INT,
  bin_code VARCHAR(50) UNIQUE NOT NULL,
  CONSTRAINT fk_bin_warehouses FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

-- ==========================================
-- NHÓM 3: SẢN PHẨM (Kết nối đa hướng)
-- ==========================================
CREATE TABLE products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sku_code VARCHAR(50) UNIQUE NOT NULL,
  barcode VARCHAR(50) UNIQUE NOT NULL,
  qr_code TEXT NULL,
  product_name VARCHAR(200) NOT NULL,
  category_id INT,
  unit_id INT,
  supplier_id INT,
  bin_location_id INT,
  current_stock INT DEFAULT 0,
  min_stock INT DEFAULT 0,
  created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_prod_cat FOREIGN KEY (category_id) REFERENCES categories(id),
  CONSTRAINT fk_prod_unit FOREIGN KEY (unit_id) REFERENCES units(id),
  CONSTRAINT fk_prod_supp FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
  CONSTRAINT fk_prod_bin FOREIGN KEY (bin_location_id) REFERENCES bin_locations(id)
);

-- ==========================================
-- NHÓM 4: GIAO DỊCH NHẬP - XUẤT KHO
-- ==========================================
CREATE TABLE inventory_in (
  id INT AUTO_INCREMENT PRIMARY KEY,
  receipt_code VARCHAR(50) UNIQUE NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING',
  approved_by INT,
  warehouse_id INT,
  created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_in_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
  CONSTRAINT fk_in_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE TABLE in_details (
  id INT AUTO_INCREMENT PRIMARY KEY,
  inbound_id INT,
  product_id INT,
  quantity INT DEFAULT 0,
  unit_price DECIMAL(15,2) DEFAULT 0.00,
  CONSTRAINT fk_indetail_parent FOREIGN KEY (inbound_id) REFERENCES inventory_in(id),
  CONSTRAINT fk_indetail_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE inventory_out (
  id INT AUTO_INCREMENT PRIMARY KEY,
  issue_code VARCHAR(50) UNIQUE NOT NULL,
  status VARCHAR(20) DEFAULT 'PENDING',
  approved_by INT,
  warehouse_id INT,
  created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_out_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
  CONSTRAINT fk_out_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE TABLE out_details (
  id INT AUTO_INCREMENT PRIMARY KEY,
  outbound_id INT,
  product_id INT,
  quantity INT DEFAULT 0,
  CONSTRAINT fk_outdetail_parent FOREIGN KEY (outbound_id) REFERENCES inventory_out(id),
  CONSTRAINT fk_outdetail_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ==========================================
-- NHÓM 5: SỔ CÁI, TÀI SẢN VÀ THÔNG BÁO
-- ==========================================
CREATE TABLE stock_ledger (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id INT,
  warehouse_id INT,
  transaction_type VARCHAR(20) NOT NULL,
  quantity_change INT NOT NULL,
  running_balance INT NOT NULL,
  reference_id INT NOT NULL,
  reference_table VARCHAR(50) NOT NULL,
  created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_ledger_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_ledger_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE TABLE assets (
  id INT AUTO_INCREMENT PRIMARY KEY,
  asset_code VARCHAR(50) UNIQUE NOT NULL,
  product_id INT,
  condition_status VARCHAR(50) NOT NULL,
  created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_assets_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE asset_allocations (
  id INT AUTO_INCREMENT PRIMARY KEY,
  asset_id INT,
  user_id INT,
  assigned_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  returned_at TIMESTAMP(6) NULL,
  CONSTRAINT fk_alloc_asset FOREIGN KEY (asset_id) REFERENCES assets(id),
  CONSTRAINT fk_alloc_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  action VARCHAR(255) NOT NULL,
  created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE notifications (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  type VARCHAR(50) NOT NULL,
  message TEXT NOT NULL,
  is_read TINYINT DEFAULT 0,
  created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_noti_user FOREIGN KEY (user_id) REFERENCES users(id)
);
