package ru.rtu_mirea.migrationdb.component.sql;

import ru.rtu_mirea.migrationdb.enums.DatabaseManagementSystem;

public class SQLScript {
    private String allTables;
    private String informationAboutColumns;
    private String primaryKeysOfTable;
    private String tableConnections;

    public SQLScript(DatabaseManagementSystem databaseManagementSystem) {
        switch (databaseManagementSystem) {
            case POSTGRESQL:
                setAllTables("""
                        SELECT table_name FROM information_schema.tables
                        WHERE table_schema='public' AND table_type='BASE TABLE';
                        """);

                setInformationAboutColumns("""
                        SELECT
                            column_name,
                            table_schema,
                            ordinal_position,
                            is_nullable,
                            udt_name,
                            is_identity,
                            identity_generation,
                            identity_start,
                            identity_increment,
                            identity_maximum,
                            identity_minimum,
                            identity_cycle,
                            is_updatable,
                            column_default
                        FROM information_schema.columns
                        WHERE table_name = '%s';
                        """);

                setPrimaryKeysOfTable("""
                        SELECT c.column_name, c.data_type
                        FROM information_schema.table_constraints tc
                                 JOIN information_schema.constraint_column_usage AS ccu USING (constraint_schema, constraint_name)
                                 JOIN information_schema.columns AS c ON c.table_schema = tc.constraint_schema
                            AND tc.table_name = c.table_name AND ccu.column_name = c.column_name
                        WHERE constraint_type = 'PRIMARY KEY' and tc.table_name = '%s';
                        """);

                setTableConnections("""
                        SELECT
                            tc.table_schema,
                            tc.constraint_name,
                            tc.table_name,
                            kcu.column_name,
                            ccu.table_schema AS foreign_table_schema,
                            ccu.table_name AS foreign_table_name,
                            ccu.column_name AS foreign_column_name
                        FROM information_schema.table_constraints AS tc
                                 JOIN information_schema.key_column_usage AS kcu
                                      ON tc.constraint_name = kcu.constraint_name
                                          AND tc.table_schema = kcu.table_schema
                                 JOIN information_schema.constraint_column_usage AS ccu
                                      ON ccu.constraint_name = tc.constraint_name
                        WHERE tc.constraint_type = 'FOREIGN KEY'
                          AND tc.table_schema='public'
                          AND tc.table_name='%s';
                        """);
                break;

            case MYSQL:
                setAllTables("""
                        SHOW tables;
                        """);

                setInformationAboutColumns("""
                        SELECT
                            COLUMN_NAME AS column_name,
                            TABLE_SCHEMA AS table_schema,
                            ORDINAL_POSITION AS ordinal_position,
                            IS_NULLABLE AS is_nullable,
                            COLUMN_TYPE AS udt_name,
                            COLUMN_KEY AS is_identity,
                            EXTRA AS identity_generation,
                            COLUMN_DEFAULT AS column_default
                        FROM
                            INFORMATION_SCHEMA.COLUMNS
                        WHERE
                            TABLE_NAME = '%s';
                        """);

                setPrimaryKeysOfTable("""
                        SELECT DISTINCT
                            c.COLUMN_NAME as column_name, c.DATA_TYPE
                        FROM
                            INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
                                JOIN information_schema.KEY_COLUMN_USAGE AS ku USING (TABLE_SCHEMA, CONSTRAINT_NAME)
                                JOIN information_schema.COLUMNS AS c ON c.TABLE_SCHEMA = tc.TABLE_SCHEMA
                                AND tc.TABLE_NAME = c.TABLE_NAME
                                AND ku.COLUMN_NAME = c.COLUMN_NAME
                        WHERE
                            CONSTRAINT_TYPE = 'PRIMARY KEY' AND tc.TABLE_NAME = '%s';
                        """);

                setTableConnections("""
                        SELECT
                            tc.TABLE_SCHEMA,
                            tc.CONSTRAINT_NAME,
                            tc.TABLE_NAME,
                            kcu.COLUMN_NAME,
                            kcu.REFERENCED_TABLE_SCHEMA AS foreign_table_schema,
                            kcu.REFERENCED_TABLE_NAME AS foreign_table_name,
                            kcu.REFERENCED_COLUMN_NAME AS foreign_column_name
                        FROM
                            INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS tc
                                JOIN information_schema.KEY_COLUMN_USAGE AS kcu
                                     ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
                                         AND tc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA
                                JOIN information_schema.KEY_COLUMN_USAGE AS kcu2
                                     ON tc.CONSTRAINT_NAME = kcu2.CONSTRAINT_NAME
                        WHERE
                            tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND
                            tc.TABLE_NAME = '%s';
                        """);
                break;

            default:
                throw new IllegalArgumentException("Unsupported DBMS");
        }
    }

    public String getAllTables() {
        return allTables;
    }

    public void setAllTables(String allTables) {
        this.allTables = allTables;
    }

    public String getInformationAboutColumns() {
        return informationAboutColumns;
    }

    public void setInformationAboutColumns(String informationAboutColumns) {
        this.informationAboutColumns = informationAboutColumns;
    }

    public String getPrimaryKeysOfTable() {
        return primaryKeysOfTable;
    }

    public void setPrimaryKeysOfTable(String primaryKeysOfTable) {
        this.primaryKeysOfTable = primaryKeysOfTable;
    }

    public String getTableConnections() {
        return tableConnections;
    }

    public void setTableConnections(String tableConnections) {
        this.tableConnections = tableConnections;
    }
}
