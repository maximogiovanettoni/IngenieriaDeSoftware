-- Add is_free column to order_items table for tracking free items from promotions
ALTER TABLE order_items
ADD COLUMN is_free BOOLEAN NOT NULL DEFAULT FALSE;
