FROM openjdk:12-alpine

WORKDIR /app
COPY ./target/bitfinex-*.jar ./bitfinex.jar

CMD ["java", "-jar", "/app/bitfinex.jar"]