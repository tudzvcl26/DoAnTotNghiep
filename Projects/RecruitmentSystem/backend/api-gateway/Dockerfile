FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn --batch-mode dependency:go-offline
COPY src ./src
RUN mvn --batch-mode clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S gateway && adduser -S -G gateway -u 10001 gateway
WORKDIR /app
COPY --from=build /workspace/target/api-gateway-1.0.0.jar /app/api-gateway.jar
USER gateway
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]
