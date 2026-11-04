package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private int completedCount = 0;
    private int wipCount = 0;
    private int blockedCount = 0;
    private int unownedCount = 0;
    private int overdueCount = 0;

    @Column(columnDefinition = "TEXT")
    private String conflictsSummary;

    @Column(columnDefinition = "TEXT")
    private String executiveSummary;

    private LocalDateTime analyzedAt = LocalDateTime.now();

    public AnalysisResult() {}

    public AnalysisResult(Project project, int completedCount, int wipCount, int blockedCount, int unownedCount, int overdueCount, String conflictsSummary, String executiveSummary) {
        this.project = project;
        this.completedCount = completedCount;
        this.wipCount = wipCount;
        this.blockedCount = blockedCount;
        this.unownedCount = unownedCount;
        this.overdueCount = overdueCount;
        this.conflictsSummary = conflictsSummary;
        this.executiveSummary = executiveSummary;
        this.analyzedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public int getWipCount() { return wipCount; }
    public void setWipCount(int wipCount) { this.wipCount = wipCount; }

    public int getBlockedCount() { return blockedCount; }
    public void setBlockedCount(int blockedCount) { this.blockedCount = blockedCount; }

    public int getUnownedCount() { return unownedCount; }
    public void setUnownedCount(int unownedCount) { this.unownedCount = unownedCount; }

    public int getOverdueCount() { return overdueCount; }
    public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }

    public String getConflictsSummary() { return conflictsSummary; }
    public void setConflictsSummary(String conflictsSummary) { this.conflictsSummary = conflictsSummary; }

    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
