#!/bin/bash

# Script to find what database services actually exist and match them
# Usage: ./find-actual-db-services.sh

set -e

NAMESPACE="dev"
KUBECONFIG="${KUBECONFIG:-/tmp/kubeconfig}"

export KUBECONFIG

echo "=========================================="
echo "Finding Actual Database Services"
echo "=========================================="
echo ""

echo "1️⃣  All Services in $NAMESPACE namespace"
echo "-----------------------------------------------------------"
kubectl get svc -n $NAMESPACE -o wide
echo ""

echo "2️⃣  Services that might be databases (checking ports 5432, 3306, etc.)"
echo "-----------------------------------------------------------"
kubectl get svc -n $NAMESPACE -o json | jq -r '.items[] | select(.spec.ports[]?.port == 5432 or .spec.ports[]?.targetPort == 5432) | "\(.metadata.name) - \(.spec.clusterIP):\(.spec.ports[0].port)"' || echo "No services on port 5432"
echo ""

echo "3️⃣  All PostgreSQL-related resources"
echo "-----------------------------------------------------------"
echo "Services:"
kubectl get svc -n $NAMESPACE -o json | jq -r '.items[] | select(.metadata.name | contains("postgres") or contains("pg") or contains("db")) | "\(.metadata.name) - \(.spec.type) - \(.spec.clusterIP)"' || echo "None found"
echo ""
echo "Pods:"
kubectl get pods -n $NAMESPACE -o json | jq -r '.items[] | select(.metadata.name | contains("postgres") or contains("pg") or contains("db")) | "\(.metadata.name) - Status: \(.status.phase)"' || echo "None found"
echo ""

echo "4️⃣  Checking ALL namespaces for PostgreSQL"
echo "-----------------------------------------------------------"
echo "Services in all namespaces:"
kubectl get svc --all-namespaces -o json | jq -r '.items[] | select(.metadata.name | contains("postgres") or contains("pg")) | "\(.metadata.namespace)/\(.metadata.name) - \(.spec.clusterIP):\(.spec.ports[0].port)"' || echo "None found"
echo ""

echo "5️⃣  Current admin-backend configuration"
echo "-----------------------------------------------------------"
echo "Expected service names:"
echo "  - postgres-admin.dev.svc.cluster.local"
echo "  - postgres-public.dev.svc.cluster.local"
echo "  - postgres-backend.dev.svc.cluster.local"
echo ""
echo "Actual services found:"
kubectl get svc -n $NAMESPACE -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}' | while read svc; do
    if kubectl get svc "$svc" -n $NAMESPACE -o jsonpath='{.spec.clusterIP}' 2>/dev/null | grep -qE "^[0-9]"; then
        CLUSTER_IP=$(kubectl get svc "$svc" -n $NAMESPACE -o jsonpath='{.spec.clusterIP}')
        PORT=$(kubectl get svc "$svc" -n $NAMESPACE -o jsonpath='{.spec.ports[0].port}')
        echo "  - $svc.dev.svc.cluster.local -> $CLUSTER_IP:$PORT"
    fi
done
echo ""

echo "6️⃣  What admin-backend pod sees"
echo "-----------------------------------------------------------"
ADMIN_POD=$(kubectl get pods -n $NAMESPACE -l app=admin-backend -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
if [ -n "$ADMIN_POD" ]; then
    echo "From admin-backend pod ($ADMIN_POD):"
    echo ""
    echo "Database environment variables:"
    kubectl exec -n $NAMESPACE $ADMIN_POD -- env | grep -E "^ADMIN_DATABASE_URL|^PUBLIC_DATABASE_URL|^BACKEND_DATABASE_URL" || echo "  No database URLs set"
    echo ""
    echo "Testing actual service names that exist:"
    for svc in $(kubectl get svc -n $NAMESPACE -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}{end}'); do
        FULL_NAME="$svc.$NAMESPACE.svc.cluster.local"
        echo -n "  $FULL_NAME: "
        kubectl exec -n $NAMESPACE $ADMIN_POD -- sh -c "nslookup $FULL_NAME >/dev/null 2>&1 && echo '✅ resolvable' || echo '❌ not resolvable'" 2>/dev/null || echo "❌ error"
    done
else
    echo "⚠️  admin-backend pod not found"
fi
echo ""

echo "✅ Analysis complete!"
echo ""
echo "💡 RECOMMENDATIONS:"
echo "1. If services exist but with different names, update admin-backend.yaml"
echo "2. If services don't exist, deploy PostgreSQL databases first"
echo "3. If services exist in different namespace, update the host names"
echo "4. Check if databases are in a shared namespace (like 'default' or 'platform')"




