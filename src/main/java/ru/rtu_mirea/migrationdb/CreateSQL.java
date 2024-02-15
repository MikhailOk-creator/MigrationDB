package ru.rtu_mirea.migrationdb;

import java.util.ArrayList;
import java.util.Map;

public class CreateSQL {
    public String createSQLForTable(String tableName,
                                    ArrayList<ColumnInfo> tableColumns,
                                    Map<String, ArrayList<RelationData>> relations,
                                    Map<String, ArrayList<String>> primaryKeys) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName  + " (");
        ArrayList<RelationData> tableRelations = relations.get(tableName);
        int flag = 0;
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
            if (primaryKeysForTable != null && primaryKeysForTable.size() == 1 && primaryKeysForTable.contains(column.getColumnName())) {
                sql.append(" PRIMARY KEY");
            }
            if (flag != tableColumns.size() - 1 && tableRelations != null) {
                sql.append(",");
                flag++;
            }
        }

        ArrayList<String> primaryKeysForTable = primaryKeys.get(tableName);
        if (primaryKeysForTable != null && primaryKeysForTable.size() > 1) {
            sql.append(", PRIMARY KEY (");
            for (int i = 0; i < primaryKeysForTable.size(); i++) {
                sql.append(primaryKeysForTable.get(i));
                if (i != primaryKeysForTable.size() - 1) {
                    sql.append(", ");
                }
            }
            sql.append(")");
        }

        // Add foreign keys
        flag = 0;
        if (tableRelations != null) {
            for (RelationData relation : tableRelations) {
                sql.append(", FOREIGN KEY (").append(relation.getColumnName()).append(") REFERENCES ")
                        .append(relation.getRefTableName()).append("(").append(relation.getRefColumnName())
                        .append(")");
            }
        }

        sql.append(")");
        return sql.toString();
    }
}