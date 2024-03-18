package ru.rtu_mirea.migrationdb.entity;

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

    public String getColumnName() {
        return columnName;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isIdentity() {
        return isIdentity;
    }

    public String getIdentityGeneration() {
        return identityGeneration;
    }

    public String getIdentityStart() {
        return identityStart;
    }

    public String getIdentityIncrement() {
        return identityIncrement;
    }

    public String getIdentityMaximum() {
        return identityMaximum;
    }

    public String getIdentityMinimum() {
        return identityMinimum;
    }

    public String getIdentityCycle() {
        return identityCycle;
    }

    public boolean isUpdatable() {
        return isUpdatable;
    }

    public String getColumnDefault() {
        return columnDefault;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public void setOrdinalPosition(int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public void setNullable(boolean nullable) {
        isNullable = nullable;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setIdentity(boolean identity) {
        isIdentity = identity;
    }

    public void setIdentityGeneration(String identityGeneration) {
        this.identityGeneration = identityGeneration;
    }

    public void setIdentityStart(String identityStart) {
        this.identityStart = identityStart;
    }

    public void setIdentityIncrement(String identityIncrement) {
        this.identityIncrement = identityIncrement;
    }

    public void setIdentityMaximum(String identityMaximum) {
        this.identityMaximum = identityMaximum;
    }

    public void setIdentityMinimum(String identityMinimum) {
        this.identityMinimum = identityMinimum;
    }

    public void setIdentityCycle(String identityCycle) {
        this.identityCycle = identityCycle;
    }

    public void setUpdatable(boolean updatable) {
        isUpdatable = updatable;
    }

    public void setColumnDefault(String columnDefault) {
        this.columnDefault = columnDefault;
    }
}
