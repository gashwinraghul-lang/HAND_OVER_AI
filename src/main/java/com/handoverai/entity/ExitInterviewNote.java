package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exit_interview_notes")
public class ExitInterviewNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private String employeeName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String notesContent;

    @Column(columnDefinition = "TEXT")
    private String keyTakeaways;

    private LocalDateTime recordedAt = LocalDateTime.now();

    public ExitInterviewNote() {}

    public ExitInterviewNote(Project project, String employeeName, String notesContent, String keyTakeaways) {
        this.project = project;
        this.employeeName = employeeName;
        this.notesContent = notesContent;
        this.keyTakeaways = keyTakeaways;
        this.recordedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getNotesContent() { return notesContent; }
    public void setNotesContent(String notesContent) { this.notesContent = notesContent; }

    public String getKeyTakeaways() { return keyTakeaways; }
    public void setKeyTakeaways(String keyTakeaways) { this.keyTakeaways = keyTakeaways; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
