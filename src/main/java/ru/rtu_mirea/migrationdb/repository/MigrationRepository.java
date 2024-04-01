package ru.rtu_mirea.migrationdb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rtu_mirea.migrationdb.entity.MigrationData;

import java.util.List;
import java.util.UUID;

@Repository
public interface MigrationRepository extends JpaRepository<MigrationData, UUID> {
    List<MigrationData> findAllByOrderByEndTimeDesc();
}
