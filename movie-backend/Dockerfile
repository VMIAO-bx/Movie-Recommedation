# ============================================================
# Stage 1: Build the Spring Boot JAR with Maven
# ============================================================
FROM maven:3.8.6-openjdk-8-slim AS build

WORKDIR /build

# Copy pom.xml first to cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B -Dhttps.protocols=TLSv1.2

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests -B -Dhttps.protocols=TLSv1.2

# ============================================================
# Stage 2: Run the JAR with minimal JRE
# ============================================================
FROM eclipse-temurin:8-jre

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /build/target/movie-backend-1.0.0.jar app.jar

# Render injects PORT env var; default 8080 for local
EXPOSE 8080

# Start the app — JVM reads server.port from $PORT
CMD ["java", "-jar", "app.jar"]
