#!/bin/bash

# Script to insert test deals into remote database
# Usage: ./insert-deals-remote.sh

SERVER_IP="72.62.40.154"
SERVER_USER="root"
SERVER_PASS="AKrBcJVirdIMlM6S8/hO"

echo "Connecting to server $SERVER_IP..."

# First, copy the SQL file to the server
echo "Copying SQL file to server..."
sshpass -p "$SERVER_PASS" scp -o StrictHostKeyChecking=no insert-dummy-deals.sql root@$SERVER_IP:/tmp/ 2>&1

if [ $? -ne 0 ]; then
    echo "Failed to copy file. Trying alternative method..."
    # Alternative: Create SQL file directly on server
    sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no root@$SERVER_IP "cat > /tmp/insert-dummy-deals.sql << 'EOFSQL'
$(cat insert-dummy-deals.sql)
EOFSQL
" 2>&1
fi

echo "Finding database pod..."
# Find the PostgreSQL pod
DB_POD=$(sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no root@$SERVER_IP "kubectl get pods -n dev -o name | grep postgres-backend | head -1 | sed 's|pod/||'" 2>&1)

if [ -z "$DB_POD" ]; then
    echo "Database pod not found. Trying alternative approach..."
    # Try to find any postgres pod
    DB_POD=$(sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no root@$SERVER_IP "kubectl get pods -A | grep postgres | grep backend | head -1 | awk '{print \$2}'" 2>&1)
    DB_NAMESPACE=$(sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no root@$SERVER_IP "kubectl get pods -A | grep postgres | grep backend | head -1 | awk '{print \$1}'" 2>&1)
    
    if [ -z "$DB_POD" ]; then
        echo "Error: Could not find database pod"
        exit 1
    fi
    
    echo "Found database pod: $DB_POD in namespace: $DB_NAMESPACE"
    
    # Copy SQL file to pod
    echo "Copying SQL file to database pod..."
    sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no root@$SERVER_IP "kubectl cp /tmp/insert-dummy-deals.sql $DB_NAMESPACE/$DB_POD:/tmp/insert-dummy-deals.sql" 2>&1
    
    # Execute SQL
    echo "Executing SQL in database..."
    sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no root@$SERVER_IP "kubectl exec -n $DB_NAMESPACE $DB_POD -- psql -U backend_user -d backend_db_dev -f /tmp/insert-dummy-deals.sql" 2>&1
else
    echo "Found database pod: $DB_POD"
    
    # Copy SQL file to pod
    echo "Copying SQL file to database pod..."
    sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no root@$SERVER_IP "kubectl cp /tmp/insert-dummy-deals.sql dev/$DB_POD:/tmp/insert-dummy-deals.sql" 2>&1
    
    # Execute SQL
    echo "Executing SQL in database..."
    sshpass -p "$SERVER_PASS" ssh -o StrictHostKeyChecking=no root@$SERVER_IP "kubectl exec -n dev $DB_POD -- psql -U backend_user -d backend_db_dev -f /tmp/insert-dummy-deals.sql" 2>&1
fi

echo "Done! Check the admin dashboard to see the deals."

