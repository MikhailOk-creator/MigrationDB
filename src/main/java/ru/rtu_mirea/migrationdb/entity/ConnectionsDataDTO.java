package ru.rtu_mirea.migrationdb.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public class ConnectionsDataDTO {
    @NotNull
    @Length(min = 1, max = 255, message = "Host name must be between 1 and 255 characters")
    private String host1;

    @NotNull
    @Min(1)
    private int port1;

    @NotNull
    @Length(min = 1, max = 255, message = "Database name must be between 1 and 255 characters")
    private String database1;

    @NotNull
    @Length(min = 1, max = 255, message = "Username must be between 1 and 255 characters")
    private String user1;

    @NotNull
    @Length(min = 1, max = 255, message = "Password must be between 1 and 255 characters")
    private String password1;

    @NotNull
    @Length(min = 1, max = 255, message = "DBMS name must be between 1 and 255 characters")
    private String dbms1;

    @NotNull
    @Length(min = 1, max = 255)
    private String host2;

    @NotNull
    @Min(1)
    private int port2;

    @NotNull
    @Length(min = 1, max = 255, message = "Database name must be between 1 and 255 characters")
    private String database2;

    @NotNull
    @Length(min = 1, max = 255, message = "Username must be between 1 and 255 characters")
    private String user2;

    @NotNull
    @Length(min = 1, max = 255, message = "Password must be between 1 and 255 characters")
    private String password2;

    @NotNull
    @Length(min = 1, max = 255, message = "DBMS name must be between 1 and 255 characters")
    private String dbms2;

    public ConnectionsDataDTO() {
    }

    public String getHost1() {
        return host1;
    }

    public void setHost1(String host1) {
        this.host1 = host1;
    }

    public int getPort1() {
        return port1;
    }

    public void setPort1(int port1) {
        this.port1 = port1;
    }

    public String getDatabase1() {
        return database1;
    }

    public void setDatabase1(String database1) {
        this.database1 = database1;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getDbms1() {
        return dbms1;
    }

    public void setDbms1(String dbms1) {
        this.dbms1 = dbms1;
    }

    public String getHost2() {
        return host2;
    }

    public void setHost2(String host2) {
        this.host2 = host2;
    }

    public int getPort2() {
        return port2;
    }

    public void setPort2(int port2) {
        this.port2 = port2;
    }

    public String getDatabase2() {
        return database2;
    }

    public void setDatabase2(String database2) {
        this.database2 = database2;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getDbms2() {
        return dbms2;
    }

    public void setDbms2(String dbms2) {
        this.dbms2 = dbms2;
    }
}
