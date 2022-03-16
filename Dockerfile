FROM openjdk:8-alpine
MAINTAINER cds "aaa"

WORKDIR /
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/local/jar/target.jar

ENTRYPOINT ["java", "-jar", "/usr/local/jar/target.jar"]