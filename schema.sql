-- =============================================================================
-- SMART FOOD DELIVERY APPLICATION - MYSQL DATABASE SCHEMA & INITIAL SEED DATA
-- Database Target: MySQL 5.7+ / 8.0+
-- Project Framework: Spring Boot (Spring Data JPA) & Modern Responsive Web Portals
-- Database Name: smartfood_db
-- =============================================================================

CREATE DATABASE IF NOT EXISTS smartfood_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE smartfood_db;

-- Disable Foreign Key Checks during table creation
SET FOREIGN_KEY_CHECKS = 0;

-- Drop existing tables in dependency order
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS deliveries;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS item_options;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS merchants;
DROP TABLE IF EXISTS user_addresses;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 1. USER MANAGEMENT MODULE
-- -----------------------------------------------------------------------------

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER', -- CUSTOMER, MERCHANT, DRIVER, ADMIN
    profile_image_url VARCHAR(500) DEFAULT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Roles table (RBAC support)
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE, -- ROLE_CUSTOMER, ROLE_MERCHANT, ROLE_DRIVER, ROLE_ADMIN
    description VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User Roles junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User Saved Addresses table
CREATE TABLE user_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    label VARCHAR(50) NOT NULL, -- e.g. 'Home', 'Work', 'Gym'
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL DEFAULT 'Phnom Penh',
    latitude DOUBLE DEFAULT NULL,
    longitude DOUBLE DEFAULT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 2. MERCHANT MANAGEMENT MODULE
-- -----------------------------------------------------------------------------

-- Merchants table
CREATE TABLE merchants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    store_name VARCHAR(150) NOT NULL,
    description TEXT DEFAULT NULL,
    store_logo_url VARCHAR(500) DEFAULT NULL,
    store_banner_url VARCHAR(500) DEFAULT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL DEFAULT 'Phnom Penh',
    phone VARCHAR(20) NOT NULL,
    cuisine_type VARCHAR(100) DEFAULT 'Various',
    opening_hours VARCHAR(100) DEFAULT '08:00 AM - 10:00 PM',
    rating DECIMAL(3,2) DEFAULT 4.80,
    delivery_fee DECIMAL(10,2) DEFAULT 1.50,
    delivery_time_mins INT DEFAULT 25,
    is_open BOOLEAN DEFAULT TRUE,
    status VARCHAR(50) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Categories table
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    display_order INT DEFAULT 0,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Menu Items table
CREATE TABLE menu_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    category_id BIGINT DEFAULT NULL,
    category_name VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT DEFAULT NULL,
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(500) DEFAULT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    popular_score INT DEFAULT 0,
    prep_time_minutes INT DEFAULT 15,
    dietary_tag VARCHAR(50) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Item Customization Options table
CREATE TABLE item_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_item_id BIGINT NOT NULL,
    option_group VARCHAR(100) NOT NULL, -- e.g. 'Patty Size', 'Spice Level', 'Add-Ons'
    option_name VARCHAR(100) NOT NULL,  -- e.g. 'Double Patty', 'Extra Cheese'
    price_adjustment DECIMAL(10,2) DEFAULT 0.00,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 3. SHOPPING CART MODULE
-- -----------------------------------------------------------------------------

-- Carts table
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    merchant_id BIGINT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cart Items table
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    menu_item_id BIGINT NOT NULL,
    item_name VARCHAR(150) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    selected_options VARCHAR(500) DEFAULT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 4. ORDER MANAGEMENT MODULE
-- -----------------------------------------------------------------------------

-- Orders table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    merchant_name VARCHAR(150) NOT NULL,
    driver_id BIGINT DEFAULT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, PREPARING, READY_FOR_PICKUP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    subtotal DECIMAL(10,2) NOT NULL,
    delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 1.50,
    total_amount DECIMAL(10,2) NOT NULL,
    delivery_address VARCHAR(255) NOT NULL,
    payment_status VARCHAR(30) DEFAULT 'PAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE RESTRICT,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Order Items table
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    menu_item_id BIGINT DEFAULT NULL,
    menu_item_name VARCHAR(150) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    options_summary VARCHAR(255) DEFAULT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 5. PAYMENT MANAGEMENT MODULE
-- -----------------------------------------------------------------------------

-- Payments table
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- KHQR, CARD, CASH_ON_DELIVERY
    transaction_ref VARCHAR(100) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, PENDING, FAILED
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 6. DELIVERY MANAGEMENT MODULE
-- -----------------------------------------------------------------------------

-- Deliveries table
CREATE TABLE deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ASSIGNED', -- ASSIGNED, PICKED_UP, DELIVERED
    pickup_time TIMESTAMP NULL DEFAULT NULL,
    delivered_time TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------------
-- 7. NOTIFICATION MODULE
-- -----------------------------------------------------------------------------

-- Notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================================
-- SEED INITIAL DATA (Bcrypt password for all demo accounts is 'password123')
-- =============================================================================

-- Seed Roles
INSERT INTO roles (id, name, description) VALUES
    (1, 'ROLE_CUSTOMER', 'Standard customer account for browsing and ordering food'),
    (2, 'ROLE_MERCHANT', 'Merchant account for managing store profiles, menus, and incoming orders'),
    (3, 'ROLE_DRIVER', 'Driver account for accepting delivery jobs and updating order progress'),
    (4, 'ROLE_ADMIN', 'Platform administrator for managing users, merchants, and system analytics');

-- Seed Users (Bcrypt Hash: $2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa = 'password123')
INSERT INTO users (id, full_name, email, phone, password_hash, role, profile_image_url, is_active) VALUES
    (1, 'System Admin', 'admin@example.com', '+85511223344', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', 'ADMIN', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300', TRUE),
    (2, 'Bopha Merchant', 'merchant@example.com', '+85598765432', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', 'MERCHANT', 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=300', TRUE),
    (3, 'Dara Driver', 'driver@example.com', '+85588776655', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', 'DRIVER', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300', TRUE),
    (4, 'Sokha Customer', 'customer@example.com', '+85512345678', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', 'CUSTOMER', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300', TRUE);

-- Assign User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 4), -- System Admin -> ROLE_ADMIN
    (2, 2), -- Bopha Merchant -> ROLE_MERCHANT
    (3, 3), -- Dara Driver -> ROLE_DRIVER
    (4, 1); -- Sokha Customer -> ROLE_CUSTOMER

-- Seed User Addresses
INSERT INTO user_addresses (id, user_id, label, street_address, city, is_default) VALUES
    (1, 4, 'Home (BKK1)', 'Building 42, St. 302, BKK1', 'Phnom Penh', TRUE),
    (2, 4, 'Office (Toul Kork)', 'Vattanac Tower Level 14, Monivong Blvd', 'Phnom Penh', FALSE);

-- Seed Merchants
INSERT INTO merchants (id, owner_user_id, store_name, description, store_logo_url, store_banner_url, address, city, phone, cuisine_type, opening_hours, rating, delivery_fee, delivery_time_mins, is_open, status) VALUES
    (1, 2, 'Zando Burger & Grill', 'Juicy artisan smash burgers, crispy truffle fries, and premium thick milkshakes crafted daily.', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=1200&auto=format&fit=crop&q=80', 'Street 271, Sangkat Phsar Doeum Thkov', 'Phnom Penh', '+855 23 888 999', 'Gourmet American', '10:00 AM - 10:30 PM', 4.80, 1.50, 20, TRUE, 'Active'),
    (2, 2, 'Sakura Sushi & Ramen Bar', 'Authentic Japanese tonkotsu ramen bowls, torched salmon aburi rolls, and fresh seasonal sashimi.', 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=1200&auto=format&fit=crop&q=80', 'Monivong Blvd, Boeung Keng Kang 1', 'Phnom Penh', '+855 23 777 666', 'Japanese Artisan', '11:00 AM - 11:00 PM', 4.90, 2.00, 30, TRUE, 'Active'),
    (3, 2, 'Khmer Coffee & Patisserie', 'Traditional slow-drip Mondulkiri iced coffee, French butter croissants, and fresh coconut pandan waffles.', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600&auto=format&fit=crop&q=80', 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1200&auto=format&fit=crop&q=80', 'Norodom Blvd, Daun Penh', 'Phnom Penh', '+855 23 555 444', 'Café & Bakery', '07:00 AM - 09:00 PM', 4.90, 1.00, 15, TRUE, 'Active');

-- Seed Categories
INSERT INTO categories (id, merchant_id, category_name, display_order) VALUES
    (1, 1, 'Signature Burgers', 1),
    (2, 1, 'Artisan Sides', 2),
    (3, 1, 'Craft Beverages', 3),
    (4, 2, 'Specialty Rolls', 1),
    (5, 2, 'Hot Broth Ramen', 2),
    (6, 3, 'Single Origin Coffee', 1),
    (7, 3, 'Fresh Bakery', 2);

-- Seed Menu Items
INSERT INTO menu_items (id, merchant_id, category_id, category_name, name, description, price, image_url, is_available, popular_score, prep_time_minutes, dietary_tag) VALUES
    (1, 1, 1, 'Signature Burgers', 'Double Truffle Smash Burger', 'Double black angus beef patties, aged white cheddar, sautéed portobello, and black truffle aioli on a toasted brioche bun.', 7.25, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=600&auto=format&fit=crop&q=80', TRUE, 99, 15, 'Chef Special'),
    (2, 1, 1, 'Signature Burgers', 'Spicy Nashville Crispy Chicken', 'Buttermilk-brined crispy chicken thigh coated with Nashville cayenne glaze, spicy pickled cucumbers, and house slaw.', 5.75, 'https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?w=600&auto=format&fit=crop&q=80', TRUE, 94, 12, 'Spicy'),
    (3, 1, 2, 'Artisan Sides', 'Parmesan Truffle French Fries', 'Hand-cut Idaho potato fries tossed with white truffle oil, freshly grated Grana Padano parmesan, and chives.', 2.95, 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=600&auto=format&fit=crop&q=80', TRUE, 88, 8, 'Vegetarian'),
    (4, 1, 3, 'Craft Beverages', 'Salted Caramel Shake', 'Hand-spun Madagascar vanilla gelato with sea salt caramel ribbon and whipped cream.', 3.50, 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=600&auto=format&fit=crop&q=80', TRUE, 82, 5, 'Dessert'),
    (5, 2, 4, 'Specialty Rolls', 'Torched Salmon Aburi Roll', '8 pieces. Fresh Norwegian salmon lightly torched with house spicy mayo, sweet unagi reduction, and tobiko.', 8.50, 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=600&auto=format&fit=crop&q=80', TRUE, 96, 18, 'Chef Special'),
    (6, 2, 5, 'Hot Broth Ramen', 'Signature Tonkotsu Black Ramen', '16-hour simmered pork bone broth, handcrafted springy noodles, slow-braised chashu pork, ajitsuke tamago egg, and black garlic oil.', 8.00, 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80', TRUE, 97, 15, 'Popular'),
    (7, 3, 6, 'Single Origin Coffee', 'Mondulkiri Drip Iced Latte', 'Double shot Mondulkiri mountain espresso over creamy condensed milk and crushed ice.', 2.75, 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=600&auto=format&fit=crop&q=80', TRUE, 95, 5, 'Coffee'),
    (8, 3, 7, 'Fresh Bakery', 'French Almond Butter Croissant', 'Twice-baked flaky butter pastry filled with rich almond frangipane cream and sliced toasted almonds.', 2.50, 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600&auto=format&fit=crop&q=80', TRUE, 90, 5, 'Vegetarian');

-- Seed Item Options
INSERT INTO item_options (id, menu_item_id, option_group, option_name, price_adjustment, is_available) VALUES
    (1, 1, 'Patty Size', 'Double 150g (Standard)', 0.00, TRUE),
    (2, 1, 'Patty Size', 'Triple Monster 225g', 2.25, TRUE),
    (3, 1, 'Add-Ons', 'Extra Aged Cheddar Slice', 0.75, TRUE),
    (4, 1, 'Add-Ons', 'Smoked Applewood Bacon', 1.25, TRUE),
    (5, 2, 'Spice Heat Level', 'Mild Heat', 0.00, TRUE),
    (6, 2, 'Spice Heat Level', 'Hot & Crispy', 0.00, TRUE),
    (7, 2, 'Spice Heat Level', 'Extra Blazing Ghost Pepper', 0.50, TRUE),
    (8, 5, 'Portion Size', '8 Pieces', 0.00, TRUE),
    (9, 5, 'Portion Size', '12 Pieces Party Size', 3.80, TRUE),
    (10, 6, 'Noodle Firmness', 'Standard Medium', 0.00, TRUE),
    (11, 6, 'Noodle Firmness', 'Hard / Firm (Katame)', 0.00, TRUE),
    (12, 7, 'Sweetness Level', '100% Full Sweet', 0.00, TRUE),
    (13, 7, 'Sweetness Level', '50% Less Sweet', 0.00, TRUE);

-- Seed Sample Active Order
INSERT INTO orders (id, order_number, user_id, merchant_id, merchant_name, driver_id, status, subtotal, delivery_fee, total_amount, delivery_address, payment_status) VALUES
    (1, 'ORD-1001', 4, 1, 'Zando Burger & Grill', 3, 'ACCEPTED', 10.20, 1.50, 11.70, 'Building 42, St. 302, BKK1, Phnom Penh', 'PAID');

INSERT INTO order_items (id, order_id, menu_item_id, menu_item_name, price, quantity, options_summary) VALUES
    (1, 1, 1, 'Double Truffle Smash Burger', 7.25, 1, 'Patty Size: Double 150g'),
    (2, 1, 3, 'Parmesan Truffle French Fries', 2.95, 1, 'Standard');

INSERT INTO payments (id, order_id, payment_method, transaction_ref, amount, status) VALUES
    (1, 1, 'KHQR', 'TXN-KHQR-98231', 11.70, 'SUCCESS');

-- =============================================================================
-- END OF SCRIPT
-- =============================================================================
