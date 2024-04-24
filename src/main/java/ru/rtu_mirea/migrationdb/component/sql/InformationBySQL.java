package ru.rtu_mirea.migrationdb.component.sql;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.rtu_mirea.migrationdb.entity.ColumnInfo;
import ru.rtu_mirea.migrationdb.entity.DatabaseManagementSystem;
import ru.rtu_mirea.migrationdb.entity.RelationData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class InformationBySQL {
    private final String sqlRepositoryPostgres = "src/main/resources/sql/postgresql";
    private final String sqlRepositoryMySQL = "src/main/resources/sql/mysql";

    public List<String> getNameOfAllTables(JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = pathToSQLQuery(nameOfDBMS, "all_tables.sql");
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            if (nameOfDBMS == DatabaseManagementSystem.POSTGRESQL) {
                return rs.getString("table_name");
            } else {
                return rs.getString("Tables_in_" + jdbcTemplate.getDataSource().getConnection().getCatalog());
            }
        });
    }

    public List<String> getInfoAboutConnectionOfTablesToMatrix(String nameOfTable, JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = String.format(pathToSQLQuery(nameOfDBMS, "table_connections.sql"), nameOfTable);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("foreign_table_name");
        });
    }

    public List<RelationData> getInfoAboutConnectionOfTablesToClass (String nameOfTable, JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = String.format(pathToSQLQuery(nameOfDBMS, "table_connections.sql"), nameOfTable);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new RelationData(
                    rs.getString("table_name"),
                    rs.getString("column_name"),
                    rs.getString("foreign_table_name"),
                    rs.getString("foreign_column_name")
            );
        });
    }

    public List<ColumnInfo> getInfoAboutColumnsOfTable(String nameOfTable, JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = String.format(pathToSQLQuery(nameOfDBMS, "information_about_columns.sql"), nameOfTable);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String IdentityGeneration = "";
            switch (nameOfDBMS) {
                case POSTGRESQL:
                    // columnInfo.setIdentityGeneration();
                    if (rs.getString("identity_generation") == null) {
                        IdentityGeneration = "";
                    } else if (rs.getString("identity_generation").equals("ALWAYS")) {
                        IdentityGeneration = "ALWAYS";
                    } else {
                        IdentityGeneration = "";
                    }
                    break;
                case MYSQL:
                    if (rs.getString("identity_generation").equals("auto_increment")) {
                        IdentityGeneration = "ALWAYS";
                    } else {
                        IdentityGeneration = "";
                    }
                    break;
            }

            return new ColumnInfo(
                    rs.getString("column_name"),
                    rs.getString("table_schema"),
                    rs.getInt("ordinal_position"),
                    rs.getString("is_nullable").equals("YES"),
                    rs.getString("udt_name"),
                    rs.getString("is_identity").equals("YES") || rs.getString("is_identity").equals("PRI"),
                    IdentityGeneration,
                    rs.getString("column_default")
            );
        });
    }

    public List<String> getPrimaryKeyOfTable(String nameOfTable, JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) throws Exception {
        String sql = String.format(pathToSQLQuery(nameOfDBMS, "primary_key_of_table.sql"), nameOfTable);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
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
