package ru.rtu_mirea.migrationdb;

import lombok.Data;

@Data
public class ConnectionData {
    private String dbms;
    private String host;
    private int port;
    private String nameDB;
    private String usernameDB;
    private String passwordDB;
    private String dbDriverClassName;
}
