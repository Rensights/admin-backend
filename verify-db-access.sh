#!/bin/bash

# Script to verify database access from within the cluster
# This checks what actually exists and what's accessible

set -e

NAMESPACE="dev"
KUBECONFIG="${KUBECONFIG:-/tmp/kubeconfig}"

export KUBECONFIG

echo "=========================================="
echo "Database Access Verification"
echo "=========================================="
echo ""

echo "1️⃣  Checking ALL services in $NAMESPACE namespace"
echo "-----------------------------------------------------------"
kubectl get svc -n $NAMESPACE
echo ""

echo "2️⃣  Looking for ANY PostgreSQL-related services"
echo "-----------------------------------------------------------"
kubectl get svc -n $NAMESPACE | grep -i postgres || echo "⚠️  No services with 'postgres' in name found"
kubectl get svc -n $NAMESPACE -o wide | grep -E "5432|postgres" || echo "⚠️  No services on port 5432 found"
echo ""

echo "3️⃣  Checking ALL pods in $NAMESPACE namespace"
echo "-----------------------------------------------------------"
kubectl get pods -n $NAMESPACE
echo ""

echo "4️⃣  Looking for PostgreSQL pods"
echo "-----------------------------------------------------------"
kubectl get pods -n $NAMESPACE | grep -i postgres || echo "⚠️  No pods with 'postgres' in name found"
echo ""

echo "5️⃣  Checking ALL secrets in $NAMESPACE namespace"
echo "-----------------------------------------------------------"
kubectl get secrets -n $NAMESPACE | grep db || echo "⚠️  No secrets with 'db' in name found"
echo ""

echo "6️⃣  Checking admin-backend pod details"
echo "-----------------------------------------------------------"
ADMIN_POD=$(kubectl get pods -n $NAMESPACE -l app=admin-backend -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
if [ -n "$ADMIN_POD" ]; then
    echo "Found admin-backend pod: $ADMIN_POD"
    echo ""
    echo "Environment variables related to database:"
    kubectl exec -n $NAMESPACE $ADMIN_POD -- env | grep -iE "DATABASE|POSTGRES|DB" || echo "No database env vars found"
    echo ""
    echo "Testing DNS resolution from pod:"
    echo "  - postgres-admin.dev.svc.cluster.local:"
    kubectl exec -n $NAMESPACE $ADMIN_POD -- nslookup postgres-admin.dev.svc.cluster.local 2>&1 || echo "    ❌ DNS lookup failed"
    echo ""
    echo "  - postgres-public.dev.svc.cluster.local:"
    kubectl exec -n $NAMESPACE $ADMIN_POD -- nslookup postgres-public.dev.svc.cluster.local 2>&1 || echo "    ❌ DNS lookup failed"
    echo ""
    echo "  - postgres-backend.dev.svc.cluster.local:"
    kubectl exec -n $NAMESPACE $ADMIN_POD -- nslookup postgres-backend.dev.svc.cluster.local 2>&1 || echo "    ❌ DNS lookup failed"
else
    echo "⚠️  admin-backend pod not found"
fi
echo ""

echo "7️⃣  Listing ALL services across ALL namespaces (looking for postgres)"
echo "-----------------------------------------------------------"
kubectl get svc --all-namespaces | grep -i postgres || echo "⚠️  No PostgreSQL services found in any namespace"
echo ""

echo "8️⃣  Checking StatefulSets and Deployments"
echo "-----------------------------------------------------------"
echo "StatefulSets:"
kubectl get statefulset -n $NAMESPACE | grep postgres || echo "  No postgres StatefulSets"
echo ""
echo "Deployments:"
kubectl get deployment -n $NAMESPACE | grep postgres || echo "  No postgres Deployments"
echo ""

echo "9️⃣  Checking service endpoints"
echo "-----------------------------------------------------------"
kubectl get endpoints -n $NAMESPACE | grep postgres || echo "⚠️  No postgres endpoints found"
echo ""

echo "🔟  Testing connection from debug pod"
echo "-----------------------------------------------------------"
echo "Creating temporary debug pod to test connections..."
kubectl run db-test-debug --image=postgres:15-alpine --rm -it --restart=Never -n $NAMESPACE -- \
    sh -c "
        echo 'Testing DNS resolution:'
        nslookup postgres-admin.dev.svc.cluster.local || echo '❌ postgres-admin not resolvable'
        nslookup postgres-public.dev.svc.cluster.local || echo '❌ postgres-public not resolvable'
        nslookup postgres-backend.dev.svc.cluster.local || echo '❌ postgres-backend not resolvable'
        echo ''
        echo 'Testing port connectivity:'
        nc -zv postgres-admin.dev.svc.cluster.local 5432 2>&1 || echo '❌ Cannot connect to postgres-admin:5432'
        nc -zv postgres-public.dev.svc.cluster.local 5432 2>&1 || echo '❌ Cannot connect to postgres-public:5432'
        nc -zv postgres-backend.dev.svc.cluster.local 5432 2>&1 || echo '❌ Cannot connect to postgres-backend:5432'
    " 2>&1 || echo "Debug pod test failed or pod doesn't have network access"

echo ""
echo "✅ Verification complete!"
echo ""
echo "📝 SUMMARY:"
echo "- Check if PostgreSQL services exist in the dev namespace"
echo "- If they don't exist, you need to deploy them first"
echo "- Verify the service names match what admin-backend expects"





