FROM docker.io/openjdk:23-slim AS builder
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# FROM docker.io/isaacaguiar/open-jdk-23-ubuntu:latest
FROM ubuntu/jre:21-24.04_stable
WORKDIR /workspace
# Install libstdc++6
COPY --from=builder spring-boot-loader /workspace
COPY --from=builder dependencies /workspace
COPY --from=builder snapshot-dependencies /workspace
COPY --from=builder application /workspace

# install logstash
RUN wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | gpg --dearmor -o /usr/share/keyrings/elastic-keyring.gpg \
 && echo "deb [signed-by=/usr/share/keyrings/elastic-keyring.gpg] https://artifacts.elastic.co/packages/9.x/apt stable main" > /etc/apt/sources.list.d/elastic-9.x.list \
 && apt-get update \
 && apt-get install -y logstash

ENV JAVA_OPTS=--enable-preview
ENTRYPOINT exec java "$JAVA_OPTS" org.springframework.boot.loader.launch.JarLauncher