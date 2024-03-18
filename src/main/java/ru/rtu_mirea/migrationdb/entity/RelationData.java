package ru.rtu_mirea.migrationdb.entity;

public class RelationData {
    private String tableName;
    private String columnName;
    private String refTableName;
    private String refColumnName;

    public RelationData() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getRefTableName() {
        return refTableName;
    }

    public void setRefTableName(String refTableName) {
        this.refTableName = refTableName;
    }

    public String getRefColumnName() {
        return refColumnName;
    }

    public void setRefColumnName(String refColumnName) {
        this.refColumnName = refColumnName;
    }
}
