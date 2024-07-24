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
COPY ./src/main/resources/sql/mysql ./src/main/resources/sql/mysql
COPY ./src/main/resources/sql/postgresql ./src/main/resources/sql/postgresql
EXPOSE 8080
ENTRYPOINT ["java","-jar","/migrationDB.jar"]