package ru.rtu_mirea.migrationdb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.rtu_mirea.migrationdb.service.MigrationService;
import ru.rtu_mirea.migrationdb.entity.ConnectionData;
import ru.rtu_mirea.migrationdb.dto.ConnectionsDataDTO;
import ru.rtu_mirea.migrationdb.entity.DatabaseManagementSystem;
import ru.rtu_mirea.migrationdb.entity.ResultOfMigration;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/migrate")
public class MigrationController {
    private final MigrationService migrationService;

    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("")
    public ResponseEntity<?> migrate(@RequestBody @Validated ConnectionsDataDTO connectionsDataDTO) {
        try {
            ConnectionData connectionData1 = new ConnectionData();
            connectionData1.setHost(connectionsDataDTO.getHost1());
            connectionData1.setPort(connectionsDataDTO.getPort1());
            connectionData1.setNameDB(connectionsDataDTO.getDatabase1());
            connectionData1.setUsernameDB(connectionsDataDTO.getUser1());
            connectionData1.setPasswordDB(connectionsDataDTO.getPassword1());
            connectionData1 = setDBMS(connectionData1, connectionsDataDTO.getDbms1());

            ConnectionData connectionData2 = new ConnectionData();
            connectionData2.setHost(connectionsDataDTO.getHost2());
            connectionData2.setPort(connectionsDataDTO.getPort2());
            connectionData2.setNameDB(connectionsDataDTO.getDatabase2());
            connectionData2.setUsernameDB(connectionsDataDTO.getUser2());
            connectionData2.setPasswordDB(connectionsDataDTO.getPassword2());
            connectionData2 = setDBMS(connectionData2, connectionsDataDTO.getDbms2());

            ResultOfMigration result = migrationService.migration(connectionData1, connectionData2);
            if (result.status()) {
                return ResponseEntity.ok(result.message());
            } else {
                return ResponseEntity.badRequest().body("Migration failed" + '\n' + result.message());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Migration failed: " + e.getMessage());
        }
    }

    private static ConnectionData setDBMS(ConnectionData connectionData, String dbmsChoice) {
        switch (dbmsChoice) {
            case "postgresql":
                connectionData.setDbDriverClassName("org.postgresql.Driver");
                connectionData.setDbms(DatabaseManagementSystem.POSTGRESQL);
                break;
            case "mysql":
                connectionData.setDbDriverClassName("com.mysql.cj.jdbc.Driver");
                connectionData.setDbms(DatabaseManagementSystem.MYSQL);
                break;
            /*case "oracle":
                connectionData.setDbDriverClassName("oracle.jdbc.driver.OracleDriver");
                connectionData.setDbms("oracle");
                break;*/
            default:
                System.out.println("Invalid choice. Exiting.");
                return null;
        }
        return connectionData;
    }
}
