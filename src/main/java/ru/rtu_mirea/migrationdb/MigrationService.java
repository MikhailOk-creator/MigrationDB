package ru.rtu_mirea.migrationdb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.rtu_mirea.migrationdb.component.*;
import ru.rtu_mirea.migrationdb.component.csv.CsvDataImporter;
import ru.rtu_mirea.migrationdb.component.csv.ExportToCSV;
import ru.rtu_mirea.migrationdb.component.sql.CreateSQL;
import ru.rtu_mirea.migrationdb.component.sql.DatabaseConfig;
import ru.rtu_mirea.migrationdb.component.sql.InformationBySQL;
import ru.rtu_mirea.migrationdb.entity.ColumnInfo;
import ru.rtu_mirea.migrationdb.entity.ConnectionData;
import ru.rtu_mirea.migrationdb.entity.RelationData;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MigrationService {
    @Value("${spring.datasource.url}")
    private String db1Url;

    @Value("${spring.datasource.username}")
    private String db1Username;

    @Value("${spring.datasource.password}")
    private String db1Password;

    @Value("${spring.datasource.driver-class-name}")
    private String db1DriverClassName;


    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    public boolean migration(ConnectionData connectionData1, ConnectionData connectionData2) throws Exception {
        JdbcTemplate jdbcTemplate1 = new JdbcTemplate();
        JdbcTemplate jdbcTemplate2 = new JdbcTemplate();

        assert connectionData1 != null;
        configureForDBPostgres(jdbcTemplate1, connectionData1);
        testDatabaseConnection(jdbcTemplate1, connectionData1.getNameDB());


        assert connectionData2 != null;
        configureForDBPostgres(jdbcTemplate2, connectionData2);
        testDatabaseConnection(jdbcTemplate2, connectionData2.getNameDB());

        configureForDBPostgres(jdbcTemplate2, connectionData2);
        testDatabaseConnection(jdbcTemplate2, connectionData2.getNameDB());

        configureForDBPostgres(jdbcTemplate1, connectionData1);
        InformationBySQL informationBySQL = new InformationBySQL();
        ArrayList<String> tables1 = informationBySQL.getNameOfAllTables(jdbcTemplate1);
        System.out.println('\n' + "Tables in " + connectionData1.getNameDB() + " database:");
        for (String table : tables1) {
            System.out.println(table);
        }

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
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println('\n' + "Matrix of connections between tables:");
        for (int i = 0; i < tables1.size(); i++) {
            for (int j = 0; j < tables1.size(); j++) {
                System.out.print(connections[i][j] + " ");
            }
            System.out.println();
        }

        // Topological sort of the graph
        TopologicalSort g = new TopologicalSort(tables1.size());
        for (int i = 0; i < tables1.size(); i++) {
            for (int j = 0; j < tables1.size(); j++) {
                if (connections[i][j] == 1) {
                    g.addEdge(i, j);
                }
            }
        }
        System.out.println('\n' + "Topological sort of the graph:");
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
            System.out.print(tables1.get(j) + " ");
            tables2_Sorted.add(tables1.get(j));
        }

        // Get info about columns of tables and create tables
        ArrayList<ColumnInfo> columns;
        Map<String, ArrayList<String>> primaryKeys = new HashMap<>();
        ArrayList<String> generatedColumns = new ArrayList<>();
        ExportToCSV exportOrigToCSV = new ExportToCSV(jdbcTemplate1);
        CsvDataImporter csvDataImporter = new CsvDataImporter(jdbcTemplate2);
        CreateSQL createSQL = new CreateSQL();
        String SQLScriptForCreatingTable = "";
        for (String table : tables2_Sorted) {
            try {
                System.out.println('\n' + "Table: " + table);

                columns = informationBySQL.getInfoAboutColumnsOfTable(table, jdbcTemplate1);
                primaryKeys.put(table, informationBySQL.getPrimaryKeyOfTable(table, jdbcTemplate1));

                // get generated columns
                for (ColumnInfo column : columns) {
                    if (column.isIdentity()) {
                        generatedColumns.add(column.getColumnName());
                    }
                }

                // Print info about columns of all tables
                System.out.println('\n' + "Info about columns of all in table " + table);
                for (ColumnInfo column : columns) {
                    System.out.println(column);
                }
                System.out.println();

                // Export data from all tables to CSV
                try {
                    exportOrigToCSV.exportTableToCsv(table);
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    return false;
                }

                // Create SQL for all tables
                System.out.println('\n');
                try {
                    SQLScriptForCreatingTable = createSQL.createSQLForTable(table, columns, relations, primaryKeys);
                    System.out.println(SQLScriptForCreatingTable);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    return false;
                }

                // Create table in new database
                configureForDBPostgres(jdbcTemplate2, connectionData2);
                System.out.println('\n' + "Creating tables in " + connectionData2.getNameDB() + " database:");
                try {
                    jdbcTemplate2.execute(SQLScriptForCreatingTable);
                    System.out.println("Table created successfully");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    return false;
                }

                System.out.println('\n' + "Importing data to new database...");
                configureForDBPostgres(jdbcTemplate2, connectionData2);
                try {
                    csvDataImporter.importCsvDataToTable(table, generatedColumns);
                    System.out.println("Data imported to table: " + table);
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    return false;
                }

                // Delete all CSV files from the project that have not been deleted for some reason
                csvDataImporter.deleteAllCsvFiles();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public void testDatabaseConnection(JdbcTemplate jdbcTemplate, String dbName) {
        try {
            // Attempt to connect to the database
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println(dbName + " Database connection successful!");
        } catch (Exception e) {
            System.out.println(dbName + " Database connection unsuccessful. Error: " + e.getMessage());
        }
    }

    public void configureForDBPostgres(JdbcTemplate jdbcTemplate, ConnectionData connectionData) {
        if (connectionData.getDbms().equals("oracle")) {
            db1Url = String.format("jdbc:oracle:thin:@%s:%d:%s", connectionData.getHost(), connectionData.getPort(), connectionData.getNameDB());
        } else {
            db1Url = String.format("jdbc:%s://%s:%d/%s", connectionData.getDbms(), connectionData.getHost(), connectionData.getPort(), connectionData.getNameDB());
        }
        db1Username = connectionData.getUsernameDB();
        db1Password = connectionData.getPasswordDB();
        db1DriverClassName = connectionData.getDbDriverClassName();

        DataSource dataSource = DatabaseConfig.postgreSQLDataSource(db1Url, db1Username, db1Password);

        jdbcTemplate.setDataSource(dataSource);
    }
}
