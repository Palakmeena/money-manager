# Stage 1: Build the JAR with JDK
FROM eclipse-temurin:21-jdk as builder

# Create workspace and set permissions
WORKDIR /workspace
RUN chmod 777 /workspace

# First copy just the Maven wrapper with explicit permissions
COPY --chmod=755 mvnw .
COPY .mvn/ .mvn/

# Then copy everything else
COPY pom.xml .
COPY src src

# Build the JAR (using direct Maven command)
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the JAR with JRE only
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/target/moneymanager-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
