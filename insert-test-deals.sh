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

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f insert-dummy-deals.sql

unset PGPASSWORD

echo "Test deals inserted successfully!"

