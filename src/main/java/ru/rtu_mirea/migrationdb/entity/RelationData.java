package ru.rtu_mirea.migrationdb.entity;

public record RelationData (
        String tableName,
        String columnName,
        String refTableName,
        String refColumnName
) {
}
