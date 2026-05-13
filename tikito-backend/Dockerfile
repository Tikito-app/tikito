FROM amazoncorretto:25-alpine3.23
RUN addgroup -S tikito && adduser -S tikito -G tikito
USER tikito:tikito
ARG JAR_FILE=target/backend-0.0.1-SNAPSHOT.jar
COPY target/backend-0.0.1-SNAPSHOT.jar /opt/tikito-backend.jar
ENTRYPOINT [\
    "java",\
    "-jar",\
    "/opt/tikito-backend.jar"]
EXPOSE ${SERVER_PORT}