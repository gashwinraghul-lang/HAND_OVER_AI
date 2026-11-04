package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "knowledge_gaps")
public class KnowledgeGap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String status = "PENDING"; // PENDING, RESOLVED
    private String suggestedSource;

    public KnowledgeGap() {}

    public KnowledgeGap(Project project, String description, String status, String suggestedSource) {
        this.project = project;
        this.description = description;
        this.status = status;
        this.suggestedSource = suggestedSource;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSuggestedSource() { return suggestedSource; }
    public void setSuggestedSource(String suggestedSource) { this.suggestedSource = suggestedSource; }
}
