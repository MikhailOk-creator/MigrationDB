package ru.rtu_mirea.migrationdb.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
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
}
