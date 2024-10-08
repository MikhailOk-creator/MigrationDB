package ru.rtu_mirea.migrationdb.entity;

import ru.rtu_mirea.migrationdb.enums.DatabaseManagementSystem;

public class ConnectionData {
    private DatabaseManagementSystem dbms;
    private String host;
    private int port;
    private String nameDB;
    private String usernameDB;
    private String passwordDB;
    private String dbDriverClassName;

    public ConnectionData() {
    }

    public DatabaseManagementSystem getDbms() {
        return dbms;
    }

    public void setDbms(DatabaseManagementSystem dbms) {
        this.dbms = dbms;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getNameDB() {
        return nameDB;
    }

    public void setNameDB(String nameDB) {
        this.nameDB = nameDB;
    }

    public String getUsernameDB() {
        return usernameDB;
    }

    public void setUsernameDB(String usernameDB) {
        this.usernameDB = usernameDB;
    }

    public String getPasswordDB() {
        return passwordDB;
    }

    public void setPasswordDB(String passwordDB) {
        this.passwordDB = passwordDB;
    }

    public String getDbDriverClassName() {
        return dbDriverClassName;
    }

    public void setDbDriverClassName(String dbDriverClassName) {
        this.dbDriverClassName = dbDriverClassName;
    }
}
