package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class TaskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String title;

    private String status = "PENDING"; // COMPLETED, IN_PROGRESS, BLOCKED, OVERDUE
    private String owner; // null if unowned
    private LocalDate deadline;
    private String sourceUrl;
    private boolean isOverdue = false;
    private boolean isUnowned = false;

    public TaskItem() {}

    public TaskItem(Project project, String title, String status, String owner, LocalDate deadline, String sourceUrl, boolean isOverdue, boolean isUnowned) {
        this.project = project;
        this.title = title;
        this.status = status;
        this.owner = owner;
        this.deadline = deadline;
        this.sourceUrl = sourceUrl;
        this.isOverdue = isOverdue;
        this.isUnowned = isUnowned;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public boolean isOverdue() { return isOverdue; }
    public void setOverdue(boolean overdue) { isOverdue = overdue; }

    public boolean isUnowned() { return isUnowned; }
    public void setUnowned(boolean unowned) { isUnowned = unowned; }
}
