# By creating a multistage docker build we can build our project using docker but
# not include the buildtools in the final image.
FROM maven:latest as builder

COPY pom.xml /usr/local/pom.xml
COPY src /usr/local/src

WORKDIR /usr/local/

RUN mvn clean package

FROM openjdk:8-jre

COPY --from=builder /usr/local/target/app.jar app.jar

CMD ["java", "-jar", "app.jar"]