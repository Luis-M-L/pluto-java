FROM openjdk:12-alpine

WORKDIR /app
COPY ./target/batches-*.jar ./batches.jar

CMD ["java", "-jar", "/app/batches.jar"]