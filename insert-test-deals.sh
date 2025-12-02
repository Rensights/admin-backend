#!/bin/bash

# Script to insert test deals into the backend database
# Usage: ./insert-test-deals.sh

# Database connection details (adjust these based on your environment)
DB_HOST="${BACKEND_DATABASE_HOST:-postgres-backend.dev.svc.cluster.local}"
DB_PORT="${BACKEND_DATABASE_PORT:-5432}"
DB_NAME="${BACKEND_DATABASE_NAME:-backend_db_dev}"
DB_USER="${BACKEND_DATABASE_USER:-backend_user}"
DB_PASSWORD="${BACKEND_DATABASE_PASSWORD:-password}"

echo "Connecting to database: $DB_NAME at $DB_HOST:$DB_PORT"

# Use PGPASSWORD environment variable for password
export PGPASSWORD="$DB_PASSWORD"

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" <<EOF
-- Insert test deals into the deals table
-- These deals will appear in the admin dashboard as pending deals for today

INSERT INTO deals (
    id,
    name,
    location,
    city,
    area,
    bedrooms,
    bedroom_count,
    size,
    listed_price,
    price_value,
    estimate_min,
    estimate_max,
    estimate_range,
    discount,
    rental_yield,
    building_status,
    status,
    batch_date,
    created_at,
    updated_at
) VALUES
(
    gen_random_uuid(),
    'Marina Pinnacle Tower',
    'Dubai Marina',
    'dubai',
    'marina',
    '1BR',
    '1',
    '750 sq ft',
    'AED 1,450,000',
    1450000,
    1750000,
    1820000,
    'AED 1,750,000 - 1,820,000',
    '18.2%',
    '6.8%',
    'READY',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    'Downtown Burj Vista',
    'Downtown Dubai',
    'dubai',
    'downtown',
    '2BR',
    '2',
    '1,100 sq ft',
    'AED 2,850,000',
    2850000,
    3450000,
    3580000,
    'AED 3,450,000 - 3,580,000',
    '19.1%',
    '5.9%',
    'READY',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT DO NOTHING;

-- Verify the inserts
SELECT 
    id,
    name,
    location,
    city,
    status,
    batch_date,
    created_at
FROM deals
WHERE status = 'PENDING'
ORDER BY created_at DESC
LIMIT 10;
EOF

unset PGPASSWORD

echo "Test deals inserted successfully!"

