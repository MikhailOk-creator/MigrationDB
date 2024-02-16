package ru.rtu_mirea.migrationdb;

import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class InformationBySQL {
    private final String sqlRepository = "src/main/resources/sql";

    public ArrayList<String> getNameOfAllTables(JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/all_tables.sql");
        return (ArrayList<String>) jdbcTemplate.query(source_sql, (rs, rowNum) -> {
            return rs.getString("table_name");
        });
    }

    public ArrayList<String> getInfoAboutConnectionOfTablesToMatrix(String nameOfTable, JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/table_connections.sql");
        String sql = String.format(source_sql, nameOfTable);
        return  (ArrayList<String>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("foreign_table_name");
        });
    }

    public ArrayList<RelationData> getInfoAboutConnectionOfTablesToClass (String nameOfTable, JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/table_connections.sql");
        String sql = String.format(source_sql, nameOfTable);
        return (ArrayList<RelationData>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            RelationData relationData = new RelationData();
            relationData.setTableName(rs.getString("table_name"));
            relationData.setColumnName(rs.getString("column_name"));
            relationData.setRefTableName(rs.getString("foreign_table_name"));
            relationData.setRefColumnName(rs.getString("foreign_column_name"));
            return relationData;
        });
    }

    public ArrayList<ColumnInfo> getInfoAboutColumnsOfTable(String nameOfTable, JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/information_about_columns.sql");
        String sql = String.format(source_sql, nameOfTable);
        return (ArrayList<ColumnInfo>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setColumnName(rs.getString("column_name"));
            columnInfo.setTableSchema(rs.getString("table_schema"));
            columnInfo.setOrdinalPosition(rs.getInt("ordinal_position"));
            columnInfo.setNullable(rs.getString("is_nullable").equals("YES"));
            columnInfo.setDataType(rs.getString("udt_name"));
            columnInfo.setIdentity(rs.getString("is_identity").equals("YES"));
            columnInfo.setIdentityGeneration(rs.getString("identity_generation"));
            columnInfo.setIdentityStart(rs.getString("identity_start"));
            columnInfo.setIdentityIncrement(rs.getString("identity_increment"));
            columnInfo.setIdentityMaximum(rs.getString("identity_maximum"));
            columnInfo.setIdentityMinimum(rs.getString("identity_minimum"));
            columnInfo.setIdentityCycle(String.valueOf(rs.getString("identity_cycle").equals("YES")));
            columnInfo.setUpdatable(rs.getString("is_updatable").equals("YES"));
            columnInfo.setColumnDefault(rs.getString("column_default"));
            return columnInfo;
        });
    }

    public ArrayList<String> getPrimaryKeyOfTable(String nameOfTable, JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/primary_key_of_table.sql");
        String sql = String.format(source_sql, nameOfTable);
        return (ArrayList<String>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("column_name");
        });
    }

    private String readSqlFromFile(String filePath) throws Exception {
        return Files.readString(Path.of(filePath));
    }
}
