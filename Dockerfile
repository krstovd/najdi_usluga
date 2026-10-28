# 1. Изградба на апликацијата со Java 21 & Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Стартување на апликацијата
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/local-services-finder-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]