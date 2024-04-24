package ru.rtu_mirea.migrationdb.entity;

public record ColumnInfo (
        String columnName,
        String tableSchema,
        int ordinalPosition,
        boolean isNullable,
        String dataType,
        boolean isIdentity,
        String identityGeneration,
        String columnDefault
) {
}
