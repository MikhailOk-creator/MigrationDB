package ru.rtu_mirea.migrationdb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.rtu_mirea.migrationdb.dto.ConnectionsDataDTO;

@Controller
@RequestMapping("/")
public class WebController {
    private final MigrationController migrate;
    private final DatabaseController database;

    public WebController(MigrationController migrate, DatabaseController database) {
        this.migrate = migrate;
        this.database = database;
    }

    @GetMapping("")
    public String index() {
        return "index";
    }

    @PostMapping("migrate_web")
    public String migrateWeb(@ModelAttribute ConnectionsDataDTO connectionsDataDTO, Model model) {
        try {
            String sourceDB = connectionsDataDTO.getDatabase1();
            String targetDB = connectionsDataDTO.getDatabase2();
            ResponseEntity<?> response = migrate.migrate(connectionsDataDTO);
            model.addAttribute("status", sourceDB + " -> " + targetDB + ": " + response.getBody());
            return "index";
        } catch (Exception e) {
            model.addAttribute("status", "Migration failed: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("migration_table")
    public String migrateTable (Model model) {
        ResponseEntity<?> response = database.getAllMigration();
        model.addAttribute("migrations", response.getBody());
        return "migration_table";
    }

    @GetMapping("migration_details/{uuid}")
    public String migrateDetails (@PathVariable String uuid, Model model) {
        ResponseEntity<?> response = database.getDetailsOfMigration(uuid);
        model.addAttribute("details", response.getBody());
        return "migration_details";
    }
}
