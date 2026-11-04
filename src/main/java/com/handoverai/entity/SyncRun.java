package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sync_runs")
public class SyncRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false)
    private Connection connection;

    private int itemsSynced = 0;
    private String status = "SUCCESS"; // SUCCESS, FAILED
    private String details;
    private LocalDateTime syncedAt = LocalDateTime.now();

    public SyncRun() {}

    public SyncRun(Connection connection, int itemsSynced, String status, String details) {
        this.connection = connection;
        this.itemsSynced = itemsSynced;
        this.status = status;
        this.details = details;
        this.syncedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Connection getConnection() { return connection; }
    public void setConnection(Connection connection) { this.connection = connection; }

    public int getItemsSynced() { return itemsSynced; }
    public void setItemsSynced(int itemsSynced) { this.itemsSynced = itemsSynced; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getSyncedAt() { return syncedAt; }
    public void setSyncedAt(LocalDateTime syncedAt) { this.syncedAt = syncedAt; }
}
