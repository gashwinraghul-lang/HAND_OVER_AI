package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "connections")
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String service; // GITHUB, TRELLO, JIRA, GDRIVE

    @JsonIgnore
    @Column(columnDefinition = "TEXT", nullable = false)
    private String encryptedApiKey;

    @Column(nullable = false)
    private String keyLast4;

    private String status = "CONNECTED"; // CONNECTED, ERROR, REVOKED
    private String requestedScopes;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Connection() {}

    public Connection(Project project, String service, String encryptedApiKey, String keyLast4, String requestedScopes) {
        this.project = project;
        this.service = service;
        this.encryptedApiKey = encryptedApiKey;
        this.keyLast4 = keyLast4;
        this.requestedScopes = requestedScopes;
        this.status = "CONNECTED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getEncryptedApiKey() { return encryptedApiKey; }
    public void setEncryptedApiKey(String encryptedApiKey) { this.encryptedApiKey = encryptedApiKey; }

    public String getKeyLast4() { return keyLast4; }
    public void setKeyLast4(String keyLast4) { this.keyLast4 = keyLast4; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestedScopes() { return requestedScopes; }
    public void setRequestedScopes(String requestedScopes) { this.requestedScopes = requestedScopes; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
