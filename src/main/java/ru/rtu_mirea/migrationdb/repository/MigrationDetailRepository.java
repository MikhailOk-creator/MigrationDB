package ru.rtu_mirea.migrationdb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rtu_mirea.migrationdb.entity.MigrationDetailData;

import java.util.UUID;

public interface MigrationDetailRepository extends JpaRepository<MigrationDetailData, UUID> {
}
