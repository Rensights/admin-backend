# Quick Database Connection Check

## Run These Commands on the Server

### Step 1: Check What Services Actually Exist

```bash
export KUBECONFIG=/tmp/kubeconfig

# Check all services in dev namespace
kubectl get svc -n dev

# Check specifically for postgres services
kubectl get svc -n dev | grep postgres

# Check if pods exist
kubectl get pods -n dev | grep postgres

# Check endpoints (if services exist, are they connected to pods?)
kubectl get endpoints -n dev | grep postgres
```

### Step 2: Check From Admin-Backend Pod

```bash
# Get the admin-backend pod name
ADMIN_POD=$(kubectl get pods -n dev -l app=admin-backend -o jsonpath='{.items[0].metadata.name}')

# Check environment variables
kubectl exec -n dev $ADMIN_POD -- env | grep DATABASE

# Test DNS resolution from the pod
kubectl exec -n dev $ADMIN_POD -- nslookup postgres-admin.dev.svc.cluster.local
kubectl exec -n dev $ADMIN_POD -- nslookup postgres-public.dev.svc.cluster.local
kubectl exec -n dev $ADMIN_POD -- nslookup postgres-backend.dev.svc.cluster.local

# Test port connectivity
kubectl exec -n dev $ADMIN_POD -- nc -zv postgres-admin.dev.svc.cluster.local 5432
kubectl exec -n dev $ADMIN_POD -- nc -zv postgres-public.dev.svc.cluster.local 5432
kubectl exec -n dev $ADMIN_POD -- nc -zv postgres-backend.dev.svc.cluster.local 5432
```

### Step 3: Check All Namespaces (Maybe DBs are elsewhere?)

```bash
# Check all namespaces for postgres services
kubectl get svc --all-namespaces | grep postgres

# Check for StatefulSets (PostgreSQL is often deployed as StatefulSet)
kubectl get statefulset --all-namespaces | grep postgres
```

### Step 4: Check Secrets (Do they exist?)

```bash
# List all secrets
kubectl get secrets -n dev | grep db

# Check if our expected secrets exist
kubectl get secret admin-db-credentials -n dev
kubectl get secret public-db-credentials -n dev
kubectl get secret backend-db-credentials -n dev
```

## Expected Services

Based on configuration, these services should exist:

1. ✅ `postgres-admin` in `dev` namespace
2. ✅ `postgres-public` in `dev` namespace  
3. ❓ `postgres-backend` in `dev` namespace (might not be deployed yet)

## If Services Don't Exist

If the services don't exist, you need to:

1. **Deploy PostgreSQL databases** - Create StatefulSets/Deployments
2. **Create ClusterIP Services** pointing to the database pods
3. **Ensure service names match exactly:**
   - `postgres-admin` (not `postgres-admin-db` or `admin-postgres`)
   - `postgres-public` (not `postgres-public-db` or `public-postgres`)
   - `postgres-backend` (not `postgres-backend-db` or `backend-postgres`)

## Quick Test Script

Run the comprehensive verification:

```bash
cd /path/to/admin-backend
./verify-db-access.sh
```

Or find actual services:

```bash
./find-actual-db-services.sh
```

## What to Report Back

After running the checks, please provide:

1. ✅ Do the services exist? (`kubectl get svc -n dev | grep postgres`)
2. ✅ What are the exact service names?
3. ✅ Are the pods running? (`kubectl get pods -n dev | grep postgres`)
4. ✅ What do the DNS lookups show from the admin-backend pod?
5. ✅ What ports are the services on?

This will help identify the exact mismatch!

