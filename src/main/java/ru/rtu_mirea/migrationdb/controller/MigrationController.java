package ru.rtu_mirea.migrationdb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rtu_mirea.migrationdb.MigrationService;
import ru.rtu_mirea.migrationdb.entity.ConnectionData;
import ru.rtu_mirea.migrationdb.entity.ConnectionsDataDTO;
import ru.rtu_mirea.migrationdb.entity.ResultOfMigration;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MigrationController {
    private final MigrationService migrationService;

    @PostMapping("/migrate")
    public ResponseEntity<?> migrate(@RequestBody ConnectionsDataDTO connectionsDataDTO) {
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

            if(connectionData1.getHost() == null || connectionData1.getNameDB() == null || connectionData1.getUsernameDB() == null || connectionData1.getPasswordDB() == null ||
                    connectionData2.getHost() == null || connectionData2.getNameDB() == null || connectionData2.getUsernameDB() == null || connectionData2.getPasswordDB() == null) {
                return ResponseEntity.badRequest().body("Migration failed: one or more fields are empty");
            }

            ResultOfMigration result = migrationService.migration(connectionData1, connectionData2);
            if (result.isStatus()) {
                return ResponseEntity.ok(result.getMessage());
            } else {
                return ResponseEntity.badRequest().body("Migration failed" + '\n' + result.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Migration failed: " + e.getMessage());
        }
    }

    private static ConnectionData setDBMS(ConnectionData connectionData, String dbmsChoice) {
        switch (dbmsChoice) {
            case "postgres":
                connectionData.setDbDriverClassName("org.postgresql.Driver");
                connectionData.setDbms("postgresql");
                break;
            case "oracle":
                connectionData.setDbDriverClassName("oracle.jdbc.driver.OracleDriver");
                connectionData.setDbms("oracle");
                break;
            default:
                System.out.println("Invalid choice. Exiting.");
                return null;
        }
        return connectionData;
    }
}
