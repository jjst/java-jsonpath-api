# syntax = docker/dockerfile:1.0-experimental
FROM maven:3.8.1-openjdk-17-slim

WORKDIR /
# Prepare by downloading dependencies
ADD pom.xml /pom.xml
RUN mkdir -p /maven/.m2
RUN --mount=type=cache,target=/maven/.m2 mvn -Dmaven.repo.local=/maven/.m2 -f /pom.xml clean compile

# Adding source, compile and package into a fat jar
ADD src /src
RUN --mount=type=cache,target=/maven/.m2 mvn -Dmaven.repo.local=/maven/.m2 -f /pom.xml package

RUN curl -L "https://github.com/lightstep/otel-launcher-java/releases/download/1.2.0/lightstep-opentelemetry-javaagent.jar" > agent.jar

EXPOSE 4567
CMD ["java", "-javaagent:agent.jar", "-jar", "target/java-jsonpath-api-jar-with-dependencies.jar"]
