package com.handoverai.dto;

import java.util.List;

public class AppDTOs {

    public static class CreateProjectRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ConnectionRequest {
        private String service;
        private String apiKey;
        private String requestedScopes;

        public String getService() { return service; }
        public void setService(String service) { this.service = service; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getRequestedScopes() { return requestedScopes; }
        public void setRequestedScopes(String requestedScopes) { this.requestedScopes = requestedScopes; }
    }

    public static class ChatRequest {
        private String query;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
    }

    public static class FlagChatRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ApprovalRequest {
        private String decision; // APPROVED or REJECTED
        private String comments;

        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }

    public static class CitationDto {
        private String source;
        private String pageOrSection;
        private String link;

        public CitationDto() {}
        public CitationDto(String source, String pageOrSection, String link) {
            this.source = source;
            this.pageOrSection = pageOrSection;
            this.link = link;
        }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getPageOrSection() { return pageOrSection; }
        public void setPageOrSection(String pageOrSection) { this.pageOrSection = pageOrSection; }
        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
    }

    public static class ChatResponseDto {
        private Long id;
        private String query;
        private String response;
        private List<CitationDto> citations;
        private boolean isFlagged;

        public ChatResponseDto(Long id, String query, String response, List<CitationDto> citations, boolean isFlagged) {
            this.id = id;
            this.query = query;
            this.response = response;
            this.citations = citations;
            this.isFlagged = isFlagged;
        }

        public Long getId() { return id; }
        public String getQuery() { return query; }
        public String getResponse() { return response; }
        public List<CitationDto> getCitations() { return citations; }
        public boolean isFlagged() { return isFlagged; }
    }
}
