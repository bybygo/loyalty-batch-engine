FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:9874/actuator/health || exit 1

RUN addgroup -S -g 1001 appuser && \
    adduser -S -u 1001 -G appuser appuser
USER appuser

ENV JAVA_OPTS="-Xms512m -Xmx512m"

EXPOSE 9874
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]