FROM openjdk:20
LABEL authors="okhapkin_mikhail"
ARG JAR_FILE=build/libs/*.jar
COPY ./build/libs/MigrationDB-0.0.1-SNAPSHOT.jar migrationDB.jar
COPY ./src/main/resources/sql/mysql ./src/main/resources/sql/mysql
COPY ./src/main/resources/sql/postgresql ./src/main/resources/sql/postgresql
ENTRYPOINT ["java","-jar","/migrationDB.jar"]