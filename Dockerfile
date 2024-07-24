FROM gradle:jdk-21-and-22-alpine as builder
WORKDIR /application
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/
COPY . .
RUN ./gradlew clean bootJar

FROM openjdk:21
LABEL authors="okhapkin_mikhail"
WORKDIR /application
COPY --from=builder /application/build/libs/*.jar /migrationDB.jar
COPY --from=builder /application /application
EXPOSE 8080
ENTRYPOINT ["java","-jar","/migrationDB.jar"]