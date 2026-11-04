package com.handoverai.controller;

import com.handoverai.entity.Connection;
import com.handoverai.entity.Project;
import com.handoverai.service.ConnectionService;
import com.handoverai.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Ensures the connections API never returns the full key or the encrypted value.
 */
@ExtendWith(MockitoExtension.class)
class ApiKeyMaskingTest {

    @Mock private ConnectionService connectionService;
    @Mock private ProjectService projectService;

    @InjectMocks private ConnectionController connectionController;

    @Test
    void listConnectionsOnlyExposesLastFourCharacters() {
        Project project = new Project("VPS Infrastructure Renewal", "Demo", "outgoing@handoverai.com");
        project.setId(1L);
        Connection connection = new Connection(project, "GITHUB",
                "encrypted-cipher-text-that-must-not-leak", "7890", "repo,issues");
        connection.setId(5L);

        when(connectionService.getProjectConnections(1L)).thenReturn(List.of(connection));

        ResponseEntity<List<Map<String, Object>>> response =
                connectionController.listConnections(1L);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        Map<String, Object> item = response.getBody().get(0);

        assertEquals("7890", item.get("keyLast4"));
        assertFalse(item.containsKey("apiKey"));
        assertFalse(item.containsKey("encryptedApiKey"));
        assertFalse(item.values().contains("encrypted-cipher-text-that-must-not-leak"));
    }
}
