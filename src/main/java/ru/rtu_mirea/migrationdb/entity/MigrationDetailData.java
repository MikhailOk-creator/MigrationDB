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
    private Date startTime;
    @Column(name = "end_time")
    private Date endTime;
    @Column(name = "duration")
    private double duration;
    @Column(name = "error_message")
    private String errorMessage;
}
