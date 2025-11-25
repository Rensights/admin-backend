# Admin Backend Service

Admin API service for Rensights platform management.

## Structure

```
admin-backend/
├── src/                    # Source code
│   ├── main/
│   │   ├── java/          # Java source files
│   │   └── resources/     # Configuration files
├── Dockerfile              # Container build definition
├── pom.xml                 # Maven project configuration
├── helm/                   # Helm chart for Kubernetes
│   ├── Chart.yaml
│   ├── values.yaml
│   └── templates/
└── deployments/            # Environment-specific values
    └── dev/
        └── values.yaml
```

## Building

```bash
mvn clean package -DskipTests
```

## Docker Build

```bash
docker build -t rensights-admin-backend:latest .
```

## Deployment

### Dev Environment
```bash
helm upgrade --install admin-backend-dev helm/ \
  -f deployments/dev/values.yaml \
  -n dev
```

