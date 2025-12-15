-- Add missing promotion detail tables
-- These tables were missing from V1 but are needed for JPA @SecondaryTable mapping

-- ============================================
-- TABLA: buy_x_get_y_details (for BuyXGetY promotion)
-- ============================================
CREATE TABLE IF NOT EXISTS buy_x_get_y_details (
    promotion_id BIGINT PRIMARY KEY NOT NULL,
    required_category VARCHAR(50) NOT NULL,
    free_category VARCHAR(50) NOT NULL,
    required_quantity INTEGER NOT NULL,
    free_quantity INTEGER NOT NULL,
    CONSTRAINT fk_buy_x_get_y_details_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE
);

-- ============================================
-- TABLA: buy_x_pay_y_details (for BuyXPayY promotion)
-- ============================================
CREATE TABLE IF NOT EXISTS buy_x_pay_y_details (
    promotion_id BIGINT PRIMARY KEY NOT NULL,
    category VARCHAR(50) NOT NULL,
    required_quantity INTEGER NOT NULL,
    charged_quantity INTEGER NOT NULL,
    CONSTRAINT fk_buy_x_pay_y_details_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE
);

-- ============================================
-- TABLA: fixed_discount_details (for FixedDiscount promotion)
-- ============================================
CREATE TABLE IF NOT EXISTS fixed_discount_details (
    promotion_id BIGINT PRIMARY KEY NOT NULL,
    minimum_purchase DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_fixed_discount_details_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE
);

-- ============================================
-- TABLA: percentage_discount_details (for PercentageDiscount promotion)
-- ============================================
CREATE TABLE IF NOT EXISTS percentage_discount_details (
    promotion_id BIGINT PRIMARY KEY NOT NULL,
    category VARCHAR(50) NOT NULL,
    discount_multiplier DECIMAL(5, 2) NOT NULL,
    CONSTRAINT fk_percentage_discount_details_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE
);
