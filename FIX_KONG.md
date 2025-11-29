# Fix Kong Configuration for Admin-Backend

## Quick Fix Commands (Run on Server)

```bash
export KUBECONFIG=/root/.kube/config
NAMESPACE="dev"

# 1. Check current status
echo "=== Current Service Status ==="
kubectl get svc -n $NAMESPACE admin-backend
kubectl get endpoints -n $NAMESPACE admin-backend
kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=admin-backend

# 2. Check Ingress
echo "=== Current Ingress ==="
kubectl get ingress -n $NAMESPACE admin-backend -o yaml

# 3. Delete and recreate ingress with correct annotations
echo "=== Fixing Ingress ==="
kubectl delete ingress -n $NAMESPACE admin-backend

# 4. Apply fixed ingress configuration
cat <<EOF | kubectl apply -f -
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: admin-backend
  namespace: dev
  annotations:
    konghq.com/strip-path: "true"
    konghq.com/protocols: "http"
    konghq.com/preserve-host: "false"
    konghq.com/connect-timeout: "60000"
    konghq.com/read-timeout: "60000"
    konghq.com/write-timeout: "60000"
spec:
  ingressClassName: kong
  rules:
    - host: dev-admin-api.72.62.40.154.nip.io
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: admin-backend
                port:
                  number: 8081
EOF

# 5. Verify
kubectl get ingress -n $NAMESPACE admin-backend
kubectl describe ingress -n $NAMESPACE admin-backend

# 6. Test connectivity
kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=admin-backend
POD_NAME=$(kubectl get pods -n $NAMESPACE -l app.kubernetes.io/name=admin-backend -o jsonpath='{.items[0].metadata.name}')
kubectl exec -n $NAMESPACE $POD_NAME -- curl -s http://localhost:8081/actuator/health

# 7. Restart Kong if needed (optional)
kubectl rollout restart deployment -n kong -l app=ingress-kong
```

## Or Use Helm to Redeploy

```bash
cd /path/to/admin-backend
helm upgrade admin-backend ./charts \
  -f env-values/dev/admin-backend.yaml \
  -n dev
```

## Check Kong Routes Directly

```bash
# Get Kong admin API
KONG_POD=$(kubectl get pods -n kong -l app=ingress-kong -o jsonpath='{.items[0].metadata.name}')
kubectl port-forward -n kong $KONG_POD 8001:8001

# In another terminal, check routes:
curl http://localhost:8001/routes | jq '.data[] | select(.hosts[] == "dev-admin-api.72.62.40.154.nip.io")'

# Check services:
curl http://localhost:8001/services | jq '.data[] | select(.name | contains("admin-backend"))'
```

