FROM openjdk:11-jdk
COPY build/libs/learning_squad_be-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]