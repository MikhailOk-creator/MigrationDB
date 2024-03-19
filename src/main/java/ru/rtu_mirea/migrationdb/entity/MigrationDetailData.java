package ru.rtu_mirea.migrationdb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "migration_detail_t")
public class MigrationDetailData {
    @Id
    private UUID id;
    @Column(name = "migration_id", nullable = false)
    private UUID migrationId;
    @Column(name = "source_table", nullable = false)
    private String sourceTable;
    @Column(name = "target_table", nullable = false)
    private String targetTable;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "start_time", nullable = false)
    private Timestamp startTime;
    @Column(name = "end_time")
    private Timestamp endTime;
    @Column(name = "duration")
    private double duration;
    @Column(name = "error_message")
    private String errorMessage;

    public MigrationDetailData() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMigrationId() {
        return migrationId;
    }

    public void setMigrationId(UUID migrationId) {
        this.migrationId = migrationId;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
