FROM maven:3.5.2-jdk-8 as maven-build
RUN apt-get update && apt-get install -y bash
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN mvn install

FROM openjdk:8-alpine
WORKDIR /app
COPY --from=maven-build /usr/src/app/target/pong-server-1.0-SNAPSHOT-jar-with-dependencies.jar .
ENTRYPOINT java -jar /app/pong-server-1.0-SNAPSHOT-jar-with-dependencies.jar