# =========================================================
# BUILD STAGE
# =========================================================

FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Root Maven project
COPY pom.xml .

# Copy required Maven modules
COPY common-events ./common-events
COPY bookmystay-core ./bookmystay-core
COPY email-service ./email-service

# Build common-events + email-service
RUN mvn -pl email-service -am clean package -DskipTests


# =========================================================
# RUNTIME STAGE
# =========================================================

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/email-service/target/*.jar app.jar

# Kafka CA certificate
COPY email-service/kafka/ca.pem /app/kafka/ca.pem

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]