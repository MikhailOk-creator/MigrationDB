package ru.rtu_mirea.migrationdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.rtu_mirea.migrationdb.component.*;
import ru.rtu_mirea.migrationdb.component.csv.CsvDataImporter;
import ru.rtu_mirea.migrationdb.component.csv.ExportToCSV;
import ru.rtu_mirea.migrationdb.component.sql.CreateSQL;
import ru.rtu_mirea.migrationdb.component.sql.DatabaseConfig;
import ru.rtu_mirea.migrationdb.component.sql.InformationBySQL;
import ru.rtu_mirea.migrationdb.entity.*;
import ru.rtu_mirea.migrationdb.repository.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

@Service
public class MigrationService {
    private final MigrationRepository migrationRepository;
    private final MigrationDetailRepository migrationDetailRepository;

    public MigrationService(MigrationRepository migrationRepository, MigrationDetailRepository migrationDetailRepository) {
        this.migrationRepository = migrationRepository;
        this.migrationDetailRepository = migrationDetailRepository;
    }

    Logger log = LoggerFactory.getLogger(MigrationService.class);

    public ResultOfMigration migration(ConnectionData connectionData1, ConnectionData connectionData2) throws Exception {
        JdbcTemplate jdbcTemplate1 = new JdbcTemplate();
        JdbcTemplate jdbcTemplate2 = new JdbcTemplate();
        MigrationData migrationData = new MigrationData();
        MigrationDetailData migrationDetailData = new MigrationDetailData();
        ResultOfMigration resultOfMigration = new ResultOfMigration();

        if (!checkConnectionToDatabase(jdbcTemplate1, connectionData1)) {
            resultOfMigration.setStatus(false);
            resultOfMigration.setMessage("Connection to " + connectionData1.getNameDB() + " database failed");
            return resultOfMigration;
        }

        if (!checkConnectionToDatabase(jdbcTemplate2, connectionData2)) {
            resultOfMigration.setStatus(false);
            resultOfMigration.setMessage("Connection to " + connectionData2.getNameDB() + " database failed");
            return resultOfMigration;
        }

        UUID migrationId = UUID.randomUUID();
        migrationData.setId(migrationId);
        migrationData.setSourceHost(connectionData1.getHost());
        migrationData.setSourcePort(connectionData1.getPort());
        migrationData.setSourceDB(connectionData1.getNameDB());
        migrationData.setTargetHost(connectionData2.getHost());
        migrationData.setTargetPort(connectionData2.getPort());
        migrationData.setTargetDB(connectionData2.getNameDB());
        migrationData.setStatus(StatusOfMigration.MIGRATING.toString());
        migrationData.setStartTime(new Date());
        migrationRepository.save(migrationData);

        configureForDBPostgres(jdbcTemplate1, connectionData1);
        InformationBySQL informationBySQL = new InformationBySQL();
        ArrayList<String> tables1 = informationBySQL.getNameOfAllTables(jdbcTemplate1);
        log.info("Tables in {} database: {}", connectionData1.getNameDB(), tables1);

        // Matrix of connections between tables
        int[][] connections = new int[tables1.size()][tables1.size()];
        // Map of connections between tables (key - table_name, value - list of relations (RelationData))
        Map<String, ArrayList<RelationData>> relations = new HashMap<>();
        for (String table : tables1) {
            try {
                ArrayList<String>connections_between_one = informationBySQL.getInfoAboutConnectionOfTablesToMatrix(table, jdbcTemplate1);
                relations.put(table, informationBySQL.getInfoAboutConnectionOfTablesToClass(table, jdbcTemplate1));
                for (String connection : connections_between_one) {
                    int index = tables1.indexOf(connection);
                    connections[tables1.indexOf(table)][index] = 1;
                }
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());
            }
        }
        log.info("Matrix of connections between tables: {}", Arrays.deepToString(connections));

        // Topological sort of the graph
        TopologicalSort g = new TopologicalSort(tables1.size());
        for (int i = 0; i < tables1.size(); i++) {
            for (int j = 0; j < tables1.size(); j++) {
                if (connections[i][j] == 1) {
                    g.addEdge(i, j);
                }
            }
        }
        String result = g.topologicalSort();
        int[] resultArray = Arrays.stream(result.split(" ")).mapToInt(Integer::parseInt).toArray();
        ArrayList<String> tables2_Sorted = new ArrayList<>();
        // reverse array
        for (int i = 0; i < resultArray.length / 2; i++) {
            int temp = resultArray[i];
            resultArray[i] = resultArray[resultArray.length - i - 1];
            resultArray[resultArray.length - i - 1] = temp;
        }
        for (int j : resultArray) {
            tables2_Sorted.add(tables1.get(j));
        }
        log.info("Tables in {} database sorted by topological sort: {}", connectionData1.getNameDB(), tables2_Sorted);

        // Get info about columns of tables and create tables
        ArrayList<ColumnInfo> columns;
        Map<String, ArrayList<String>> primaryKeys = new HashMap<>();
        ArrayList<String> generatedColumns = new ArrayList<>();
        ExportToCSV exportOrigToCSV = new ExportToCSV(jdbcTemplate1);
        CsvDataImporter csvDataImporter = new CsvDataImporter(jdbcTemplate2);
        CreateSQL createSQL = new CreateSQL();
        String SQLScriptForCreatingTable;
        for (String table : tables2_Sorted) {

            migrationDetailData.setId(UUID.randomUUID());
            migrationDetailData.setMigrationId(migrationId);
            migrationDetailData.setSourceTable(table);
            migrationDetailData.setTargetTable(table);
            migrationDetailData.setStatus(StatusOfMigration.MIGRATING.toString());
            Date startTimeForTable = new Date();
            migrationDetailData.setStartTime(startTimeForTable);

            try {
                log.info("Table: {}", table);

                columns = informationBySQL.getInfoAboutColumnsOfTable(table, jdbcTemplate1);
                primaryKeys.put(table, informationBySQL.getPrimaryKeyOfTable(table, jdbcTemplate1));

                // get generated columns
                for (ColumnInfo column : columns) {
                    if (column.isIdentity()) {
                        generatedColumns.add(column.getColumnName());
                    }
                }

                log.info("Info about columns of all in table {}", table);

                // Export data from all tables to CSV
                try {
                    exportOrigToCSV.exportTableToCsv(table);
                } catch (IOException e) {
                    log.error("Error: {}", e.getMessage());
                    resultOfMigration.setStatus(false);
                    resultOfMigration.setMessage("Error to export data from " + table + " table." + '\n' +
                            "Error: " + e.getMessage());

                    AbortMigration(migrationDetailData, migrationData, "Error to export data from " + table + " table." + '\n' +
                            "Error: " + e.getMessage(), startTimeForTable);

                    return resultOfMigration;
                }

                // Create SQL for all tables
                try {
                    SQLScriptForCreatingTable = createSQL.createSQLForTable(table, columns, relations, primaryKeys);
                    log.info("SQL for creating table: {}", SQLScriptForCreatingTable);
                } catch (Exception e) {
                    log.error("Error: {}", e.getMessage());
                    resultOfMigration.setStatus(false);
                    resultOfMigration.setMessage("Error to create SQL for " + table + " table." + '\n' +
                            "Error: " + e.getMessage());

                    AbortMigration(migrationDetailData, migrationData, "Error to create SQL for " + table + " table." + '\n' +
                            "Error: " + e.getMessage(), startTimeForTable);

                    return resultOfMigration;
                }

                // Create table in new database
                configureForDBPostgres(jdbcTemplate2, connectionData2);
                log.info("Creating table in {} database...", connectionData2.getNameDB());
                try {
                    jdbcTemplate2.execute(SQLScriptForCreatingTable);
                    log.info("Table created in {} database", connectionData2.getNameDB());
                } catch (Exception e) {
                    log.info("Error: {}", e.getMessage());
                    resultOfMigration.setStatus(false);
                    resultOfMigration.setMessage("Error to create table in " + connectionData2.getNameDB() + " database." + '\n' +
                            "Error: " + e.getMessage());

                    AbortMigration(migrationDetailData, migrationData, "Error to create table in " + connectionData2.getNameDB() + " database." + '\n' +
                            "Error: " + e.getMessage(), startTimeForTable);

                    return resultOfMigration;
                }

                log.info("Importing data to table: {}", table);
                configureForDBPostgres(jdbcTemplate2, connectionData2);
                try {
                    csvDataImporter.importCsvDataToTable(table, generatedColumns);
                    log.info("Data imported to table: {}", table);
                } catch (IOException e) {
                    log.error("Error: {}", e.getMessage());
                    resultOfMigration.setStatus(false);
                    resultOfMigration.setMessage("Error to import data to " + table + " table." + '\n' +
                            "Error: " + e.getMessage());

                    AbortMigration(migrationDetailData, migrationData, "Error to import data to " + table + " table." + '\n' +
                            "Error: " + e.getMessage(), startTimeForTable);

                    return resultOfMigration;
                }

                // Delete all CSV files from the project that have not been deleted for some reason
                csvDataImporter.deleteAllCsvFiles();

                migrationDetailData.setStatus(StatusOfMigration.DONE.toString());
                migrationDetailData.setEndTime(new Date());
                migrationDetailData.setDuration((double) (new Date().getTime() - startTimeForTable.getTime()));
                migrationDetailRepository.save(migrationDetailData);
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());
                resultOfMigration.setStatus(false);
                resultOfMigration.setMessage("Error to migrate " + table + " table." + '\n' +
                        "Error: " + e.getMessage());

                AbortMigration(migrationDetailData, migrationData, "Error to migrate " + table + " table." + '\n' +
                        "Error: " + e.getMessage(), startTimeForTable);

                return resultOfMigration;
            }
        }
        log.info("Migration successful!");
        resultOfMigration.setStatus(true);
        resultOfMigration.setMessage("Migration completed successfully");

        migrationData.setStatus(StatusOfMigration.DONE.toString());
        migrationData.setEndTime(new Date());
        migrationData.setDuration((double) (new Date().getTime() - migrationData.getStartTime().getTime()));
        migrationRepository.save(migrationData);

        return resultOfMigration;
    }

    private boolean checkConnectionToDatabase(JdbcTemplate jdbcTemplate, ConnectionData connectionData) {
        try{
            configureForDBPostgres(jdbcTemplate, connectionData);
            return testDatabaseConnection(jdbcTemplate, connectionData.getNameDB());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            return false;
        }
    }

    private boolean testDatabaseConnection(JdbcTemplate jdbcTemplate, String dbName) {
        try {
            // Attempt to connect to the database
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("{} Database connection successful!", dbName);
            return true;
        } catch (Exception e) {
            log.error("{} Database connection unsuccessful. Error: {}", dbName, e.getMessage());
            return false;
        }
    }

    private void AbortMigration(MigrationDetailData migrationDetailData, MigrationData migrationData, String message, Date startTimeForTable) {
        migrationDetailData.setStatus(StatusOfMigration.ABORTED.toString());
        migrationDetailData.setEndTime(new Date());
        migrationDetailData.setDuration((double) (new Date().getTime() - startTimeForTable.getTime()));
        migrationDetailData.setErrorMessage(message);
        migrationDetailRepository.save(migrationDetailData);

        migrationData.setStatus(StatusOfMigration.ABORTED.toString());
        migrationData.setEndTime(new Date());
        migrationData.setDuration((double) (new Date().getTime() - migrationData.getStartTime().getTime()));
        migrationRepository.save(migrationData);
    }

    private void configureForDBPostgres(JdbcTemplate jdbcTemplate, ConnectionData connectionData) {
        String db1Url;
        if (connectionData.getDbms().equals("oracle")) {
            db1Url = String.format("jdbc:oracle:thin:@%s:%d:%s", connectionData.getHost(), connectionData.getPort(), connectionData.getNameDB());
        } else {
            db1Url = String.format("jdbc:%s://%s:%d/%s", connectionData.getDbms(), connectionData.getHost(), connectionData.getPort(), connectionData.getNameDB());
        }

        DataSource dataSource = DatabaseConfig.postgreSQLDataSource(db1Url, connectionData.getUsernameDB(), connectionData.getPasswordDB());

        jdbcTemplate.setDataSource(dataSource);
    }
}
