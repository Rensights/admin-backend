-- Insert dummy deals for testing
-- Run this script to add test deals to the database

-- Clear existing test deals (optional - uncomment if needed)
-- DELETE FROM deals WHERE status = 'PENDING' AND batch_date::date = CURRENT_DATE;

-- Insert 10 dummy deals for today's batch
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
-- Deal 1
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
-- Deal 2
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
),
-- Deal 3
(
    gen_random_uuid(),
    'Business Bay Executive',
    'Business Bay',
    'dubai',
    'business-bay',
    'Studio',
    'studio',
    '580 sq ft',
    'AED 895,000',
    895000,
    1080000,
    1125000,
    'AED 1,080,000 - 1,125,000',
    '17.8%',
    '7.2%',
    'OFF_PLAN',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Deal 4
(
    gen_random_uuid(),
    'Jumeirah Beach Residence',
    'JBR',
    'dubai',
    'jumeirah',
    '2BR',
    '2',
    '1,250 sq ft',
    'AED 3,250,000',
    3250000,
    3950000,
    4100000,
    'AED 3,950,000 - 4,100,000',
    '18.9%',
    '6.1%',
    'READY',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Deal 5
(
    gen_random_uuid(),
    'DIFC Financial Plaza',
    'DIFC',
    'dubai',
    'downtown',
    '1BR',
    '1',
    '850 sq ft',
    'AED 1,750,000',
    1750000,
    2120000,
    2200000,
    'AED 2,120,000 - 2,200,000',
    '18.4%',
    '6.5%',
    'READY',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Deal 6
(
    gen_random_uuid(),
    'Palm Jumeirah Shoreline',
    'Palm Jumeirah',
    'dubai',
    'jumeirah',
    '3BR',
    '3',
    '1,680 sq ft',
    'AED 4,850,000',
    4850000,
    5850000,
    6050000,
    'AED 5,850,000 - 6,050,000',
    '17.5%',
    '5.4%',
    'READY',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Deal 7
(
    gen_random_uuid(),
    'Marina Walk Promenade',
    'Dubai Marina',
    'dubai',
    'marina',
    'Studio',
    'studio',
    '520 sq ft',
    'AED 785,000',
    785000,
    950000,
    985000,
    'AED 950,000 - 985,000',
    '18.6%',
    '7.5%',
    'OFF_PLAN',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Deal 8
(
    gen_random_uuid(),
    'Emaar Boulevard Heights',
    'Downtown Dubai',
    'dubai',
    'downtown',
    '2BR',
    '2',
    '1,150 sq ft',
    'AED 3,150,000',
    3150000,
    3800000,
    3950000,
    'AED 3,800,000 - 3,950,000',
    '17.9%',
    '5.7%',
    'READY',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Deal 9
(
    gen_random_uuid(),
    'Business Bay Canal View',
    'Business Bay',
    'dubai',
    'business-bay',
    '1BR',
    '1',
    '680 sq ft',
    'AED 1,185,000',
    1185000,
    1425000,
    1480000,
    'AED 1,425,000 - 1,480,000',
    '17.6%',
    '6.9%',
    'OFF_PLAN',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
-- Deal 10
(
    gen_random_uuid(),
    'The Beach Residence',
    'JBR',
    'dubai',
    'jumeirah',
    '1BR',
    '1',
    '720 sq ft',
    'AED 1,650,000',
    1650000,
    1990000,
    2065000,
    'AED 1,990,000 - 2,065,000',
    '18.1%',
    '6.3%',
    'READY',
    'PENDING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT DO NOTHING;

-- Verify the inserts
SELECT 
    COUNT(*) as total_pending,
    COUNT(*) FILTER (WHERE building_status = 'READY') as ready_count,
    COUNT(*) FILTER (WHERE building_status = 'OFF_PLAN') as offplan_count
FROM deals
WHERE status = 'PENDING' 
  AND batch_date::date = CURRENT_DATE;

-- Show sample of inserted deals
SELECT 
    name,
    location,
    city,
    bedrooms,
    listed_price,
    discount,
    rental_yield,
    building_status,
    status
FROM deals
WHERE status = 'PENDING' 
  AND batch_date::date = CURRENT_DATE
ORDER BY created_at DESC
LIMIT 10;

