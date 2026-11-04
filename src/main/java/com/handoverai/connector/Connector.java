package com.handoverai.connector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface Connector {
    String getServiceName();
    boolean testConnection(String apiKey);
    List<SyncedItem> sync(String apiKey);

    class SyncedItem {
        private final String type; // TASK, ISSUE, CARD, DOC
        private final String title;
        private final String status;
        private final String owner;
        private final LocalDate deadline;
        private final String sourceUrl;
        private final boolean isOverdue;
        private final boolean isUnowned;

        public SyncedItem(String type, String title, String status, String owner, LocalDate deadline, String sourceUrl, boolean isOverdue, boolean isUnowned) {
            this.type = type;
            this.title = title;
            this.status = status;
            this.owner = owner;
            this.deadline = deadline;
            this.sourceUrl = sourceUrl;
            this.isOverdue = isOverdue;
            this.isUnowned = isUnowned;
        }

        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getStatus() { return status; }
        public String getOwner() { return owner; }
        public LocalDate getDeadline() { return deadline; }
        public String getSourceUrl() { return sourceUrl; }
        public boolean isOverdue() { return isOverdue; }
        public boolean isUnowned() { return isUnowned; }
    }
}
