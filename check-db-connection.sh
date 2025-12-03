#!/bin/bash

# Script to check database connections and provide credentials
# Usage: ./check-db-connection.sh

set -e

NAMESPACE="dev"
KUBECONFIG="${KUBECONFIG:-/tmp/kubeconfig}"

echo "=========================================="
echo "Database Connection Check & Credentials"
echo "=========================================="
echo ""

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl not found. Please install kubectl."
    exit 1
fi

# Set kubeconfig
export KUBECONFIG

echo "1️⃣  Checking PostgreSQL Services in namespace: $NAMESPACE"
echo "-----------------------------------------------------------"
kubectl get svc -n $NAMESPACE | grep postgres || echo "⚠️  No postgres services found!"
echo ""

echo "2️⃣  Checking PostgreSQL Pods in namespace: $NAMESPACE"
echo "-----------------------------------------------------------"
kubectl get pods -n $NAMESPACE | grep postgres || echo "⚠️  No postgres pods found!"
echo ""

echo "3️⃣  Checking Secrets"
echo "-----------------------------------------------------------"
echo "Admin DB Credentials:"
kubectl get secret admin-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-username}' 2>/dev/null | base64 -d 2>/dev/null || echo "❌ Secret not found"
echo ""
echo "Public DB Credentials:"
kubectl get secret public-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-username}' 2>/dev/null | base64 -d 2>/dev/null || echo "❌ Secret not found"
echo ""
echo "Backend DB Credentials:"
kubectl get secret backend-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-username}' 2>/dev/null | base64 -d 2>/dev/null || echo "❌ Secret not found"
echo ""

echo "4️⃣  Extracting All Credentials"
echo "=========================================="
echo ""

# Admin DB Credentials
echo "📊 ADMIN DATABASE CREDENTIALS:"
echo "-----------------------------------------------------------"
ADMIN_USER=$(kubectl get secret admin-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-username}' 2>/dev/null | base64 -d 2>/dev/null || echo "")
ADMIN_PASS=$(kubectl get secret admin-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-password}' 2>/dev/null | base64 -d 2>/dev/null || echo "")
ADMIN_HOST=$(kubectl get secret admin-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-host}' 2>/dev/null | base64 -d 2>/dev/null || echo "postgres-admin.dev.svc.cluster.local")
ADMIN_PORT=$(kubectl get secret admin-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-port}' 2>/dev/null | base64 -d 2>/dev/null || echo "5432")
ADMIN_DB=$(kubectl get secret admin-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-database}' 2>/dev/null | base64 -d 2>/dev/null || echo "admin_db_dev")

if [ -n "$ADMIN_USER" ]; then
    echo "Host: $ADMIN_HOST"
    echo "Port: $ADMIN_PORT"
    echo "Database: $ADMIN_DB"
    echo "Username: $ADMIN_USER"
    echo "Password: $ADMIN_PASS"
    echo "Connection String: jdbc:postgresql://$ADMIN_HOST:$ADMIN_PORT/$ADMIN_DB"
    echo "External: jdbc:postgresql://72.62.40.154:<NODEPORT>/$ADMIN_DB"
else
    echo "❌ Admin DB credentials secret not found!"
fi
echo ""

# Public DB Credentials
echo "📊 PUBLIC DATABASE CREDENTIALS:"
echo "-----------------------------------------------------------"
PUBLIC_USER=$(kubectl get secret public-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-username}' 2>/dev/null | base64 -d 2>/dev/null || echo "")
PUBLIC_PASS=$(kubectl get secret public-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-password}' 2>/dev/null | base64 -d 2>/dev/null || echo "")
PUBLIC_HOST=$(kubectl get secret public-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-host}' 2>/dev/null | base64 -d 2>/dev/null || echo "postgres-public.dev.svc.cluster.local")
PUBLIC_PORT=$(kubectl get secret public-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-port}' 2>/dev/null | base64 -d 2>/dev/null || echo "5432")
PUBLIC_DB=$(kubectl get secret public-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-database}' 2>/dev/null | base64 -d 2>/dev/null || echo "public_db_dev")

if [ -n "$PUBLIC_USER" ]; then
    echo "Host: $PUBLIC_HOST"
    echo "Port: $PUBLIC_PORT"
    echo "Database: $PUBLIC_DB"
    echo "Username: $PUBLIC_USER"
    echo "Password: $PUBLIC_PASS"
    echo "Connection String: jdbc:postgresql://$PUBLIC_HOST:$PUBLIC_PORT/$PUBLIC_DB"
    echo "External: jdbc:postgresql://72.62.40.154:<NODEPORT>/$PUBLIC_DB"
else
    echo "❌ Public DB credentials secret not found!"
fi
echo ""

# Backend DB Credentials
echo "📊 BACKEND DATABASE CREDENTIALS:"
echo "-----------------------------------------------------------"
BACKEND_USER=$(kubectl get secret backend-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-username}' 2>/dev/null | base64 -d 2>/dev/null || echo "")
BACKEND_PASS=$(kubectl get secret backend-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-password}' 2>/dev/null | base64 -d 2>/dev/null || echo "")
BACKEND_HOST=$(kubectl get secret backend-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-host}' 2>/dev/null | base64 -d 2>/dev/null || echo "postgres-backend.dev.svc.cluster.local")
BACKEND_PORT=$(kubectl get secret backend-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-port}' 2>/dev/null | base64 -d 2>/dev/null || echo "5432")
BACKEND_DB=$(kubectl get secret backend-db-credentials -n $NAMESPACE -o jsonpath='{.data.postgres-database}' 2>/dev/null | base64 -d 2>/dev/null || echo "backend_db_dev")

if [ -n "$BACKEND_USER" ]; then
    echo "Host: $BACKEND_HOST"
    echo "Port: $BACKEND_PORT"
    echo "Database: $BACKEND_DB"
    echo "Username: $BACKEND_USER"
    echo "Password: $BACKEND_PASS"
    echo "Connection String: jdbc:postgresql://$BACKEND_HOST:$BACKEND_PORT/$BACKEND_DB"
    echo "External: jdbc:postgresql://72.62.40.154:<NODEPORT>/$BACKEND_DB"
else
    echo "❌ Backend DB credentials secret not found!"
fi
echo ""

echo "5️⃣  Checking Service NodePorts"
echo "=========================================="
echo ""
echo "PostgreSQL Services with NodePort:"
kubectl get svc -n $NAMESPACE -o wide | grep postgres || echo "No postgres services found"
echo ""

echo "6️⃣  Checking Kong Ingress"
echo "=========================================="
echo ""
kubectl get ingress -n $NAMESPACE | grep admin || echo "No admin ingress found"
echo ""

echo "✅ Check complete!"
echo ""
echo "📝 NOTES:"
echo "- If services don't exist, you need to deploy PostgreSQL databases"
echo "- To expose databases externally, change service type to NodePort"
echo "- External connection: 72.62.40.154:<NODEPORT>"







