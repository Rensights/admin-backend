-- Migration script to add 'active' column to deals table
-- Run this script on your database

ALTER TABLE deals 
ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT true;

-- Update existing approved deals to be active by default
UPDATE deals 
SET active = true 
WHERE active IS NULL OR active = false;

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_deals_status_active ON deals(status, active);
CREATE INDEX IF NOT EXISTS idx_deals_status_active_city ON deals(status, active, city);

