package ru.rtu_mirea.migrationdb.entity;

public record ResultOfMigration (
        boolean status,
        String message
) {
}
