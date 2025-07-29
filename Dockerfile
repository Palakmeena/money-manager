# Stage 1: Build the JAR with JDK
FROM eclipse-temurin:21-jdk as builder
WORKDIR /workspace
COPY . .
# Build the JAR (Maven wrapper must be in your repo)
RUN ./mvnw clean package

# Stage 2: Run the JAR with JRE only
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the JAR from the builder stage
COPY --from=builder /workspace/target/moneymanager-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
