# FROM eclipse-temurin:21-jre
# WORKDIR /app
# COPY target/moneymanager-0.0.1-SNAPSHOT.jar moneymanager-v1.0.jar
# EXPOSE 9090
# ENTRYPOINT ["java", "-jar", "moneymanager-v1.0.jar"]


FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy the specific JAR file name we saw in your logs
COPY target/moneymanager-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]