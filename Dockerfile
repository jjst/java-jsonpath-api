# syntax = docker/dockerfile:1.0-experimental
FROM adoptopenjdk/openjdk15:jdk-15.0.1_9-alpine-slim

RUN apk update
RUN apk add maven

WORKDIR /code

# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
RUN mkdir /home/.m2
WORKDIR /home/.m2
USER root
RUN --mount=type=cache,target=/root/.m2 mvn -f /code/pom.xml clean compile

# Adding source, compile and package into a fat jar
ADD src /code/src
RUN --mount=type=cache,target=/root/.m2 mvn -f /code/pom.xml package

EXPOSE 4567
CMD ["java", "-jar", "target/java-jsonpath-api-jar-with-dependencies.jar"]
