package ru.rtu_mirea.migrationdb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import ru.rtu_mirea.migrationdb.component.*;
import ru.rtu_mirea.migrationdb.component.csv.ExportFromCSV;
import ru.rtu_mirea.migrationdb.component.csv.ImportToCSV;
import ru.rtu_mirea.migrationdb.component.sql.CreateSQL;
import ru.rtu_mirea.migrationdb.component.sql.DatabaseConfig;
import ru.rtu_mirea.migrationdb.component.sql.InformationBySQL;
import ru.rtu_mirea.migrationdb.entity.*;
import ru.rtu_mirea.migrationdb.enums.StatusOfMigration;
import ru.rtu_mirea.migrationdb.repository.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Main method of the service. This service can connect and migrate data between relation databases.
 * @author Mikahil Okhapkin
 * @version 1.0
 */
@Service
public class MigrationService {
    private final MigrationRepository migrationRepository;
    private final MigrationDetailRepository migrationDetailRepository;

    public MigrationService(MigrationRepository migrationRepository, MigrationDetailRepository migrationDetailRepository) {
        this.migrationRepository = migrationRepository;
        this.migrationDetailRepository = migrationDetailRepository;
    }

    Logger log = LoggerFactory.getLogger(MigrationService.class);

    /**
     * Method for migration data between databases.
     * This class checks the connection to databases.
     * Then it collects information about the source database.
     * Next, it sorts the order of the tables for migration.
     * Next, perform the table migration process according to this order.
     * @param connectionData1 Data for connection to the first database. Include host, port, name of database, username, password.
     * @param connectionData2 Data for connection to the first database. Include the same data as the first connection.
     * @return Result of migration. Include status (true or false) and message.
     * @throws Exception If something goes wrong
     * @see ConnectionData
     * @see ResultOfMigration
     */
    public ResultOfMigration migration(ConnectionData connectionData1, ConnectionData connectionData2) throws Exception {
        JdbcTemplate jdbcTemplate1 = new JdbcTemplate();
        JdbcTemplate jdbcTemplate2 = new JdbcTemplate();
        MigrationData migrationData = new MigrationData();
        MigrationDetailData migrationDetailData = new MigrationDetailData();
        User authorizedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!checkConnectionToDatabase(jdbcTemplate1, connectionData1)) {
            return new ResultOfMigration(false,"Connection to " + connectionData1.getNameDB() + " database failed");
        }

        if (!checkConnectionToDatabase(jdbcTemplate2, connectionData2)) {
            return new ResultOfMigration(false,"Connection to " + connectionData2.getNameDB() + " database failed");
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
        migrationData.setStartTime(new Timestamp(System.currentTimeMillis()));
        migrationData.setUserThatStartedMigration(authorizedUser.getUsername());
        migrationRepository.save(migrationData);

        configureForDBPostgres(jdbcTemplate1, connectionData1);
        InformationBySQL informationBySQL = new InformationBySQL(connectionData1.getDbms());
        List<String> tables1 = informationBySQL.getNameOfAllTables(jdbcTemplate1, connectionData1.getDbms());
        log.info("Tables in {} database: {}", connectionData1.getNameDB(), tables1);

        // Matrix of connections between tables
        int[][] connections = new int[tables1.size()][tables1.size()];
        Map<String, List<RelationData>> relations = new HashMap<>();
        for (String table : tables1) {
            try {
                List<String>connections_between_one = informationBySQL.getInfoAboutConnectionOfTablesToMatrix(table, jdbcTemplate1);
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
        List<String> tables2_Sorted = new ArrayList<>();
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
        List<ColumnInfo> columns;
        Map<String, List<String>> primaryKeys = new HashMap<>();
        List<String> generatedColumns = new ArrayList<>();
        ImportToCSV importOrigToCSV = new ImportToCSV(jdbcTemplate1);
        ExportFromCSV exportFromCSV = new ExportFromCSV(jdbcTemplate2);
        CreateSQL createSQL = new CreateSQL();
        String SQLScriptForCreatingTable;
        for (String table : tables2_Sorted) {

            migrationDetailData.setId(UUID.randomUUID());
            migrationDetailData.setMigrationId(migrationId);
            migrationDetailData.setSourceTable(table);
            migrationDetailData.setTargetTable(table);
            migrationDetailData.setStatus(StatusOfMigration.MIGRATING.toString());
            Timestamp startTimeForTable = new Timestamp(System.currentTimeMillis());
            migrationDetailData.setStartTime(startTimeForTable);

            try {
                log.info("Table: {}", table);

                columns = informationBySQL.getInfoAboutColumnsOfTable(table, jdbcTemplate1, connectionData1.getDbms());
                primaryKeys.put(table, informationBySQL.getPrimaryKeyOfTable(table, jdbcTemplate1));

                // get generated columns
                for (ColumnInfo column : columns) {
                    if (!column.identityGeneration().isEmpty()) {
                        generatedColumns.add(column.columnName());
                    }
                }

                // Sort columns by ordinal position
                columns.sort(Comparator.comparing(ColumnInfo::ordinalPosition));

                log.info("Info about columns of all in table {}", table);

                // Export data from all tables to CSV
                try {
                    importOrigToCSV.importTableToCsv(table, connectionData1.getDbms());
                } catch (IOException e) {
                    log.error("Error: {}", e.getMessage());

                    AbortMigration(migrationDetailData, migrationData, "Error to import data from " + table + " table." + '\n' +
                            "Error: " + e.getMessage(), startTimeForTable);

                    return new ResultOfMigration(false, "Error to import data from " + table + " table." + '\n' +
                            "Error: " + e.getMessage());
                }

                // Create SQL for all tables
                try {
                    SQLScriptForCreatingTable = createSQL.createSQLForTable(table, columns, relations, primaryKeys);
                    log.info("SQL for creating table: {}", SQLScriptForCreatingTable);
                } catch (Exception e) {
                    log.error("Error: {}", e.getMessage());

                    AbortMigration(migrationDetailData, migrationData, "Error to create SQL for " + table + " table." + '\n' +
                            "Error: " + e.getMessage(), startTimeForTable);

                    return new ResultOfMigration(false, "Error to create SQL for " + table + " table." + '\n' +
                            "Error: " + e.getMessage());
                }

                // Create table in new database
                configureForDBPostgres(jdbcTemplate2, connectionData2);
                log.info("Creating table in {} database...", connectionData2.getNameDB());
                try {
                    jdbcTemplate2.execute(SQLScriptForCreatingTable);
                    log.info("Table created in {} database", connectionData2.getNameDB());
                } catch (Exception e) {
                    log.info("Error: {}", e.getMessage());

                    AbortMigration(migrationDetailData, migrationData, "Error to create table in " + connectionData2.getNameDB() + " database." + '\n' +
                            "Error: " + e.getMessage(), startTimeForTable);

                    return new ResultOfMigration(false, "Error to create table in " + connectionData2.getNameDB() + " database." + '\n' +
                            "Error: " + e.getMessage());
                }

                log.info("Exporting data to table: {}", table);
                configureForDBPostgres(jdbcTemplate2, connectionData2);
                try {
                    exportFromCSV.exportCsvDataToTable(table, generatedColumns, connectionData1.getDbms());
                    log.info("Data exported to table: {}", table);
                } catch (IOException e) {
                    log.error("Error: {}", e.getMessage());

                    AbortMigration(migrationDetailData, migrationData, "Error to export data to " + table + " table." + '\n' +
                            "Error: " + e.getMessage(), startTimeForTable);

                    return new ResultOfMigration(false, "Error to export data to " + table + " table." + '\n' +
                            "Error: " + e.getMessage());
                }

                // Delete all CSV files from the project that have not been deleted for some reason
                exportFromCSV.deleteAllCsvFiles();

                migrationDetailData.setStatus(StatusOfMigration.DONE.toString());
                migrationDetailData.setEndTime(new Timestamp(System.currentTimeMillis()));
                migrationDetailData.setDuration((double) (new Date().getTime() - startTimeForTable.getTime()));
                migrationDetailRepository.save(migrationDetailData);
            } catch (Exception e) {
                log.error("Error: {}", e.getMessage());

                AbortMigration(migrationDetailData, migrationData, "Error to migrate " + table + " table." + '\n' +
                        "Error: " + e.getMessage(), startTimeForTable);

                return new ResultOfMigration(false, "Error to migrate " + table + " table." + '\n' +
                        "Error: " + e.getMessage());
            }
        }
        log.info("Migration successful!");

        migrationData.setStatus(StatusOfMigration.DONE.toString());
        migrationData.setEndTime(new Timestamp(System.currentTimeMillis()));
        migrationData.setDuration((double) (new Date().getTime() - migrationData.getStartTime().getTime()));
        migrationRepository.save(migrationData);

        return new ResultOfMigration(true, "Migration completed successfully");
    }

    /**
     * Method to check connection to database.
     * @param jdbcTemplate - object for connection to database
     * @param connectionData - data for connection to database
     * @return boolean status of test connection
     * @see ConnectionData
     */
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

    /**
     * A class for write data about abort migration and its details in the application's database.
     * @param migrationDetailData Data about the migration of a specific table during which an execution error occurred.
     * @param migrationData Data about the migration in which an execution error occurred.
     * @param message The message with error.
     * @param startTimeForTable Time of start migrate of specific table. Use for calculation duration of migrations.
     */
    private void AbortMigration(MigrationDetailData migrationDetailData, MigrationData migrationData, String message, Date startTimeForTable) {
        migrationDetailData.setStatus(StatusOfMigration.ABORTED.toString());
        migrationDetailData.setEndTime(new Timestamp(System.currentTimeMillis()));
        migrationDetailData.setDuration((double) (new Timestamp(System.currentTimeMillis()).getTime() - startTimeForTable.getTime()));
        migrationDetailData.setErrorMessage(message);
        migrationDetailRepository.save(migrationDetailData);

        migrationData.setStatus(StatusOfMigration.ABORTED.toString());
        migrationData.setEndTime(new Timestamp(System.currentTimeMillis()));
        migrationData.setDuration((double) (new Timestamp(System.currentTimeMillis()).getTime() - startTimeForTable.getTime()));
        migrationRepository.save(migrationData);
    }

    private void configureForDBPostgres(JdbcTemplate jdbcTemplate, ConnectionData connectionData) {
        String db1Url;
        if (connectionData.getDbms().toString().equals("oracle")) {
            db1Url = String.format(
                    "jdbc:oracle:thin:@%s:%d:%s",
                    connectionData.getHost(),
                    connectionData.getPort(),
                    connectionData.getNameDB()
            );
        } else {
            db1Url = String.format(
                    "jdbc:%s://%s:%d/%s",
                    connectionData.getDbms().toString().toLowerCase(),
                    connectionData.getHost(),
                    connectionData.getPort(),
                    connectionData.getNameDB()
            );
        }

        DataSource dataSource = DatabaseConfig.postgreSQLDataSource(db1Url, connectionData.getUsernameDB(), connectionData.getPasswordDB());

        jdbcTemplate.setDataSource(dataSource);
    }
}
