# Database Connection Troubleshooting Guide

## Quick Check Commands

Run these on the server to check database status:

```bash
export KUBECONFIG=/tmp/kubeconfig

# Check if PostgreSQL services exist
kubectl get svc -n dev | grep postgres

# Check if PostgreSQL pods are running
kubectl get pods -n dev | grep postgres

# Check service endpoints
kubectl get endpoints -n dev | grep postgres
```

## Current Configuration

The admin-backend expects these services in the `dev` namespace:

1. **postgres-admin** (ClusterIP service)
   - Internal: `postgres-admin.dev.svc.cluster.local:5432`
   - Database: `admin_db_dev`

2. **postgres-public** (ClusterIP service)
   - Internal: `postgres-public.dev.svc.cluster.local:5432`
   - Database: `public_db_dev`

3. **postgres-backend** (ClusterIP service)
   - Internal: `postgres-backend.dev.svc.cluster.local:5432`
   - Database: `backend_db_dev`

## Getting All Credentials

Run the check script:
```bash
cd /Users/ihakhverdiyev/Desktop/rensights/services/admin-backend
./check-db-connection.sh
```

Or manually extract:

```bash
# Admin DB
kubectl get secret admin-db-credentials -n dev -o jsonpath='{.data}' | jq -r 'to_entries[] | "\(.key): \(.value | @base64d)"'

# Public DB
kubectl get secret public-db-credentials -n dev -o jsonpath='{.data}' | jq -r 'to_entries[] | "\(.key): \(.value | @base64d)"'

# Backend DB
kubectl get secret backend-db-credentials -n dev -o jsonpath='{.data}' | jq -r 'to_entries[] | "\(.key): \(.value | @base64d)"'
```

## Making Databases Accessible from Outside

### Option 1: Change Service Type to NodePort (Recommended)

Run the fix script:
```bash
./fix-db-services.sh
```

Or manually:

```bash
# Patch each service to NodePort
kubectl patch svc postgres-admin -n dev -p '{"spec":{"type":"NodePort","ports":[{"port":5432,"nodePort":30433}]}}'
kubectl patch svc postgres-public -n dev -p '{"spec":{"type":"NodePort","ports":[{"port":5432,"nodePort":30434}]}}'
kubectl patch svc postgres-backend -n dev -p '{"spec":{"type":"NodePort","ports":[{"port":5432,"nodePort":30435}]}}'
```

**External Connection:**
- Admin DB: `72.62.40.154:30433`
- Public DB: `72.62.40.154:30434`
- Backend DB: `72.62.40.154:30435`

### Option 2: Port Forwarding (Temporary)

```bash
# Terminal 1 - Admin DB
kubectl port-forward -n dev svc/postgres-admin 5433:5432

# Terminal 2 - Public DB
kubectl port-forward -n dev svc/postgres-public 5434:5432

# Terminal 3 - Backend DB
kubectl port-forward -n dev svc/postgres-backend 5435:5432
```

**Connection:**
- Admin DB: `localhost:5433`
- Public DB: `localhost:5434`
- Backend DB: `localhost:5435`

### Option 3: LoadBalancer (If supported)

```bash
kubectl patch svc postgres-admin -n dev -p '{"spec":{"type":"LoadBalancer"}}'
kubectl patch svc postgres-public -n dev -p '{"spec":{"type":"LoadBalancer"}}'
kubectl patch svc postgres-backend -n dev -p '{"spec":{"type":"LoadBalancer"}}'
```

## Common Issues

### Issue 1: Services Don't Exist

If the services don't exist, you need to deploy PostgreSQL databases first:

```bash
# Check if you have Helm charts for PostgreSQL
# If not, you may need to create StatefulSets/Deployments
```

### Issue 2: Pods Not Running

```bash
# Check pod status
kubectl get pods -n dev | grep postgres

# Check pod logs
kubectl logs -n dev <pod-name> | grep postgres

# Describe pod for errors
kubectl describe pod -n dev <pod-name> | grep postgres
```

### Issue 3: DNS Resolution Failing

The error `UnknownHostException: postgres-admin.dev.svc.cluster.local` means:
- Service doesn't exist, OR
- DNS is not working in the cluster

Fix:
```bash
# Verify service exists
kubectl get svc postgres-admin -n dev

# Check CoreDNS
kubectl get pods -n kube-system | grep coredns

# Test DNS from a pod
kubectl run -it --rm debug --image=busybox --restart=Never -- nslookup postgres-admin.dev.svc.cluster.local
```

### Issue 4: Kong Not Routing Correctly

Kong is for HTTP routing, not database connections. Databases should be:
- Accessible via ClusterIP from within the cluster
- OR exposed via NodePort/LoadBalancer for external access

## Verifying Connection from Pod

Test connection from inside the cluster:

```bash
# Run a test pod
kubectl run postgres-test --image=postgres:15-alpine --rm -it --restart=Never -- sh

# Inside the pod, test connection:
psql -h postgres-admin.dev.svc.cluster.local -p 5432 -U <username> -d admin_db_dev
```

## Complete Credentials Summary

After running `./check-db-connection.sh`, you'll get:

```
ADMIN DATABASE:
- Host: postgres-admin.dev.svc.cluster.local (internal)
- Host: 72.62.40.154:30433 (external NodePort)
- Database: admin_db_dev
- Username: <from secret>
- Password: <from secret>

PUBLIC DATABASE:
- Host: postgres-public.dev.svc.cluster.local (internal)
- Host: 72.62.40.154:30434 (external NodePort)
- Database: public_db_dev
- Username: <from secret>
- Password: <from secret>

BACKEND DATABASE:
- Host: postgres-backend.dev.svc.cluster.local (internal)
- Host: 72.62.40.154:30435 (external NodePort)
- Database: backend_db_dev
- Username: <from secret>
- Password: <from secret>
```

## Next Steps

1. ✅ Run `./check-db-connection.sh` to get credentials
2. ✅ Run `./fix-db-services.sh` to expose databases externally
3. ✅ Verify services exist: `kubectl get svc -n dev | grep postgres`
4. ✅ Verify pods are running: `kubectl get pods -n dev | grep postgres`
5. ✅ Update application with external URLs if needed

