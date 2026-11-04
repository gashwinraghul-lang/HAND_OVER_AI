package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_face_profiles")
public class UserFaceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @JsonIgnore
    @Column(columnDefinition = "TEXT", nullable = false)
    private String encryptedEmbedding; // AES-GCM encrypted 128-float face descriptor

    private LocalDateTime consentGivenAt;
    private LocalDateTime enrolledAt = LocalDateTime.now();

    public UserFaceProfile() {}

    public UserFaceProfile(User user, String encryptedEmbedding, LocalDateTime consentGivenAt) {
        this.user = user;
        this.encryptedEmbedding = encryptedEmbedding;
        this.consentGivenAt = consentGivenAt;
        this.enrolledAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEncryptedEmbedding() { return encryptedEmbedding; }
    public void setEncryptedEmbedding(String encryptedEmbedding) { this.encryptedEmbedding = encryptedEmbedding; }

    public LocalDateTime getConsentGivenAt() { return consentGivenAt; }
    public void setConsentGivenAt(LocalDateTime consentGivenAt) { this.consentGivenAt = consentGivenAt; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(LocalDateTime enrolledAt) { this.enrolledAt = enrolledAt; }
}
