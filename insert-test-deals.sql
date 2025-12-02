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
);

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
ORDER BY created_at DESC;

