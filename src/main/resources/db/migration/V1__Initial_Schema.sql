-- V1__Initial_Schema.sql
-- Initial database schema for AutoParts Hub

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Sellers Table
CREATE TABLE sellers (
    user_id BIGINT PRIMARY KEY,
    business_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    bank_account_token VARCHAR(255),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    average_rating DECIMAL(3,2),
    total_ratings INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Categories Table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    parent_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- Brands Table
CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    logo_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Listings Table
CREATE TABLE listings (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    seller_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT,
    condition VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    oem_part_number VARCHAR(100),
    aftermarket_part_number VARCHAR(100),
    quantity INTEGER NOT NULL,
    warranty_information VARCHAR(500),
    average_rating DECIMAL(3,2),
    total_ratings INTEGER,
    active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (seller_id) REFERENCES sellers(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (brand_id) REFERENCES brands(id)
);

-- Listing Images Table
CREATE TABLE listing_images (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE
);

-- Compatibility Mappings Table
CREATE TABLE compatibility_mappings (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year_start INTEGER NOT NULL,
    year_end INTEGER NOT NULL,
    engine VARCHAR(100),
    trim VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE
);

-- Orders Table
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    shipping_cost DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    shipping_address TEXT NOT NULL,
    shipping_city VARCHAR(100) NOT NULL,
    shipping_state VARCHAR(100) NOT NULL,
    shipping_postal_code VARCHAR(20) NOT NULL,
    shipping_country VARCHAR(100) NOT NULL,
    shipping_method VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    tracking_number VARCHAR(100),
    payment_method VARCHAR(50) NOT NULL,
    payment_transaction_id VARCHAR(100),
    notes TEXT,
    payment_date TIMESTAMP,
    shipped_date TIMESTAMP,
    delivered_date TIMESTAMP,
    cancelled_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- Order Items Table
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    listing_title VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (listing_id) REFERENCES listings(id),
    FOREIGN KEY (seller_id) REFERENCES sellers(user_id)
);

-- Notifications Table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(500) NOT NULL,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    action_url VARCHAR(255),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Reviews Table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT,
    reviewer_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    comment VARCHAR(1000) NOT NULL,
    image_url VARCHAR(255),
    verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    seller_response VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE SET NULL,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_listings_seller ON listings(seller_id);
CREATE INDEX idx_listings_category ON listings(category_id);
CREATE INDEX idx_listings_brand ON listings(brand_id);
CREATE INDEX idx_compatibility_listing ON compatibility_mappings(listing_id);
CREATE INDEX idx_compatibility_vehicle ON compatibility_mappings(make, model, year_start, year_end);
CREATE INDEX idx_listing_images_listing ON listing_images(listing_id);
CREATE INDEX idx_categories_parent ON categories(parent_id);

-- Indexes for orders
CREATE INDEX idx_orders_buyer ON orders(buyer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_listing ON order_items(listing_id);
CREATE INDEX idx_order_items_seller ON order_items(seller_id);

-- Indexes for notifications and reviews
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_reviews_order_item ON reviews(order_item_id);
CREATE INDEX idx_reviews_reviewer ON reviews(reviewer_id);
CREATE INDEX idx_reviews_target ON reviews(target_id);
CREATE INDEX idx_reviews_type ON reviews(type);
