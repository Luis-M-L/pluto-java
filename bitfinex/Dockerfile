FROM openjdk:12-alpine

WORKDIR /app
COPY ./target/bitfinex-*.jar ./bitfinex.jar

ENV API_KEYSTORE /ssh/

CMD ["java", "-jar", "/app/bitfinex.jar"]