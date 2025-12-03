FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
# Copy root pom.xml
COPY pom.xml .
# Copy src directory (contains pom.xml and source code)
COPY src ./src
WORKDIR /app/src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/src/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

