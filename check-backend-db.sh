#!/bin/bash
# Script to check which database the backend and admin-backend are using

export KUBECONFIG=/root/.kube/config

echo "=== Backend Service Database ==="
echo "DATABASE_URL from backend deployment:"
kubectl get deployment backend -n dev -o jsonpath='{.spec.template.spec.containers[0].env[?(@.name=="DATABASE_URL")].value}' 2>/dev/null || echo "Not found"
echo ""

echo "Database name from backend secret:"
kubectl get secret backend-db-credentials -n dev -o jsonpath='{.data.postgres-database}' 2>/dev/null | base64 -d || echo "Not found"
echo ""

echo ""
echo "=== Admin-Backend Service Backend Database ==="
echo "BACKEND_DATABASE_URL from admin-backend deployment:"
kubectl get deployment admin-backend -n dev -o jsonpath='{.spec.template.spec.containers[0].env[?(@.name=="BACKEND_DATABASE_URL")].value}' 2>/dev/null || echo "Not found"
echo ""

echo "Database name from backend-db-credentials secret (used by admin-backend):"
kubectl get secret backend-db-credentials -n dev -o jsonpath='{.data.postgres-database}' 2>/dev/null | base64 -d || echo "Not found"
echo ""

echo ""
echo "=== Checking if users table exists in backend database ==="
BACKEND_POD=$(kubectl get pods -n dev -l app.kubernetes.io/name=backend -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ -n "$BACKEND_POD" ]; then
  echo "Connecting to backend pod: $BACKEND_POD"
  kubectl exec -n dev $BACKEND_POD -- env | grep DATABASE_URL || echo "Could not get DATABASE_URL"
else
  echo "Backend pod not found"
fi

echo ""
echo "=== Checking admin-backend pod logs for database connection ==="
ADMIN_BACKEND_POD=$(kubectl get pods -n dev -l app.kubernetes.io/name=admin-backend -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ -n "$ADMIN_BACKEND_POD" ]; then
  echo "Admin-backend pod: $ADMIN_BACKEND_POD"
  kubectl exec -n dev $ADMIN_BACKEND_POD -- env | grep BACKEND_DATABASE_URL || echo "Could not get BACKEND_DATABASE_URL"
  echo ""
  echo "Recent logs mentioning 'users' table:"
  kubectl logs -n dev $ADMIN_BACKEND_POD --tail=50 | grep -i "users\|database" | tail -10 || echo "No relevant logs"
else
  echo "Admin-backend pod not found"
fi

