package ru.rtu_mirea.migrationdb.component.sql;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.rtu_mirea.migrationdb.entity.ColumnInfo;
import ru.rtu_mirea.migrationdb.entity.DatabaseManagementSystem;
import ru.rtu_mirea.migrationdb.entity.RelationData;

import java.util.List;
import java.util.Objects;

public class InformationBySQL {

    private final SQLScript scripts;

    public InformationBySQL(DatabaseManagementSystem nameOfDBMS) {
        this.scripts = new SQLScript(nameOfDBMS);
    }

    public List<String> getNameOfAllTables(JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) {
        String sql = scripts.getAllTables();
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            if (nameOfDBMS == DatabaseManagementSystem.POSTGRESQL) {
                return rs.getString("table_name");
            } else {
                return rs.getString("Tables_in_" + Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection().getCatalog());
            }
        });
    }

    public List<String> getInfoAboutConnectionOfTablesToMatrix(String nameOfTable, JdbcTemplate jdbcTemplate) {
        String sql = String.format(scripts.getTableConnections(), nameOfTable);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("foreign_table_name");
        });
    }

    public List<RelationData> getInfoAboutConnectionOfTablesToClass (String nameOfTable, JdbcTemplate jdbcTemplate) {
        String sql = String.format(scripts.getTableConnections(), nameOfTable);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new RelationData(
                    rs.getString("table_name"),
                    rs.getString("column_name"),
                    rs.getString("foreign_table_name"),
                    rs.getString("foreign_column_name")
            );
        });
    }

    public List<ColumnInfo> getInfoAboutColumnsOfTable(String nameOfTable, JdbcTemplate jdbcTemplate, DatabaseManagementSystem nameOfDBMS) {
        String sql = String.format(scripts.getInformationAboutColumns(), nameOfTable);
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

    public List<String> getPrimaryKeyOfTable(String nameOfTable, JdbcTemplate jdbcTemplate) {
        String sql = String.format(scripts.getPrimaryKeysOfTable(), nameOfTable);
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("column_name");
        });
    }

}
