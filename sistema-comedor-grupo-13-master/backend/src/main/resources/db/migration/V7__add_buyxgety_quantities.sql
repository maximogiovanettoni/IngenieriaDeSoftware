-- Add quantity columns to promotion table for BUY_X_GET_Y type
ALTER TABLE promotions
ADD COLUMN IF NOT EXISTS required_quantity INTEGER DEFAULT 1,
ADD COLUMN IF NOT EXISTS free_quantity INTEGER DEFAULT 1;

-- Update existing BUY_X_GET_Y promotions to have default values
UPDATE promotions 
SET required_quantity = 1, free_quantity = 1 
WHERE type = 'BUY_X_GET_Y';
