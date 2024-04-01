package ru.rtu_mirea.migrationdb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rtu_mirea.migrationdb.entity.MigrationDetailData;

import java.util.List;
import java.util.UUID;

public interface MigrationDetailRepository extends JpaRepository<MigrationDetailData, UUID> {
    List<MigrationDetailData> findByMigrationId(UUID migrationId);
    List<MigrationDetailData> findAllByOrderByEndTimeDesc();
    List<MigrationDetailData> findByMigrationIdOrderByEndTimeDesc(UUID migrationId);
}
