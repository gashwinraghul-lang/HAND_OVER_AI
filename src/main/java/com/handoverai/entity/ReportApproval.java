package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_approvals")
public class ReportApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private HandoverReport report;

    private String reviewerName;
    private String decision; // APPROVED, REJECTED
    private String comments;
    private LocalDateTime reviewedAt = LocalDateTime.now();

    public ReportApproval() {}

    public ReportApproval(HandoverReport report, String reviewerName, String decision, String comments) {
        this.report = report;
        this.reviewerName = reviewerName;
        this.decision = decision;
        this.comments = comments;
        this.reviewedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HandoverReport getReport() { return report; }
    public void setReport(HandoverReport report) { this.report = report; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
