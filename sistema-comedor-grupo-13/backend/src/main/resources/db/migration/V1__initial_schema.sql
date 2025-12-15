-- ============================================
-- SISTEMA DE COMEDORES - SCHEMA INICIAL
-- ============================================

-- ============================================
-- TABLA: users
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    address VARCHAR(255) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    profile_image_url VARCHAR(500),
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    must_change_password BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- ============================================
-- TABLA: token
-- ============================================
CREATE TABLE IF NOT EXISTS token (
    id BIGSERIAL PRIMARY KEY,
    token_type VARCHAR(50) NOT NULL,
    value VARCHAR(512) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_token_value ON token(value);
CREATE INDEX idx_token_user_id ON token(user_id);

-- ============================================
-- TABLA: staff_deletion_audit
-- ============================================
CREATE TABLE IF NOT EXISTS staff_deletion_audit (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    staff_email VARCHAR(255),
    deleted_by VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP NOT NULL,
    reason TEXT
);

-- ============================================
-- TABLA: ingredient
-- ============================================
CREATE TABLE IF NOT EXISTS ingredient (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    unit_measure VARCHAR(50) NOT NULL,
    stock DECIMAL(10, 3),
    active BOOLEAN NOT NULL DEFAULT true,
    available BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_ingredient_active ON ingredient(active);
CREATE INDEX idx_ingredient_available ON ingredient(available);

-- ============================================
-- TABLA: ingredient_audit_log
-- ============================================
CREATE TABLE IF NOT EXISTS ingredient_audit_log (
    id BIGSERIAL PRIMARY KEY,
    ingredient_name VARCHAR(255) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    amount_delta DECIMAL(10, 3),
    reason VARCHAR(300)
);

CREATE INDEX idx_ingredient_audit_modified_at ON ingredient_audit_log(modified_at);

-- ============================================
-- TABLA: ingredient_observers
-- ============================================
CREATE TABLE IF NOT EXISTS ingredient_observers (
    id BIGSERIAL PRIMARY KEY,
    observer_type VARCHAR(50) NOT NULL,
    ingredient_id BIGINT NOT NULL,
    observer_id BIGINT NOT NULL,
    CONSTRAINT fk_ingredient_observer_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE CASCADE,
    CONSTRAINT uq_ingredient_observer UNIQUE (ingredient_id, observer_type, observer_id)
);

CREATE INDEX idx_ingredient_observers_ingredient ON ingredient_observers(ingredient_id);

-- ============================================
-- TABLA: products (herencia SINGLE_TABLE)
-- ============================================
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    product_type VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(300),
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    available BOOLEAN NOT NULL DEFAULT false,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_products_type ON products(product_type);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_available ON products(available);

-- ============================================
-- TABLA: product_audit_log
-- ============================================
CREATE TABLE IF NOT EXISTS product_audit_log (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    operation VARCHAR(50) NOT NULL,
    reason VARCHAR(300)
);

CREATE INDEX idx_product_audit_modified_at ON product_audit_log(modified_at);

-- ============================================
-- TABLA: product_observers
-- ============================================
CREATE TABLE IF NOT EXISTS product_observers (
    id BIGSERIAL PRIMARY KEY,
    observer_type VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    observer_id BIGINT NOT NULL,
    CONSTRAINT fk_product_observer_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_product_observer UNIQUE (product_id, observer_type, observer_id)
);

CREATE INDEX idx_product_observers_product ON product_observers(product_id);

-- ============================================
-- TABLA: elaborate_product_ingredients
-- ============================================
CREATE TABLE IF NOT EXISTS elaborate_product_ingredients (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    ingredient_id BIGINT NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL,
    CONSTRAINT fk_product_ingredient_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_ingredient_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(id) ON DELETE RESTRICT
);

CREATE INDEX idx_elaborate_product_ingredients_product ON elaborate_product_ingredients(product_id);
CREATE INDEX idx_elaborate_product_ingredients_ingredient ON elaborate_product_ingredients(ingredient_id);

-- ============================================
-- TABLA: product_ingredient_observers
-- ============================================
CREATE TABLE IF NOT EXISTS product_ingredient_observers (
    id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    CONSTRAINT fk_product_ingredient_observer_base FOREIGN KEY (id) REFERENCES ingredient_observers(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_ingredient_observer_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_ingredient_observers_product ON product_ingredient_observers(product_id);

-- ============================================
-- TABLA: combo_products
-- ============================================
CREATE TABLE IF NOT EXISTS combo_products (
    id BIGSERIAL PRIMARY KEY,
    combo_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_combo_product_combo FOREIGN KEY (combo_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_combo_product_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

CREATE INDEX idx_combo_products_combo ON combo_products(combo_id);
CREATE INDEX idx_combo_products_product ON combo_products(product_id);

-- ============================================
-- TABLA: combo_product_observers
-- ============================================
CREATE TABLE IF NOT EXISTS combo_product_observers (
    id BIGINT PRIMARY KEY,
    combo_id BIGINT NOT NULL,
    CONSTRAINT fk_combo_product_observer_base FOREIGN KEY (id) REFERENCES product_observers(id) ON DELETE CASCADE,
    CONSTRAINT fk_combo_product_observer_combo FOREIGN KEY (combo_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_combo_product_observers_combo ON combo_product_observers(combo_id);

-- ============================================
-- TABLA: promotions (herencia SINGLE_TABLE)
-- ============================================
CREATE TABLE IF NOT EXISTS promotions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    promotion_category VARCHAR(50) NOT NULL,
    description VARCHAR(300),
    active BOOLEAN NOT NULL DEFAULT true,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    -- Campos específicos de PercentageDiscount
    percentage DECIMAL(5, 2),
    -- Campos específicos de FixedDiscount
    discount_amount DECIMAL(10, 2),
    -- Campos específicos de BuyXPayY
    x_quantity INTEGER,
    y_quantity INTEGER,
    -- Campos específicos de BuyXGetY
    buy_quantity INTEGER,
    get_quantity INTEGER,
    product_to_buy_id BIGINT,
    product_to_get_id BIGINT,
    CONSTRAINT fk_promotion_product_buy FOREIGN KEY (product_to_buy_id) REFERENCES products(id) ON DELETE SET NULL,
    CONSTRAINT fk_promotion_product_get FOREIGN KEY (product_to_get_id) REFERENCES products(id) ON DELETE SET NULL
);

CREATE INDEX idx_promotions_type ON promotions(type);
CREATE INDEX idx_promotions_active ON promotions(active);
CREATE INDEX idx_promotions_category ON promotions(promotion_category);

-- ============================================
-- TABLA: promotion_days
-- ============================================
CREATE TABLE IF NOT EXISTS promotion_days (
    promotion_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    CONSTRAINT fk_promotion_days_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE
);

CREATE INDEX idx_promotion_days_promotion ON promotion_days(promotion_id);

-- ============================================
-- TABLA: promotion_hours
-- ============================================
CREATE TABLE IF NOT EXISTS promotion_hours (
    promotion_id BIGINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT fk_promotion_hours_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE
);

CREATE INDEX idx_promotion_hours_promotion ON promotion_hours(promotion_id);

-- ============================================
-- TABLA: orders
-- ============================================
CREATE TABLE IF NOT EXISTS orders (
    order_number BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- ============================================
-- TABLA: order_items (ElementCollection)
-- ============================================
CREATE TABLE IF NOT EXISTS order_items (
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal DECIMAL(10, 3) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_number) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- ============================================
-- TABLA: applied_promotions (ElementCollection)
-- ============================================
CREATE TABLE IF NOT EXISTS applied_promotions (
    order_id BIGINT NOT NULL,
    applied_promotion_name VARCHAR(255) NOT NULL,
    applied_promotion_type VARCHAR(50) NOT NULL,
    applied_discount DECIMAL(10, 2) NOT NULL,
    start_date DATE,
    end_date DATE,
    applicable_days TEXT,
    CONSTRAINT fk_applied_promotions_order FOREIGN KEY (order_id) REFERENCES orders(order_number) ON DELETE CASCADE
);

CREATE INDEX idx_applied_promotions_order ON applied_promotions(order_id);

-- ============================================
-- TABLA: order_state_update_events
-- ============================================
CREATE TABLE IF NOT EXISTS order_state_update_events (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    change_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_status_event_order FOREIGN KEY (order_id) REFERENCES orders(order_number) ON DELETE CASCADE
);

CREATE INDEX idx_order_state_update_events_order ON order_state_update_events(order_id);
CREATE INDEX idx_order_state_update_events_changed_at ON order_state_update_events(changed_at);
