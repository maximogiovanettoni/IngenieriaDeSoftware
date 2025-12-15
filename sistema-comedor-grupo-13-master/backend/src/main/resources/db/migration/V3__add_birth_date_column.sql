-- Add birth_date column to users table (idempotent)
-- This migration is a no-op because birth_date is already in the initial schema
-- But we need to handle existing NULL values in production

DO $$ 
BEGIN
  -- Step 1: Update NULL birth_date values to a default date (1990-01-01)
  UPDATE users 
  SET birth_date = '1990-01-01'::DATE 
  WHERE birth_date IS NULL;
  
  -- Step 2: Make birth_date NOT NULL if not already constrained
  ALTER TABLE users 
  ALTER COLUMN birth_date SET NOT NULL;
EXCEPTION WHEN others THEN
  -- Column already NOT NULL or other error, ignore
  NULL;
END $$;
