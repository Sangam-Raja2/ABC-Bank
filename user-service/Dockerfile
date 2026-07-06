# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Leverage layer caching: copy poms first
COPY pom.xml /build/pom.xml
COPY user-service/pom.xml /build/user-service/pom.xml
COPY banking-service/pom.xml /build/banking-service/pom.xml

# Copy sources
COPY user-service/src /build/user-service/src

WORKDIR /build
RUN mvn -pl user-service -am -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /build/user-service/target/user-service.jar /app/user-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/user-service.jar"]
