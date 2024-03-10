package ru.rtu_mirea.migrationdb.entity;

import lombok.Data;

@Data
public class ResultOfMigration {
    public boolean status;
    public String message;
}
