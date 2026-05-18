# Multi-stage Dockerfile for Flash-Cart backend
# Builder stage: build the jar with Maven (uses mvnw if present)
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Copy Maven wrapper and project files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Make mvnw executable and build
RUN chmod +x mvnw && ./mvnw -DskipTests package -B

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built jar from builder
COPY --from=builder /workspace/target/*.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
