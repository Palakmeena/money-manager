# FROM eclipse-temurin:21-jre
# WORKDIR /app
# COPY target/moneymanager-0.0.1-SNAPSHOT.jar moneymanager-v1.0.jar
# EXPOSE 9090
# ENTRYPOINT ["java", "-jar", "moneymanager-v1.0.jar"]


FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]