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
    public CommandLineRunner run (JdbcTemplate jdbcTemplate) {
        return args -> {
            Scanner scanner = new Scanner(System.in);

            System.out.print("Choose the DBMS:" + '\n' +
                    "1) PostgreSQL;" + '\n' +
                    "2) MySQL;" + '\n' +
                    "3) Oracle" + '\n' +
                    "-> ");
            int dbmsChoice = scanner.nextInt();

            String dbDriverClassName, dbms, host, nameDB, usernameDB, passwordDB;
            int port;

            System.out.print("Enter the host: ");
            host = scanner.next();
            System.out.print("Enter the port: ");
            port = scanner.nextInt();
            System.out.print("Enter the name of the database: ");
            nameDB = scanner.next();
            System.out.print("Enter the username: ");
            usernameDB = scanner.next();
            System.out.print("Enter the password: ");
            passwordDB = scanner.next();

            switch (dbmsChoice) {
                case 1:
                    dbDriverClassName = "org.postgresql.Driver";
                    dbms = "postgresql";
                    break;
                case 2:
                    dbDriverClassName = "com.mysql.cj.jdbc.Driver";
                    dbms = "mysql";
                    break;
                case 3:
                    dbDriverClassName = "oracle.jdbc.driver.OracleDriver";
                    dbms = "oracle";
                    break;
                default:
                    System.out.println("Invalid choice. Exiting.");
                    return;
            }

            configureForDB1(jdbcTemplate, dbms, host, port, nameDB, usernameDB, passwordDB, dbDriverClassName);

            testDatabaseConnection(jdbcTemplate, nameDB);
        };
    }

    private void configureForDB1(JdbcTemplate jdbcTemplate,
                                 String dbms,
                                 String host,
                                 int port,
                                 String name,
                                 String username,
                                 String password,
                                 String driver) {
        if (dbms.equals("oracle")) {
            db1Url = String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, name);
        } else {
            db1Url = String.format("jdbc:%s://%s:%d/%s", dbms, host, port, name);
        }
        db1Username = username;
        db1Password = password;
        db1DriverClassName = driver;

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
