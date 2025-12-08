# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy Maven configuration files first (better layer caching)
# Only copy pom.xml files first to leverage Maven dependency cache
COPY pom.xml .
COPY src/pom.xml ./src/

# Download dependencies (this layer will be cached unless pom.xml changes)
WORKDIR /app/src
RUN mvn dependency:go-offline -B || true

# Now copy source code (this layer only invalidates when code changes)
WORKDIR /app
COPY src ./src

# Build the application
WORKDIR /app/src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# SECURITY FIX: Create non-root user for running the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy built JAR from builder
COPY --from=builder /app/src/target/*.jar app.jar

# SECURITY: Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# SECURITY: Switch to non-root user
USER appuser

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
