package ru.rtu_mirea.migrationdb;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvDataImporter {
    private final JdbcTemplate jdbcTemplate;
    private final String csvDirectory = "src/main/resources/csv/";

    public CsvDataImporter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void importCsvDataToTable(String tableName, ArrayList<String> generatedColumns) throws IOException {
        String nameOfCSVFile = "data_" + tableName + ".csv";
        String pathToCSVFile = csvDirectory + nameOfCSVFile;

        // List all CSV files in the directory
        Files.list(Path.of(csvDirectory))
                .filter(path -> path.toString().endsWith(nameOfCSVFile))
                .forEach(csvFile -> {
                    try {
                        importCsvFile(csvFile, tableName, generatedColumns);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        // Delete the CSV file
        try {
            Files.deleteIfExists(Path.of(pathToCSVFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importCsvFile(Path csvFile, String tableName, ArrayList<String> generatedColumns) throws IOException {
        String headerSql = String.format("SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '%s'", tableName);
        List<String> columnNames = jdbcTemplate.query(headerSql, (resultSet, i) -> resultSet.getString("column_name"));
        // Read data from CSV file
        Files.lines(csvFile).skip(1) // Skip header line
                .forEach(line -> {
                    String[] values = line.split(",");
                    insertDataIntoTable(values, tableName, columnNames, generatedColumns);
                });
    }

    private void insertDataIntoTable(String[] values, String tableName, List<String> columnNames, ArrayList<String> generatedColumns) {
        // Assuming the columns are in the same order as in the CSV file
        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableName)
                .append(" (");

        ArrayList<Integer> indexesOfGeneratedColumns = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++) {
            if (!generatedColumns.contains(columnNames.get(i))) {
                sql.append(columnNames.get(i));
                if (i < columnNames.size() - 1) {
                    sql.append(", ");
                }
            } else {
                indexesOfGeneratedColumns.add(i);
            }
        }

        sql.append(") VALUES (");

        for (int i = 0; i < values.length; i++) {
            if (!indexesOfGeneratedColumns.contains(i)) {
                if (values[i].equals("null") || values[i].equals("NULL")) {
                    sql.append(values[i]);
                } else {
                    sql.append("'").append(values[i]).append("'");
                }
                if (i < values.length - 1) {
                    sql.append(", ");
                }
            }
        }

        sql.append(")");

        // Execute the SQL insert statement
        jdbcTemplate.execute(sql.toString());
    }

    public void deleteAllCsvFiles() {
        try {
            Files.list(Path.of(csvDirectory))
                    .filter(path -> path.toString().endsWith(".csv"))
                    .forEach(csvFile -> {
                        try {
                            Files.deleteIfExists(csvFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
