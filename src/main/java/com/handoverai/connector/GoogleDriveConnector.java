package com.handoverai.connector;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleDriveConnector implements Connector {

    @Override
    public String getServiceName() {
        return "GDRIVE";
    }

    @Override
    public boolean testConnection(String apiKey) {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public List<SyncedItem> sync(String apiKey) {
        return new ArrayList<>();
    }
}
