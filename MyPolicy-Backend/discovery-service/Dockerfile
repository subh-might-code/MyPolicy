# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8761
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -sf http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
