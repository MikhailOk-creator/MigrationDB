package ru.rtu_mirea.migrationdb.entity;

import lombok.Data;

@Data
public class ConnectionsDataDTO {
    private String host1;
    private int port1;
    private String database1;
    private String user1;
    private String password1;
    private String dbms1;
    private String host2;
    private int port2;
    private String database2;
    private String user2;
    private String password2;
    private String dbms2;
}
