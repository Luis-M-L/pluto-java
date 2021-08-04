FROM openjdk:12-alpine

WORKDIR /app
COPY ./target/persistencia-*.jar ./instruments.jar

CMD ["java", "-jar", "/app/instruments.jar"]