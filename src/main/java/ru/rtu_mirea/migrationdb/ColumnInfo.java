package ru.rtu_mirea.migrationdb;

import lombok.Data;

@Data
public class ColumnInfo {
    private String columnName;
    private String tableSchema;
    private int ordinalPosition;
    private boolean isNullable;
    private String dataType;
    private boolean isIdentity;
    private String identityGeneration;
    private String identityStart;
    private String identityIncrement;
    private String identityMaximum;
    private String identityMinimum;
    private String identityCycle;
    private boolean isUpdatable;
    private String columnDefault;
}
