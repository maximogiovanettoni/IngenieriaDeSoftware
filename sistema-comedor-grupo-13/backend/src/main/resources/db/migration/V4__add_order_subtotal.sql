-- Add subtotal column to orders (used by Order entity)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS subtotal numeric(10,3) NOT NULL DEFAULT 0;
