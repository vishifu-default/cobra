FROM openjdk:21-slim

WORKDIR /app

COPY cobra-producer.1.0.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]