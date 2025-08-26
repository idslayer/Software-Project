FROM docker.io/openjdk:17-slim AS builder
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM docker.io/docker.io/openjdk:17-slim
WORKDIR /workspace
# Install libstdc++6
COPY --from=builder spring-boot-loader /workspace
COPY --from=builder dependencies /workspace
COPY --from=builder snapshot-dependencies /workspace
COPY --from=builder application /workspace
ENV JAVA_OPTS=--enable-preview
ENTRYPOINT exec java "$JAVA_OPTS" org.springframework.boot.loader.launch.JarLauncher