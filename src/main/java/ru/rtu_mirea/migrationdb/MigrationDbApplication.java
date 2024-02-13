package ru.rtu_mirea.migrationdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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


           /* System.out.println('\n'+"Database 2");
            ConnectionData connectionData2 = commandLineEnter(scanner);

            assert connectionData2 != null;
            configureForDB1(jdbcTemplate2, connectionData2);
            testDatabaseConnection(jdbcTemplate2, connectionData2.getNameDB());
            */
            ArrayList<String> tables1 = getNameOfAllTables(jdbcTemplate1);
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
                    ArrayList<String>connections_between_one = getInfoAboutConnectionOfTablesToMatrix(table, jdbcTemplate1);
                    relations.put(table, getInfoAboutConnectionOfTablesToClass(table, jdbcTemplate1));
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
                    ArrayList<ColumnInfo> columns = getInfoAboutColumnsOfTable(table, jdbcTemplate1);
                    primaryKeys.put(table, getPrimaryKeyOfTable(table, jdbcTemplate1));
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
            CreateSQL createSQL = new CreateSQL();
            ArrayList<String> sql_scripts = new ArrayList<>();
            for (int i = 0; i < tables1.size(); i++) {
                try {
                    String sql = createSQL.createSQLForTable(tables1.get(resultArray[i]), allColumnsInTables.get(resultArray[i]), connections, resultArray[i], tables1, relations, primaryKeys);
                    sql_scripts.add(sql);
                    System.out.println(sql);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
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

    private ArrayList<String> getInfoAboutConnectionOfTablesToMatrix(String nameOfTable, JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/table_connections.sql");
        String sql = String.format(source_sql, nameOfTable);
        return  (ArrayList<String>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("foreign_table_name");
        });
    }

    private ArrayList<RelationData> getInfoAboutConnectionOfTablesToClass (String nameOfTable, JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/table_connections.sql");
        String sql = String.format(source_sql, nameOfTable);
        return (ArrayList<RelationData>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            RelationData relationData = new RelationData();
            relationData.setTableName(rs.getString("table_name"));
            relationData.setColumnName(rs.getString("column_name"));
            relationData.setRefTableName(rs.getString("foreign_table_name"));
            relationData.setRefColumnName(rs.getString("foreign_column_name"));
            return relationData;
        });
    }

    private ArrayList<ColumnInfo> getInfoAboutColumnsOfTable(String nameOfTable, JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/information_about_columns.sql");
        String sql = String.format(source_sql, nameOfTable);
        return (ArrayList<ColumnInfo>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setColumnName(rs.getString("column_name"));
            columnInfo.setTableSchema(rs.getString("table_schema"));
            columnInfo.setOrdinalPosition(rs.getInt("ordinal_position"));
            columnInfo.setNullable(rs.getString("is_nullable").equals("YES"));
            columnInfo.setDataType(rs.getString("udt_name"));
            columnInfo.setIdentity(rs.getString("is_identity").equals("YES"));
            columnInfo.setIdentityGeneration(rs.getString("identity_generation"));
            columnInfo.setIdentityStart(rs.getString("identity_start"));
            columnInfo.setIdentityIncrement(rs.getString("identity_increment"));
            columnInfo.setIdentityMaximum(rs.getString("identity_maximum"));
            columnInfo.setIdentityMinimum(rs.getString("identity_minimum"));
            columnInfo.setIdentityCycle(String.valueOf(rs.getString("identity_cycle").equals("YES")));
            columnInfo.setUpdatable(rs.getString("is_updatable").equals("YES"));
            columnInfo.setColumnDefault(rs.getString("column_default"));
            return columnInfo;
        });
    }

    private ArrayList<String> getPrimaryKeyOfTable(String nameOfTable, JdbcTemplate jdbcTemplate) throws Exception {
        String source_sql = readSqlFromFile(sqlRepository + "/primary_key_of_table.sql");
        String sql = String.format(source_sql, nameOfTable);
        return (ArrayList<String>) jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getString("column_name");
        });
    }

    private String readSqlFromFile(String filePath) throws Exception {
        return Files.readString(Path.of(filePath));
    }
}
