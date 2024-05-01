package ru.rtu_mirea.migrationdb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rtu_mirea.migrationdb.repository.MigrationDetailRepository;
import ru.rtu_mirea.migrationdb.repository.MigrationRepository;
import ru.rtu_mirea.migrationdb.repository.UserRepository;

import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/database")
public class DatabaseController {
    private final MigrationRepository migrationRepository;
    private final MigrationDetailRepository migrationDetailRepository;
    private final UserRepository userRepository;

    public DatabaseController(MigrationRepository migrationRepository,
                              MigrationDetailRepository migrationDetailRepository,
                              UserRepository userRepository) {
        this.migrationRepository = migrationRepository;
        this.migrationDetailRepository = migrationDetailRepository;
        this.userRepository = userRepository;
    }

    /**
     * Returns data on all migrations
     * @return а list of migrations
     * */
    @GetMapping("/history")
    public ResponseEntity<?> getAllMigration() {
        try {
            return ResponseEntity.ok(migrationRepository.findAllByOrderByEndTimeDesc());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something wrong: " + e.getMessage());
        }
    }

    /**
     * Returns data on all migrations details
     * @return а list of migrations details
     * */
    @GetMapping("/history/details")
    public ResponseEntity<?> getAllMigrationDetails() {
        try {
            return ResponseEntity.ok(migrationDetailRepository.findAllByOrderByEndTimeDesc());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something wrong: " + e.getMessage());
        }
    }

    /**
     * Returns data on all migrations details by uuid of migrations
     * @return a list of information about a specific migration
     * */
    @GetMapping("/history/{migrationId}")
    public ResponseEntity<?> getDetailsOfMigration(@PathVariable(name = "migrationId") String uuidOfMigration) {
        try {
            return ResponseEntity.ok(migrationDetailRepository.findByMigrationIdOrderByEndTimeDesc(UUID.fromString(uuidOfMigration)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something wrong: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something wrong: " + e.getMessage());
        }
    }
}
