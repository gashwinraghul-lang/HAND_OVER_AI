package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    private Long userId;
    private String userName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String userQuery;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String aiResponse;

    @Column(columnDefinition = "TEXT")
    private String citationsJson;

    private boolean isFlaggedWrong = false;
    private String flagReason;
    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatMessage() {}

    public ChatMessage(Project project, Long userId, String userName, String userQuery, String aiResponse, String citationsJson) {
        this.project = project;
        this.userId = userId;
        this.userName = userName;
        this.userQuery = userQuery;
        this.aiResponse = aiResponse;
        this.citationsJson = citationsJson;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserQuery() { return userQuery; }
    public void setUserQuery(String userQuery) { this.userQuery = userQuery; }

    public String getAiResponse() { return aiResponse; }
    public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }

    public String getCitationsJson() { return citationsJson; }
    public void setCitationsJson(String citationsJson) { this.citationsJson = citationsJson; }

    public boolean isFlaggedWrong() { return isFlaggedWrong; }
    public void setFlaggedWrong(boolean flaggedWrong) { isFlaggedWrong = flaggedWrong; }

    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
