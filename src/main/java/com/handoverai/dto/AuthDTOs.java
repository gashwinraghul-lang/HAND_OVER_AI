package com.handoverai.dto;

import java.util.List;

public class AuthDTOs {

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class SignupRequest {
        private String name;
        private String email;
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class FaceVerifyRequest {
        private String email;
        private String password;
        private List<Double> embedding;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public List<Double> getEmbedding() { return embedding; }
        public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }
    }

    public static class FaceEnrollRequest {
        private List<Double> embedding;
        private boolean consent;

        public List<Double> getEmbedding() { return embedding; }
        public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }

        public boolean isConsent() { return consent; }
        public void setConsent(boolean consent) { this.consent = consent; }
    }

    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String role;
        private boolean hasFaceEnrolled;

        public UserResponse(Long id, String name, String email, String role, boolean hasFaceEnrolled) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
            this.hasFaceEnrolled = hasFaceEnrolled;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public boolean isHasFaceEnrolled() { return hasFaceEnrolled; }
    }
}
