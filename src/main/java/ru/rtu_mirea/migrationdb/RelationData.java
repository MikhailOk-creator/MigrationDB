package ru.rtu_mirea.migrationdb;

import lombok.Data;

@Data
public class RelationData {
    private String tableName;
    private String columnName;
    private String refTableName;
    private String refColumnName;
}
