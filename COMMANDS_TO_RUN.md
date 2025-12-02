# Commands to Run on Server (72.62.40.154)

## SSH into the server:
```bash
ssh root@72.62.40.154
# Password: AKrBcJVirdIMlM6S8/hO
```

## Once connected, run these commands:

### Quick Check (Copy all at once):

```bash
export KUBECONFIG=/tmp/kubeconfig 2>/dev/null || export KUBECONFIG=/root/.kube/config 2>/dev/null || true

echo "=== Services in dev namespace ==="
kubectl get svc -n dev

echo ""
echo "=== Postgres services ==="
kubectl get svc -n dev | grep postgres || echo "NO POSTGRES SERVICES FOUND"

echo ""
echo "=== Postgres pods ==="
kubectl get pods -n dev | grep postgres || echo "NO POSTGRES PODS FOUND"

echo ""
echo "=== All namespaces (looking for postgres) ==="
kubectl get svc --all-namespaces | grep postgres || echo "NO POSTGRES SERVICES IN ANY NAMESPACE"

echo ""
echo "=== Admin-backend pod ==="
ADMIN_POD=$(kubectl get pods -n dev -l app=admin-backend -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
if [ -n "$ADMIN_POD" ]; then
    echo "Pod: $ADMIN_POD"
    echo "Testing DNS from pod:"
    kubectl exec -n dev $ADMIN_POD -- nslookup postgres-admin.dev.svc.cluster.local 2>&1 | head -5
    kubectl exec -n dev $ADMIN_POD -- nslookup postgres-public.dev.svc.cluster.local 2>&1 | head -5
    kubectl exec -n dev $ADMIN_POD -- nslookup postgres-backend.dev.svc.cluster.local 2>&1 | head -5
else
    echo "Admin-backend pod not found"
fi
```

### Or run the diagnostic script:

```bash
# Copy the contents of run-on-server.sh and paste it, then run
chmod +x run-on-server.sh
./run-on-server.sh
```

## What We're Looking For:

1. **Do the services exist?**
   - Expected: `postgres-admin`, `postgres-public`, `postgres-backend`
   - If they don't exist, we need to deploy them

2. **Do the pods exist and are they running?**
   - Check pod status: `kubectl get pods -n dev | grep postgres`

3. **Can admin-backend pod resolve the DNS names?**
   - Test from inside the pod with nslookup

## After Running, Please Share:
- Output of `kubectl get svc -n dev`
- Output of `kubectl get pods -n dev | grep postgres`
- Output of DNS tests from admin-backend pod

This will tell us exactly what's missing!







