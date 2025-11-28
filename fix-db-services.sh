#!/bin/bash

# Script to fix PostgreSQL services and expose them externally
# Usage: ./fix-db-services.sh

set -e

NAMESPACE="dev"
KUBECONFIG="${KUBECONFIG:-/tmp/kubeconfig}"

export KUBECONFIG

echo "=========================================="
echo "Fixing PostgreSQL Services"
echo "=========================================="
echo ""

# Function to patch service to NodePort
patch_service_to_nodeport() {
    local SERVICE_NAME=$1
    local NODEPORT=$2
    
    echo "Patching service $SERVICE_NAME to NodePort $NODEPORT..."
    
    # Check if service exists
    if kubectl get svc "$SERVICE_NAME" -n "$NAMESPACE" &>/dev/null; then
        kubectl patch svc "$SERVICE_NAME" -n "$NAMESPACE" -p '{"spec":{"type":"NodePort"}}' || true
        
        # Try to set nodePort (may fail if port already in use)
        kubectl patch svc "$SERVICE_NAME" -n "$NAMESPACE" --type='json' \
            -p="[{\"op\":\"replace\",\"path\":\"/spec/ports/0/nodePort\",\"value\":$NODEPORT}]" 2>/dev/null || \
        echo "⚠️  Could not set nodePort $NODEPORT (may be in use or service is ClusterIP)"
    else
        echo "⚠️  Service $SERVICE_NAME not found in namespace $NAMESPACE"
    fi
}

# Function to check and create port-forward
create_port_forward() {
    local SERVICE_NAME=$1
    local LOCAL_PORT=$2
    local REMOTE_PORT=$3
    
    echo "Creating port-forward for $SERVICE_NAME: localhost:$LOCAL_PORT -> $SERVICE_NAME:$REMOTE_PORT"
    echo "Run: kubectl port-forward -n $NAMESPACE svc/$SERVICE_NAME $LOCAL_PORT:$REMOTE_PORT"
}

echo "1️⃣  Checking existing services"
echo "-----------------------------------------------------------"
kubectl get svc -n $NAMESPACE | grep postgres || echo "No postgres services found"
echo ""

echo "2️⃣  Patching services to NodePort (if they exist)"
echo "-----------------------------------------------------------"
patch_service_to_nodeport "postgres-admin" "30433"
patch_service_to_nodeport "postgres-public" "30434"
patch_service_to_nodeport "postgres-backend" "30435"
echo ""

echo "3️⃣  Updated services:"
echo "-----------------------------------------------------------"
kubectl get svc -n $NAMESPACE | grep postgres || echo "No postgres services found"
echo ""

echo "4️⃣  Alternative: Port Forwarding (if NodePort doesn't work)"
echo "=========================================="
echo ""
create_port_forward "postgres-admin" "5433" "5432"
create_port_forward "postgres-public" "5434" "5432"
create_port_forward "postgres-backend" "5435" "5432"
echo ""

echo "✅ Service patching complete!"
echo ""
echo "📝 External Access Information:"
echo "- Admin DB: 72.62.40.154:30433 (if NodePort works)"
echo "- Public DB: 72.62.40.154:30434 (if NodePort works)"
echo "- Backend DB: 72.62.40.154:30435 (if NodePort works)"
echo ""
echo "Or use port-forwarding as shown above"

