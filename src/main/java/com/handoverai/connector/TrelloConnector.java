package com.handoverai.connector;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class TrelloConnector implements Connector {

    @Override
    public String getServiceName() {
        return "TRELLO";
    }

    @Override
    public boolean testConnection(String apiKey) {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public List<SyncedItem> sync(String apiKey) {
        List<SyncedItem> items = new ArrayList<>();
        // "one Trello card: one completed or in-progress item"
        items.add(new SyncedItem(
                "CARD",
                "TRELLO-42: Audit GlobalPay billing portal recurring subscriptions",
                "IN_PROGRESS",
                "outgoing.dev",
                LocalDate.of(2025, 6, 28),
                "https://trello.com/c/vps-infra/42-audit-globalpay",
                false,
                false
        ));
        return items;
    }
}
