package com.handoverai.controller;

import com.handoverai.dto.AppDTOs.ConnectionRequest;
import com.handoverai.entity.Connection;
import com.handoverai.service.ConnectionService;
import com.handoverai.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/{projectId}/connections")
public class ConnectionController {

    private final ConnectionService connectionService;
    private final ProjectService projectService;

    public ConnectionController(ConnectionService connectionService, ProjectService projectService) {
        this.connectionService = connectionService;
        this.projectService = projectService;
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "";
    }

    private void requireAccess(Long projectId) {
        projectService.assertWorkspaceAccess(projectId, currentEmail());
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listConnections(@PathVariable Long projectId) {
        requireAccess(projectId);
        List<Connection> connections = connectionService.getProjectConnections(projectId);

        // Mask keys - never return raw or encrypted keys to the browser.
        List<Map<String, Object>> masked = connections.stream().map(conn -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", conn.getId());
            m.put("service", conn.getService());
            m.put("keyLast4", conn.getKeyLast4());
            m.put("status", conn.getStatus());
            m.put("requestedScopes", conn.getRequestedScopes());
            m.put("lastSyncedAt", conn.getLastSyncedAt());
            m.put("createdAt", conn.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(masked);
    }

    @PostMapping
    public ResponseEntity<?> createConnection(@PathVariable Long projectId,
                                              @RequestBody ConnectionRequest request) {
        requireAccess(projectId);
        Connection conn = connectionService.createConnection(
                projectId, request.getService(), request.getApiKey(), request.getRequestedScopes());

        Map<String, Object> response = new HashMap<>();
        response.put("id", conn.getId());
        response.put("service", conn.getService());
        response.put("keyLast4", conn.getKeyLast4());
        response.put("status", conn.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{connId}")
    public ResponseEntity<?> deleteConnection(@PathVariable Long projectId,
                                              @PathVariable Long connId) {
        requireAccess(projectId);
        connectionService.deleteConnection(projectId, connId);
        return ResponseEntity.ok(Map.of("message", "Connection deleted successfully."));
    }

    @PostMapping("/{connId}/test")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable Long projectId,
                                                              @PathVariable Long connId) {
        requireAccess(projectId);
        return ResponseEntity.ok(connectionService.testConnection(projectId, connId));
    }

    @PostMapping("/{connId}/rotate")
    public ResponseEntity<?> rotateKey(@PathVariable Long projectId,
                                       @PathVariable Long connId,
                                       @RequestBody ConnectionRequest request) {
        requireAccess(projectId);
        Connection conn = connectionService.rotateKey(projectId, connId, request.getApiKey());
        Map<String, Object> response = new HashMap<>();
        response.put("id", conn.getId());
        response.put("service", conn.getService());
        response.put("keyLast4", conn.getKeyLast4());
        response.put("status", conn.getStatus());
        response.put("message", "API key rotated and encrypted.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncProject(@PathVariable Long projectId) {
        requireAccess(projectId);
        Map<String, Object> result = connectionService.syncProject(projectId);
        return ResponseEntity.ok(result);
    }
}
