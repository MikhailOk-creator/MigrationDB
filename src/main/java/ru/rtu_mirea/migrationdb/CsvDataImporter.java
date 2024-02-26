package ru.rtu_mirea.migrationdb;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvDataImporter {
    private final JdbcTemplate jdbcTemplate;
    private final String csvDirectory = "src/main/resources/csv/";

    public CsvDataImporter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void importCsvDataToTable(String tableName) throws IOException {
        // List all CSV files in the directory
        Files.list(Path.of(csvDirectory))
                .filter(path -> path.toString().endsWith("data_" + tableName + ".csv"))
                .forEach(csvFile -> {
                    try {
                        importCsvFile(csvFile, tableName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        System.out.println("Data imported to table: " + tableName);
    }

    private void importCsvFile(Path csvFile, String tableName) throws IOException {
        String headerSql = String.format("SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '%s'", tableName);
        List<String> columnNames = jdbcTemplate.query(headerSql, (resultSet, i) -> resultSet.getString("column_name"));
        // Read data from CSV file
        Files.lines(csvFile).skip(1) // Skip header line
                .forEach(line -> {
                    String[] values = line.split(",");
                    insertDataIntoTable(values, tableName, columnNames);
                });
    }

    private void insertDataIntoTable(String[] values, String tableName, List<String> columnNames) {
        // Assuming the columns are in the same order as in the CSV file
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableName)
                .append(" (");

        for (int i = 0; i < columnNames.size(); i++) {
            sql.append(columnNames.get(i));
            if (i < columnNames.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(") VALUES (");

        for (int i = 0; i < values.length; i++) {
            sql.append(values[i]);
            if (i < values.length - 1) {
                sql.append(", ");
            }
        }

        sql.append(")");

        // Execute the SQL insert statement
        jdbcTemplate.execute(sql.toString());
    }
}
