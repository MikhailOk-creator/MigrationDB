package ru.rtu_mirea.migrationdb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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

        Scanner scanner = new Scanner(System.in);

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
        // reverse array
        for (int i = 0; i < resultArray.length / 2; i++) {
            int temp = resultArray[i];
            resultArray[i] = resultArray[resultArray.length - i - 1];
            resultArray[resultArray.length - i - 1] = temp;
        }
        for (int j : resultArray) {
            System.out.print(tables1.get(j) + " ");
        }

        // Info about columns of all tables
        ArrayList<ArrayList<ColumnInfo>> allColumnsInTables = new ArrayList<>();
        Map<String, ArrayList<String>> primaryKeys = new HashMap<>();
        for (String table : tables1) {
            try {
                ArrayList<ColumnInfo> columns = informationBySQL.getInfoAboutColumnsOfTable(table, jdbcTemplate1);
                primaryKeys.put(table, informationBySQL.getPrimaryKeyOfTable(table, jdbcTemplate1));
                allColumnsInTables.add(columns);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        // Print info about columns of all tables
        System.out.println('\n' + "Info about columns of all tables:");
        for (int i = 0; i < tables1.size(); i++) {
            System.out.println("Table: " + tables1.get(i));
            for (ColumnInfo column : allColumnsInTables.get(i)) {
                System.out.println(column);
            }
            System.out.println();
        }

        // Export data from all tables to CSV
        ExportToCSV exportOrigToCSV = new ExportToCSV(jdbcTemplate1);
        for (int i = 0; i < tables1.size(); i++) {
            try {
                exportOrigToCSV.exportTableToCsv(tables1.get(resultArray[i]));
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        // Create SQL for all tables
        System.out.println('\n');
        CreateSQL createSQL = new CreateSQL();
        ArrayList<String> sql_scripts = new ArrayList<>();
        for (int i = 0; i < tables1.size(); i++) {
            try {
                String sql = createSQL.createSQLForTable(tables1.get(resultArray[i]), allColumnsInTables.get(resultArray[i]), relations, primaryKeys);
                sql_scripts.add(sql);
                System.out.println(sql);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        // Create tables in new database
        configureForDBPostgres(jdbcTemplate2, connectionData2);
        System.out.println('\n' + "Creating tables in " + connectionData2.getNameDB() + " database:");
        int i = 0;
        for (String sql : sql_scripts) {
            try {
                jdbcTemplate2.execute(sql);
                System.out.println("Table created successfully");
                i++;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        if (i == tables1.size()) {
            System.out.println("All tables created successfully");
        } else {
            System.out.println("Some tables were not created");
        }

        System.out.println("Importing data to new database...");
        // TODO: Add method to import data from CSV to new database
        /*configureForDBPostgres(jdbcTemplate2, connectionData2);
        CsvDataImporter csvDataImporter = new CsvDataImporter(jdbcTemplate2);
        for (int j = 0; j < tables1.size(); j++) {
            try {
                csvDataImporter.importCsvDataToTable(tables1.get(resultArray[j]));
                System.out.println("Data imported to table: " + tables1.get(resultArray[j]));
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }*/
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
