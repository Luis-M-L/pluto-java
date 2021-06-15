FROM openjdk:12-alpine

WORKDIR /app
COPY ./target/persistencia-*.jar ./persistencia.jar

CMD ["java", "-jar", "/app/persistencia.jar"]