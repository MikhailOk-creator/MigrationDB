FROM openjdk:20
LABEL authors="okhapkin_mikhail"
ARG JAR_FILE=build/libs/*.jar
COPY ./build/libs/MigrationDB-0.0.1-SNAPSHOT.jar migrationDB.jar
ENTRYPOINT ["java","-jar","/migrationDB.jar"]