FROM openjdk:12-alpine

WORKDIR /app
COPY ./target/assetmanager-*.jar ./assetmanager.jar

CMD ["java", "-jar", "/app/assetmanager.jar -DEXECUTE"]