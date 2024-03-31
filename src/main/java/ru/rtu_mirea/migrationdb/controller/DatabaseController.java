package ru.rtu_mirea.migrationdb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rtu_mirea.migrationdb.repository.MigrationDetailRepository;
import ru.rtu_mirea.migrationdb.repository.MigrationRepository;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/history")
public class DatabaseController {
    private final MigrationRepository migrationRepository;
    private final MigrationDetailRepository migrationDetailRepository;

    public DatabaseController(MigrationRepository migrationRepository, MigrationDetailRepository migrationDetailRepository) {
        this.migrationRepository = migrationRepository;
        this.migrationDetailRepository = migrationDetailRepository;
    }

    /**
     * Returns data on all migrations
     * @return а list of migrations
     * */
    @GetMapping("")
    public ResponseEntity<?> getAllMigration() {
        try {
            return ResponseEntity.ok(migrationRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something wrong: " + e.getMessage());
        }
    }

    /**
     * Returns data on all migrations details
     * @return а list of migrations details
     * */
    @GetMapping("/details")
    public ResponseEntity<?> getAllMigrationDetails() {
        try {
            return ResponseEntity.ok(migrationDetailRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something wrong: " + e.getMessage());
        }
    }

    /**
     * Returns data on all migrations details by uuid of migrations
     * @return a list of information about a specific migration
     * */
    @GetMapping("/{migrationId}")
    public ResponseEntity<?> getDetailsOfMigration(@PathVariable(name = "migrationId") String uuidOfMigration) {
        try {
            return ResponseEntity.ok(migrationDetailRepository.findByMigrationId(UUID.fromString(uuidOfMigration)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something wrong: " + e.getMessage());
        }
    }
}
