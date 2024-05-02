package ru.rtu_mirea.migrationdb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "migration_t")
public class MigrationData {

    public MigrationData() {
    }

    @Id
    private UUID id;
    @Column(name = "source_host", nullable = false)
    private String sourceHost;
    @Column(name = "source_port", nullable = false)
    private int sourcePort;
    @Column(name = "source_db", nullable = false)
    private String sourceDB;
    @Column(name = "target_host", nullable = false)
    private String targetHost;
    @Column(name = "target_port", nullable = false)
    private int targetPort;
    @Column(name = "target_db", nullable = false)
    private String targetDB;
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
    @Column(name = "user_that_started", nullable = false)
    private String userThatStartedMigration;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public String getSourceDB() {
        return sourceDB;
    }

    public void setSourceDB(String sourceDB) {
        this.sourceDB = sourceDB;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public String getTargetDB() {
        return targetDB;
    }

    public void setTargetDB(String targetDB) {
        this.targetDB = targetDB;
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

    public String getUserThatStartedMigration() {
        return userThatStartedMigration;
    }

    public void setUserThatStartedMigration(String userThatStartedMigration) {
        this.userThatStartedMigration = userThatStartedMigration;
    }
}
