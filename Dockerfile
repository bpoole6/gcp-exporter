FROM openjdk:21-ea-30-jdk-slim
COPY target/app.jar /app.jar
ENTRYPOINT java -jar /app.jar