# Starts from the Gradle image
FROM gradle:jdk11 AS build

# Copy the Java source code inside the container
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Compile the code
RUN gradle -Pprod build --no-daemon

FROM openjdk:11.0-jre-slim

EXPOSE 80 8080

ENV JAVA_OPTIONS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod,swagger
ENV MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
