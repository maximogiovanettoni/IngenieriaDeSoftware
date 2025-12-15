-- Remove unused stock_delta column from product_audit_log
ALTER TABLE IF EXISTS product_audit_log
  DROP COLUMN IF EXISTS stock_delta;
