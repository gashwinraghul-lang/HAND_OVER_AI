package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "access_items")
public class AccessItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String systemName; // AWS, GlobalPay, GitHub, Datadog

    private String accountMasked; // e.g. "acct_***924"
    private String accessLevel;   // Admin, Read-Only, Billing
    private String transferStatus; // PENDING_REVOKE, TRANSFERRED, RETAINED

    public AccessItem() {}

    public AccessItem(Project project, String systemName, String accountMasked, String accessLevel, String transferStatus) {
        this.project = project;
        this.systemName = systemName;
        this.accountMasked = accountMasked;
        this.accessLevel = accessLevel;
        this.transferStatus = transferStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }

    public String getAccountMasked() { return accountMasked; }
    public void setAccountMasked(String accountMasked) { this.accountMasked = accountMasked; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public String getTransferStatus() { return transferStatus; }
    public void setTransferStatus(String transferStatus) { this.transferStatus = transferStatus; }
}
