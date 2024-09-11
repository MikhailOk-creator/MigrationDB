package ru.rtu_mirea.migrationdb.component.csv;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.rtu_mirea.migrationdb.enums.DatabaseManagementSystem;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImportToCSV {
    private final JdbcTemplate jdbcTemplate;
    private final String csvDirectory = "src/main/resources/csv/";

    public ImportToCSV(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void importTableToCsv(String tableName, DatabaseManagementSystem dbms) throws IOException {
        String sql = String.format("SELECT * FROM %s", tableName);
        String outputFile = getOutputFilePath(tableName);

        // Create the directory if it doesn't exist
        createDirectoryIfNotExists(csvDirectory);

        // Create the file and write data
        try (FileWriter csvWriter = new FileWriter(outputFile)) {
            // Write header
            writeHeader(tableName, csvWriter, dbms);

            // Write data
            List<String> rows = jdbcTemplate.query(sql, (resultSet, i) -> {
                StringBuilder row = new StringBuilder();
                for (int j = 1; j <= resultSet.getMetaData().getColumnCount(); j++) {
                    row.append(resultSet.getString(j)).append(",");
                }
                return row.toString();
            });

            for (String row : rows) {
                csvWriter.append(row).append("\n");
            }
        }
    }

    private void createDirectoryIfNotExists(String directory) throws IOException {
        Path path = Path.of(directory);
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }

    private void writeHeader(String tableName, FileWriter csvWriter, DatabaseManagementSystem nameOfDBMS) throws IOException {
        String headerSql = switch (nameOfDBMS) {
            case POSTGRESQL ->
                    String.format("SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '%s'", tableName);
            case MYSQL ->
                    String.format("SELECT COLUMN_NAME AS column_name FROM information_schema.columns WHERE table_name = '%s' ORDER BY ordinal_position", tableName);
            default -> throw new IllegalArgumentException("Unsupported DBMS");
        };
        assert headerSql != null;
        List<String> columnNames = jdbcTemplate.query(headerSql, (resultSet, i) -> resultSet.getString("column_name"));

        // Join column names with commas
        String header = String.join(",", columnNames);

        // Write header to CSV
        csvWriter.append(header).append("\n");
    }

    private String getOutputFilePath(String tableName) {
        return csvDirectory + "data_" + tableName + ".csv";
    }
}
