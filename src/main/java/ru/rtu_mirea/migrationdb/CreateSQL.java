package ru.rtu_mirea.migrationdb;

import java.util.ArrayList;
import java.util.Map;

public class CreateSQL {
    public String createSQLForTable(String tableName,
                                    ArrayList<ColumnInfo> tableColumns,
                                    int[][] connections,
                                    int index_in_matrix_of_connections,
                                    ArrayList<String> tables,
                                    Map<String, ArrayList<RelationData>> relations,
                                    Map<String, ArrayList<String>> primaryKeys) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " IF NOT EXIST (\n");
        for (ColumnInfo column : tableColumns) {
            sql.append(column.getColumnName()).append(" ").append(column.getDataType());
            if (column.isNullable()) {
                sql.append(" NULL");
            } else {
                sql.append(" NOT NULL");
            }
            if (column.isIdentity()) {
                switch (column.getIdentityGeneration()) {
                    case "ALWAYS":
                        sql.append(" GENERATED ALWAYS AS IDENTITY");
                        break;
                    default:
                        sql.append(" GENERATED BY DEFAULT AS IDENTITY");
                        break;
                }
            }
            if (column.getColumnDefault() != null) {
                sql.append(" DEFAULT ").append(column.getColumnDefault());
            }
            ArrayList<String> primaryKeysForTable = primaryKeys.get(tableName);
            if (primaryKeysForTable != null && primaryKeysForTable.contains(column.getColumnName())) {
                sql.append(" PRIMARY KEY");
            }
            sql.append(",\n");
        }

        // Add foreign keys
        ArrayList<RelationData> tableRelations = relations.get(tableName);
        if (tableRelations != null) {
            for (RelationData relation : tableRelations) {
                sql.append("FOREIGN KEY (").append(relation.getColumnName()).append(") REFERENCES ")
                            .append(relation.getRefTableName()).append("(").append(relation.getRefColumnName()).append("),\n");
            }
        }

        sql.append(");\n");
        return sql.toString();
    }
}
