FROM gradle:8.8-jdk17 AS builder

WORKDIR /home/gradle/project
COPY . .

RUN gradle build -x test

FROM eclipse-temurin:17-jre-alpine

EXPOSE 8080

COPY --from=builder /home/gradle/project/build/libs/*.jar /app/application.jar

ENTRYPOINT ["java", "-jar", "/app/application.jar"]