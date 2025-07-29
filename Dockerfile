# Stage 1: Build the JAR with JDK
FROM eclipse-temurin:21-jdk as builder
WORKDIR /workspace

# First copy just the Maven wrapper files
COPY mvnw .
COPY .mvn/ .mvn/
# Make the wrapper executable
RUN chmod +x mvnw

# Then copy everything else
COPY . .
# Build the JAR
RUN ./mvnw clean package

# Stage 2: Run the JAR with JRE only
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/target/moneymanager-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
