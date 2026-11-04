package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "handover_reports")
public class HandoverReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String version = "v1.0";

    @Column(columnDefinition = "TEXT")
    private String executiveSummary;

    @Column(columnDefinition = "TEXT")
    private String completedWorkSummary;

    @Column(columnDefinition = "TEXT")
    private String pendingWorkSummary;

    @Column(columnDefinition = "TEXT")
    private String risksSummary;

    @Column(columnDefinition = "TEXT")
    private String accessInstructions;

    private String pdfPath;
    private String status = "PENDING_APPROVAL"; // PENDING_APPROVAL, APPROVED, REJECTED
    private LocalDateTime generatedAt = LocalDateTime.now();
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportApproval> approvals = new ArrayList<>();

    public HandoverReport() {}

    public HandoverReport(Project project, String version, String executiveSummary, String pdfPath) {
        this.project = project;
        this.version = version;
        this.executiveSummary = executiveSummary;
        this.pdfPath = pdfPath;
        this.status = "PENDING_APPROVAL";
        this.generatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public String getCompletedWorkSummary() { return completedWorkSummary; }
    public void setCompletedWorkSummary(String completedWorkSummary) { this.completedWorkSummary = completedWorkSummary; }

    public String getPendingWorkSummary() { return pendingWorkSummary; }
    public void setPendingWorkSummary(String pendingWorkSummary) { this.pendingWorkSummary = pendingWorkSummary; }

    public String getRisksSummary() { return risksSummary; }
    public void setRisksSummary(String risksSummary) { this.risksSummary = risksSummary; }

    public String getAccessInstructions() { return accessInstructions; }
    public void setAccessInstructions(String accessInstructions) { this.accessInstructions = accessInstructions; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public List<ReportApproval> getApprovals() { return approvals; }
    public void setApprovals(List<ReportApproval> approvals) { this.approvals = approvals; }
}
