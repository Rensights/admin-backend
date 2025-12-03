# Insert Test Deals Data

This guide explains how to insert dummy deals data into the database for testing.

## Files

- `insert-dummy-deals.sql` - SQL script with 10 test deals
- `insert-test-deals.sh` - Shell script to execute the SQL file

## Prerequisites

- PostgreSQL client (`psql`) installed
- Access to the backend database
- Database credentials

## Method 1: Using the Shell Script (Recommended)

```bash
cd /Users/ihakhverdiyev/Desktop/rensights/services/admin-backend

# Set environment variables (if needed)
export BACKEND_DATABASE_HOST=postgres-backend.dev.svc.cluster.local
export BACKEND_DATABASE_PORT=5432
export BACKEND_DATABASE_NAME=backend_db_dev
export BACKEND_DATABASE_USER=backend_user
export BACKEND_DATABASE_PASSWORD=your_password

# Run the script
./insert-test-deals.sh
```

## Method 2: Using psql Directly

```bash
# Connect to the database
psql -h postgres-backend.dev.svc.cluster.local \
     -p 5432 \
     -U backend_user \
     -d backend_db_dev \
     -f insert-dummy-deals.sql
```

## Method 3: Using Kubernetes Pod (if running in K8s)

```bash
# Find the database pod
kubectl get pods -n dev | grep postgres-backend

# Copy SQL file to pod
kubectl cp insert-dummy-deals.sql <pod-name>:/tmp/ -n dev

# Execute SQL in pod
kubectl exec -it <pod-name> -n dev -- psql -U backend_user -d backend_db_dev -f /tmp/insert-dummy-deals.sql
```

## What Gets Inserted

The script inserts **10 test deals** with:
- Status: `PENDING` (ready for admin approval)
- Batch Date: Today's date
- Various property types: Studio, 1BR, 2BR, 3BR
- Different areas: Marina, Downtown, Business Bay, Jumeirah
- Mix of Ready and Off-Plan properties
- Realistic pricing and discount percentages

## Verify Data

After inserting, you can verify the data:

```sql
-- Count pending deals for today
SELECT COUNT(*) FROM deals 
WHERE status = 'PENDING' 
  AND batch_date::date = CURRENT_DATE;

-- View all pending deals
SELECT name, location, city, bedrooms, listed_price, discount, status
FROM deals
WHERE status = 'PENDING'
ORDER BY created_at DESC;
```

## Notes

- The script uses `ON CONFLICT DO NOTHING` to prevent duplicate inserts
- All deals are set with `batch_date = CURRENT_TIMESTAMP` (today)
- Deals will appear in the admin dashboard at `/deals` page
- After approval, deals will be visible in the app-frontend at `/deals`

