FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

EXPOSE 3000

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
