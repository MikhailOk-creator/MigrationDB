package ru.rtu_mirea.migrationdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class MigrationDbApplication {

    @Value("${spring.datasource.url}")
    private String db1Url;

    @Value("${spring.datasource.username}")
    private String db1Username;

    @Value("${spring.datasource.password}")
    private String db1Password;

    @Value("${spring.datasource.driver-class-name}")
    private String db1DriverClassName;

    public static void main(String[] args) {
        SpringApplication.run(MigrationDbApplication.class, args);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public CommandLineRunner run (JdbcTemplate jdbcTemplate1, JdbcTemplate jdbcTemplate2) {
        return args -> {
            Scanner scanner = new Scanner(System.in);

            System.out.println('\n' + "Database 1");
            ConnectionData connectionData1 = commandLineEnter(scanner);

            assert connectionData1 != null;
            configureForDBPostgres(jdbcTemplate1, connectionData1);
            testDatabaseConnection(jdbcTemplate1, connectionData1.getNameDB());


            System.out.println('\n'+"Database 2");
            ConnectionData connectionData2 = commandLineEnter(scanner);

            assert connectionData2 != null;
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
        };
    }

    public static ConnectionData commandLineEnter (Scanner scanner) {
        ConnectionData connectionData = new ConnectionData();

        System.out.print("Choose the DBMS:" + '\n' +
                "1) PostgreSQL;" + '\n' +
                "2) MySQL;" + '\n' +
                "3) Oracle" + '\n' +
                "-> ");
        int dbmsChoice = scanner.nextInt();

        // Set connection data to the first connection
        System.out.print("Enter the host: ");
        connectionData.setHost(scanner.next());
        System.out.print("Enter the port: ");
        connectionData.setPort(scanner.nextInt());
        System.out.print("Enter the name of the database: ");
        connectionData.setNameDB(scanner.next());
        System.out.print("Enter the username: ");
        connectionData.setUsernameDB(scanner.next());
        System.out.print("Enter the password: ");
        connectionData.setPasswordDB(scanner.next());

        connectionData = setDBMS(connectionData, dbmsChoice);

        return connectionData;
    }

    private static ConnectionData setDBMS(ConnectionData connectionData, int dbmsChoice) {
        switch (dbmsChoice) {
            case 1:
                connectionData.setDbDriverClassName("org.postgresql.Driver");
                connectionData.setDbms("postgresql");
                break;
            case 2:
                connectionData.setDbDriverClassName("com.mysql.cj.jdbc.Driver");
                connectionData.setDbms("mysql");
                break;
            case 3:
                connectionData.setDbDriverClassName("oracle.jdbc.driver.OracleDriver");
                connectionData.setDbms("oracle");
                break;
            default:
                System.out.println("Invalid choice. Exiting.");
                return null;
        }
        return connectionData;
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
