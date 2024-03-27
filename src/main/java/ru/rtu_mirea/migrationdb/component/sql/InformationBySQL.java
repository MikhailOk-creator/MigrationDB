package ru.rtu_mirea.migrationdb.component.sql;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.rtu_mirea.migrationdb.entity.ColumnInfo;
import ru.rtu_mirea.migrationdb.entity.DatabaseManagementSystem;
import ru.rtu_mirea.migrationdb.entity.RelationData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class InformationBySQL {
    private final String sqlRepositoryPostgres = "src/main/resources/sql/postgresql";
    private final String sqlRepositoryMySQL = "src/main/resources/sql/mysql";

    public ArrayList<String> getNameOfAllTables(JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = pathToSQLQuery(nameOfDBMS, "all_tables.sql");
        return (ArrayList<String>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            if (nameOfDBMS == DatabaseManagementSystem.POSTGRESQL) {
                return rs.getString("table_name");
            } else {
                return rs.getString("Tables_in_" + jdbcTemplate.getDataSource().getConnection().getCatalog());
            }
        });
    }

    public ArrayList<String> getInfoAboutConnectionOfTablesToMatrix(String nameOfTable, JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = String.format(pathToSQLQuery(nameOfDBMS, "table_connections.sql"), nameOfTable);
        return  (ArrayList<String>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("foreign_table_name");
        });
    }

    public ArrayList<RelationData> getInfoAboutConnectionOfTablesToClass (String nameOfTable, JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = String.format(pathToSQLQuery(nameOfDBMS, "table_connections.sql"), nameOfTable);
        return (ArrayList<RelationData>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            RelationData relationData = new RelationData();
            relationData.setTableName(rs.getString("table_name"));
            relationData.setColumnName(rs.getString("column_name"));
            relationData.setRefTableName(rs.getString("foreign_table_name"));
            relationData.setRefColumnName(rs.getString("foreign_column_name"));
            return relationData;
        });
    }

    public ArrayList<ColumnInfo> getInfoAboutColumnsOfTable(String nameOfTable, JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = String.format(pathToSQLQuery(nameOfDBMS, "information_about_columns.sql"), nameOfTable);
        return (ArrayList<ColumnInfo>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setColumnName(rs.getString("column_name"));
            columnInfo.setTableSchema(rs.getString("table_schema"));
            columnInfo.setOrdinalPosition(rs.getInt("ordinal_position"));
            columnInfo.setNullable(rs.getString("is_nullable").equals("YES"));
            columnInfo.setDataType(rs.getString("udt_name"));
            columnInfo.setIdentity(rs.getString("is_identity").equals("YES") || rs.getString("is_identity").equals("PRI"));
            switch (nameOfDBMS) {
                case POSTGRESQL:
                    // columnInfo.setIdentityGeneration();
                    if (rs.getString("identity_generation") == null) {
                        columnInfo.setIdentityGeneration("");
                    } else if (rs.getString("identity_generation").equals("ALWAYS")) {
                        columnInfo.setIdentityGeneration("ALWAYS");
                    } else {
                        columnInfo.setIdentityGeneration("");
                    }
                    break;
                case MYSQL:
                    if (rs.getString("identity_generation").equals("auto_increment")) {
                        columnInfo.setIdentityGeneration("ALWAYS");
                    } else {
                        columnInfo.setIdentityGeneration("");
                    }
                    break;
            }
            columnInfo.setColumnDefault(rs.getString("column_default"));
            return columnInfo;
        });
    }

    public ArrayList<String> getPrimaryKeyOfTable(String nameOfTable, JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = String.format(pathToSQLQuery(nameOfDBMS, "primary_key_of_table.sql"), nameOfTable);
        return (ArrayList<String>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("column_name");
        });
    }

    private String readSqlFromFile(String filePath) throws Exception {
        return Files.readString(Path.of(filePath));
    }

    private String pathToSQLQuery (DatabaseManagementSystem nameOfDBMS, String sqlFile) throws Exception {
        return switch (nameOfDBMS) {
            case POSTGRESQL -> readSqlFromFile(sqlRepositoryPostgres + "/" + sqlFile);
            case MYSQL -> readSqlFromFile(sqlRepositoryMySQL + '/' + sqlFile);
            default -> throw new IllegalArgumentException("Unsupported DBMS");
        };
    }
}
