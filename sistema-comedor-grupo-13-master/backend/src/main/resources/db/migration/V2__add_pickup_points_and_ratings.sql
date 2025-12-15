-- Create pickup_points table
CREATE TABLE IF NOT EXISTS pickup_points (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    location VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true
);

-- Create ratings table
CREATE TABLE IF NOT EXISTS ratings (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INTEGER NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(order_id)
);

-- Add pickup_point_id to orders table if it doesn't exist
ALTER TABLE orders ADD COLUMN IF NOT EXISTS pickup_point_id BIGINT;

-- Add foreign key constraint
ALTER TABLE orders
ADD CONSTRAINT fk_orders_pickup_point FOREIGN KEY (pickup_point_id) REFERENCES pickup_points(id);

-- Insert default pickup points
INSERT INTO pickup_points (name, description, location, active) VALUES
('Mostrador', 'Retiro en el mostrador principal', 'Edificio Principal - Planta Baja', true),
('Ventanilla A', 'Ventanilla de retiro rápido A', 'Edificio Principal - Piso 1', true),
('Ventanilla B', 'Ventanilla de retiro rápido B', 'Edificio Principal - Piso 2', true),
('Vestíbulo', 'Retiro en el vestíbulo de entrada', 'Entrada Principal', true)
ON CONFLICT (name) DO NOTHING;
