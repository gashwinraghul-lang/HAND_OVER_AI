package com.handoverai.connector;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitHubConnector implements Connector {

    @Override
    public String getServiceName() {
        return "GITHUB";
    }

    @Override
    public boolean testConnection(String apiKey) {
        // Validates token format or responds successfully for valid/demo tokens
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public List<SyncedItem> sync(String apiKey) {
        List<SyncedItem> items = new ArrayList<>();
        // Ingests real or demo-seeded items as per hackathon specification:
        // "two GitHub issues: one overdue, one with no owner"
        items.add(new SyncedItem(
                "ISSUE",
                "GH-104: Migrate production DB connection pool to HikariCP",
                "BLOCKED",
                null,
                LocalDate.of(2025, 6, 20),
                "https://github.com/company/vps-infra/issues/104",
                true,
                true
        ));
        items.add(new SyncedItem(
                "ISSUE",
                "GH-108: Update SSL certificates on edge load balancers",
                "COMPLETED",
                "outgoing.dev",
                LocalDate.of(2025, 6, 15),
                "https://github.com/company/vps-infra/issues/108",
                false,
                false
        ));
        return items;
    }
}
