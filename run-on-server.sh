#!/bin/bash
# Run this script directly on the server (72.62.40.154)
# Copy and paste this entire script and run it on the server

set -e

export KUBECONFIG=/tmp/kubeconfig 2>/dev/null || export KUBECONFIG=/root/.kube/config 2>/dev/null || true

echo "=========================================="
echo "DATABASE CONNECTION DIAGNOSTICS"
echo "=========================================="
echo ""

echo "1️⃣  ALL SERVICES in dev namespace"
echo "-----------------------------------------------------------"
kubectl get svc -n dev 2>/dev/null || echo "Cannot access dev namespace"
echo ""

echo "2️⃣  POSTGRES SERVICES"
echo "-----------------------------------------------------------"
kubectl get svc -n dev 2>/dev/null | grep -i postgres || echo "❌ NO POSTGRES SERVICES FOUND"
echo ""

echo "3️⃣  POSTGRES PODS"
echo "-----------------------------------------------------------"
kubectl get pods -n dev 2>/dev/null | grep -i postgres || echo "❌ NO POSTGRES PODS FOUND"
echo ""

echo "4️⃣  CHECKING ALL NAMESPACES"
echo "-----------------------------------------------------------"
kubectl get svc --all-namespaces 2>/dev/null | grep -i postgres || echo "❌ NO POSTGRES SERVICES IN ANY NAMESPACE"
echo ""

echo "5️⃣  ADMIN-BACKEND POD STATUS"
echo "-----------------------------------------------------------"
ADMIN_POD=$(kubectl get pods -n dev -l app=admin-backend -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
if [ -n "$ADMIN_POD" ]; then
    echo "✅ Found admin-backend pod: $ADMIN_POD"
    echo ""
    echo "Environment variables:"
    kubectl exec -n dev $ADMIN_POD -- env 2>/dev/null | grep -E "DATABASE|POSTGRES|DB" || echo "No DB env vars"
    echo ""
    echo "Testing DNS from pod:"
    kubectl exec -n dev $ADMIN_POD -- nslookup postgres-admin.dev.svc.cluster.local 2>&1 | head -5 || echo "❌ DNS lookup failed"
    kubectl exec -n dev $ADMIN_POD -- nslookup postgres-public.dev.svc.cluster.local 2>&1 | head -5 || echo "❌ DNS lookup failed"
    kubectl exec -n dev $ADMIN_POD -- nslookup postgres-backend.dev.svc.cluster.local 2>&1 | head -5 || echo "❌ DNS lookup failed"
else
    echo "❌ admin-backend pod not found"
fi
echo ""

echo "6️⃣  SECRETS"
echo "-----------------------------------------------------------"
kubectl get secrets -n dev 2>/dev/null | grep db || echo "No db secrets found"
echo ""

echo "7️⃣  ENDPOINTS (service to pod mapping)"
echo "-----------------------------------------------------------"
kubectl get endpoints -n dev 2>/dev/null | grep postgres || echo "No postgres endpoints"
echo ""

echo "8️⃣  STATEFULSETS & DEPLOYMENTS"
echo "-----------------------------------------------------------"
echo "StatefulSets:"
kubectl get statefulset -n dev 2>/dev/null | grep postgres || echo "  None"
echo "Deployments:"
kubectl get deployment -n dev 2>/dev/null | grep postgres || echo "  None"
echo ""

echo "=========================================="
echo "SUMMARY"
echo "=========================================="
echo ""
echo "Expected services:"
echo "  - postgres-admin.dev.svc.cluster.local"
echo "  - postgres-public.dev.svc.cluster.local"
echo "  - postgres-backend.dev.svc.cluster.local"
echo ""
echo "If services don't exist, you need to deploy PostgreSQL databases first!"
echo ""







