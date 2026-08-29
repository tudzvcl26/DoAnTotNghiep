FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace
COPY . .
RUN mvn --batch-mode clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
ARG SERVICE
RUN apk add --no-cache font-dejavu \
    && addgroup -S recruitment \
    && adduser -S -G recruitment -u 10001 recruitment
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-0.0.1-SNAPSHOT.jar /app/service.jar
USER recruitment
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/service.jar"]
