package ru.rtu_mirea.migrationdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
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
}
