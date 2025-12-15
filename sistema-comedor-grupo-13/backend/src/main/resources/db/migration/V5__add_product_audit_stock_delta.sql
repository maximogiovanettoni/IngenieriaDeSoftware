-- Add stock_delta column to product_audit_log if it doesn't exist
-- First, add the column as nullable if it doesn't exist
ALTER TABLE IF EXISTS product_audit_log
  ADD COLUMN IF NOT EXISTS stock_delta integer;

-- Update existing NULL values to 0
UPDATE product_audit_log SET stock_delta = 0 WHERE stock_delta IS NULL;

-- Add NOT NULL constraint if the column was just created
ALTER TABLE product_audit_log
  ALTER COLUMN stock_delta SET NOT NULL;

-- Add DEFAULT if not present
ALTER TABLE product_audit_log
  ALTER COLUMN stock_delta SET DEFAULT 0;
