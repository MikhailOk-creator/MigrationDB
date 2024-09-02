# MigrationDB
Application for data migration between relational databases.
* Backend: Java 17 + Spring Boot 3
* Database: PostgreSQL 15
---
This application can migrate data (tables and their relationships) between relational databases.

At the moment, the application supports the following databases for extraction data:
* PosgreSQL
* MySQL

At the moment, the application supports the following databases for insertion data:
* PostgreSQL

---
Before starting the application for the first time, specify the data for connecting to the application database and the system administrator data (username, password, email).

A file with data for the application database and admin data (located in root): ```.env```. 
Also, you specify all the necessary data in the environment section in docker-compose files.

---
To start the migration process, you can transfer data to connect to the source and target databases through the API or in the web interface.
To open the web interface, you need to come either to the application port, or go to port 80 (if the Nginx web server was also running).

API to start the migration: ```/api/migrate```

Example of the request body to start the migration:
```json
{
    "host1": "localhost",
    "port1": "54321",
    "database1": "db_orig",
    "user1": "postgres",
    "password1": "su",
    "dbms1": "postgresql",
    "host2": "localhost",
    "port2": "54322",
    "database2": "db_copy",
    "user2": "postgres",
    "password2": "su",
    "dbms2": "postgresql"
}
```
---
Also, you can get information about the completed migration.

API to get info about completed migration: 
```/api/database/history```

API to get info about details by migration of tables: 
```/api/database/history/details```

API to get info about details by migration of tables of a specific migration. You need to specify the migration uuid:
```/api/database/history/{uuid_of_migration}```

---
The project also presents 2 main and 1 for the development of docker-compose files.
* ```docker-compose.yaml``` - to run the application without any other files. To run, you only need to create or download this file. All images are taken from Docker Hub.
* ```docker-compose-local.yaml``` - to run the application using Dockerfile's and other files that are uploaded to disk. The exception is the database, the image for it is also taken from Docker Hub.

**Attention!**
At the moment, uploading images to the Docker Hub is a problem. Therefore, the developer recommends using ```docker-compose-local.yaml``` file to run the current version of the application.