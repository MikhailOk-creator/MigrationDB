# MigrationDB
Applications for data migration between relational databases.
* Backend: Java 21 + Spring Boot + Spring Security
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

A file with data for the application database and admin data (located in root): ```.env```

---
To start the migration process, you can transfer data to connect to the source and target databases through the API or in the web-interface.

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
```//api/database/history/{uuid_of_migration}```