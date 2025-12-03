-- Create deals table in public_db_dev
-- This script creates the deals table with all required columns including the active column

CREATE TABLE IF NOT EXISTS deals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    area VARCHAR(100) NOT NULL,
    bedrooms VARCHAR(50) NOT NULL,
    bedroom_count VARCHAR(50),
    size VARCHAR(100) NOT NULL,
    listed_price VARCHAR(100) NOT NULL,
    price_value NUMERIC(15, 2) NOT NULL,
    estimate_min NUMERIC(15, 2),
    estimate_max NUMERIC(15, 2),
    estimate_range VARCHAR(200),
    discount VARCHAR(50),
    rental_yield VARCHAR(50),
    building_status VARCHAR(20) NOT NULL CHECK (building_status IN ('READY', 'OFF_PLAN')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    active BOOLEAN NOT NULL DEFAULT true,
    batch_date TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_deals_status ON deals(status);
CREATE INDEX IF NOT EXISTS idx_deals_status_active ON deals(status, active);
CREATE INDEX IF NOT EXISTS idx_deals_status_active_city ON deals(status, active, city);
CREATE INDEX IF NOT EXISTS idx_deals_city ON deals(city);
CREATE INDEX IF NOT EXISTS idx_deals_batch_date ON deals(batch_date);
CREATE INDEX IF NOT EXISTS idx_deals_created_at ON deals(created_at);

