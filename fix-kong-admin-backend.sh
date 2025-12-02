#!/bin/bash
# Fix Kong configuration for admin-backend

set -e

export KUBECONFIG=/root/.kube/config || export KUBECONFIG=/tmp/kubeconfig
NAMESPACE="dev"
SERVICE_NAME="admin-backend"
INGRESS_NAME="admin-backend"

echo "=== Diagnosing Kong Configuration for Admin-Backend ==="
echo ""

# 1. Check service exists and has endpoints
echo "1. Checking service and endpoints..."
SERVICE_IP=$(kubectl get svc -n $NAMESPACE $SERVICE_NAME -o jsonpath='{.spec.clusterIP}' 2>/dev/null || echo "")
if [ -z "$SERVICE_IP" ]; then
    echo "   ❌ Service $SERVICE_NAME not found!"
    exit 1
fi

ENDPOINTS=$(kubectl get endpoints -n $NAMESPACE $SERVICE_NAME -o jsonpath='{.subsets[0].addresses[*].ip}' 2>/dev/null || echo "")
if [ -z "$ENDPOINTS" ]; then
    echo "   ❌ Service has no endpoints! Pod might not be running."
    kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=$SERVICE_NAME
    exit 1
fi

echo "   ✅ Service IP: $SERVICE_IP"
echo "   ✅ Endpoints: $ENDPOINTS"
echo ""

# 2. Check pod is running
echo "2. Checking pod status..."
POD_NAME=$(kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=$SERVICE_NAME -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
if [ -z "$POD_NAME" ]; then
    echo "   ❌ No pods found for $SERVICE_NAME"
    exit 1
fi

POD_STATUS=$(kubectl get pod -n $NAMESPACE $POD_NAME -o jsonpath='{.status.phase}' 2>/dev/null || echo "")
if [ "$POD_STATUS" != "Running" ]; then
    echo "   ❌ Pod is not running. Status: $POD_STATUS"
    kubectl get pod -n $NAMESPACE $POD_NAME
    exit 1
fi

echo "   ✅ Pod: $POD_NAME (Status: $POD_STATUS)"
echo ""

# 3. Test pod connectivity
echo "3. Testing pod connectivity..."
if kubectl exec -n $NAMESPACE $POD_NAME -- curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "   ✅ Pod health endpoint is responding"
else
    echo "   ⚠️  Pod health endpoint not responding (might still be starting)"
fi
echo ""

# 4. Check Ingress configuration
echo "4. Checking Ingress configuration..."
INGRESS_EXISTS=$(kubectl get ingress -n $NAMESPACE $INGRESS_NAME 2>/dev/null || echo "NOT_FOUND")
if [ "$INGRESS_EXISTS" = "NOT_FOUND" ]; then
    echo "   ❌ Ingress $INGRESS_NAME not found!"
    echo "   Checking for any admin-backend ingress..."
    kubectl get ingress -n $NAMESPACE | grep admin
    exit 1
fi

echo "   ✅ Ingress found:"
kubectl get ingress -n $NAMESPACE $INGRESS_NAME -o yaml | grep -A 20 "annotations:" | head -25
echo ""

# 5. Check Kong service and route
echo "5. Checking Kong service mapping..."
KONG_SERVICE=$(kubectl get ingress -n $NAMESPACE $INGRESS_NAME -o jsonpath='{.metadata.annotations.konghq\.com/upstream-host}' 2>/dev/null || echo "")
echo "   Kong upstream host annotation: ${KONG_SERVICE:-'not set'}"
echo ""

# 6. Fix Kong annotations if needed
echo "6. Applying Kong configuration fixes..."

# Get the full service name
FULL_SERVICE_NAME=$(kubectl get svc -n $NAMESPACE $SERVICE_NAME -o jsonpath='{.metadata.name}')
SERVICE_PORT=$(kubectl get svc -n $NAMESPACE $SERVICE_NAME -o jsonpath='{.spec.ports[0].port}')

echo "   Service: $FULL_SERVICE_NAME:$SERVICE_PORT"

# Update ingress with proper Kong annotations
kubectl annotate ingress -n $NAMESPACE $INGRESS_NAME \
    konghq.com/strip-path="true" \
    konghq.com/protocols="http" \
    konghq.com/plugins="cors" \
    --overwrite 2>/dev/null || true

echo "   ✅ Updated Kong annotations"
echo ""

# 7. Verify Kong can reach the service
echo "7. Verifying Kong service connectivity..."

# Get Kong pod
KONG_POD=$(kubectl get pods -n kong -l app=ingress-kong -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
if [ -n "$KONG_POD" ]; then
    echo "   Testing from Kong pod to service..."
    if kubectl exec -n kong $KONG_POD -- nc -zv $FULL_SERVICE_NAME.$NAMESPACE.svc.cluster.local $SERVICE_PORT 2>&1 | grep -q "open"; then
        echo "   ✅ Kong can reach the service"
    else
        echo "   ⚠️  Kong cannot reach the service directly (this might be normal if using ClusterIP)"
    fi
else
    echo "   ⚠️  Could not find Kong pod to test connectivity"
fi
echo ""

# 8. Restart Kong ingress controller if needed
echo "8. Checking if Kong needs restart..."
KONG_DEPLOYMENT=$(kubectl get deployment -n kong -l app=ingress-kong -o name 2>/dev/null | head -1 || echo "")
if [ -n "$KONG_DEPLOYMENT" ]; then
    echo "   Kong deployment found: $KONG_DEPLOYMENT"
    echo "   To restart Kong (if needed), run:"
    echo "   kubectl rollout restart $KONG_DEPLOYMENT"
else
    echo "   ⚠️  Kong deployment not found"
fi
echo ""

echo "=== Summary ==="
echo "Service: $FULL_SERVICE_NAME"
echo "Namespace: $NAMESPACE"
echo "Port: $SERVICE_PORT"
echo "Pod: $POD_NAME ($POD_STATUS)"
echo ""
echo "Kong Ingress should route:"
echo "  Host: dev-admin-api.72.62.40.154.nip.io"
echo "  -> Service: $FULL_SERVICE_NAME.$NAMESPACE.svc.cluster.local:$SERVICE_PORT"
echo ""
echo "If issues persist, try:"
echo "  1. kubectl delete ingress -n $NAMESPACE $INGRESS_NAME"
echo "  2. Redeploy using: helm upgrade admin-backend ./charts -f env-values/dev/admin-backend.yaml -n $NAMESPACE"



