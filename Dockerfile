FROM registry.access.redhat.com/ubi8/openjdk-21:latest

WORKDIR /app
COPY build/libs/loyalty-batch-engine-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9874
ENTRYPOINT ["java", "-jar", "app.jar"]