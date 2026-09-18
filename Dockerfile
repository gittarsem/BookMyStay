# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

COPY common-events ./common-events
COPY bookmystay-core ./bookmystay-core
COPY email-service ./email-service

RUN mvn -pl bookmystay-core -am clean package -DskipTests


# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/bookmystay-core/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]