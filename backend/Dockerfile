# Multi-stage Dockerfile for Flash-Cart backend
# Builder stage: build the jar with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Copy project files
COPY pom.xml .
COPY src src

# Build with Maven
RUN mvn -DskipTests package -B

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built jar from builder
COPY --from=builder /workspace/target/*.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
