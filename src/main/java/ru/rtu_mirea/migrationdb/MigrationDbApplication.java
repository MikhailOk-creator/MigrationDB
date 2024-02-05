package ru.rtu_mirea.migrationdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

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

    private final String sqlRepository = "src/main/resources/sql";

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
            configureForDB1(jdbcTemplate1, connectionData1);
            testDatabaseConnection(jdbcTemplate1, connectionData1.getNameDB());


            System.out.println('\n'+"Database 2");
            ConnectionData connectionData2 = commandLineEnter(scanner);

            assert connectionData2 != null;
            configureForDB1(jdbcTemplate2, connectionData2);
            testDatabaseConnection(jdbcTemplate2, connectionData2.getNameDB());

            ArrayList<String> tables1 = getNameOfAllTables(jdbcTemplate1);
            System.out.println('\n' + "Tables in " + connectionData1.getNameDB() + " database:");
            for (String table : tables1) {
                System.out.println(table);
            }

            // Matrix of connections between tables
            int[][] connections = new int[tables1.size()][tables1.size()];
            for (String table : tables1) {
                try {
                    ArrayList<String>connections_between_one = getInfoAboutConnectionOfTables(table, jdbcTemplate1);
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

    private void configureForDB1(JdbcTemplate jdbcTemplate, ConnectionData connectionData) {
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

    private void testDatabaseConnection(JdbcTemplate jdbcTemplate, String dbName) {
        try {
            // Attempt to connect to the database
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            System.out.println(dbName + " Database connection successful!");
        } catch (Exception e) {
            System.out.println(dbName + " Database connection unsuccessful. Error: " + e.getMessage());
        }
    }

    private ArrayList<String> getNameOfAllTables(JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/all_tables.sql");
        return (ArrayList<String>) jdbcTemplate.query(source_sql, (rs, rowNum) -> {
            return rs.getString("table_name");
        });
    }

    private ArrayList<String> getInfoAboutConnectionOfTables(String nameOfTable, JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/table_connections.sql");
        String sql = String.format(source_sql, nameOfTable);
        return  (ArrayList<String>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("foreign_table_name");
        });
    }

    private String readSqlFromFile(String filePath) throws Exception {
        return Files.readString(Path.of(filePath));
    }
}
