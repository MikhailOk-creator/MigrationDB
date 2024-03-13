package ru.rtu_mirea.migrationdb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "migration_t")
public class MigrationData {
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
    private Date startTime;
    @Column(name = "end_time")
    private Date endTime;
    @Column(name = "duration")
    private double duration;
    @Column(name = "error_message")
    private String errorMessage;
}
