package com.handoverai.service;

import com.handoverai.ai.AIService;
import com.handoverai.connector.Connector;
import com.handoverai.connector.ConnectorFactory;
import com.handoverai.entity.*;
import com.handoverai.repository.*;
import com.handoverai.security.EncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final SyncRunRepository syncRunRepository;
    private final ProjectRepository projectRepository;
    private final TaskItemRepository taskRepository;
    private final DocumentChunkRepository chunkRepository;
    private final AnalysisResultRepository analysisRepository;
    private final ConnectorFactory connectorFactory;
    private final EncryptionUtil encryptionUtil;
    private final AIService aiService;

    public ConnectionService(ConnectionRepository connectionRepository,
                             SyncRunRepository syncRunRepository,
                             ProjectRepository projectRepository,
                             TaskItemRepository taskRepository,
                             DocumentChunkRepository chunkRepository,
                             AnalysisResultRepository analysisRepository,
                             ConnectorFactory connectorFactory,
                             EncryptionUtil encryptionUtil,
                             AIService aiService) {
        this.connectionRepository = connectionRepository;
        this.syncRunRepository = syncRunRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.chunkRepository = chunkRepository;
        this.analysisRepository = analysisRepository;
        this.connectorFactory = connectorFactory;
        this.encryptionUtil = encryptionUtil;
        this.aiService = aiService;
    }

    public List<Connection> getProjectConnections(Long projectId) {
        return connectionRepository.findByProjectId(projectId);
    }

    private Connection requireConnectionInProject(Long projectId, Long connectionId) {
        Connection conn = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new IllegalArgumentException("Connection not found: " + connectionId));
        if (!conn.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Connection does not belong to this project.");
        }
        return conn;
    }

    @Transactional
    public Map<String, Object> testConnection(Long projectId, Long connectionId) {
        Connection conn = requireConnectionInProject(projectId, connectionId);

        String decryptedKey = encryptionUtil.decrypt(conn.getEncryptedApiKey());
        Connector connector = connectorFactory.getConnector(conn.getService())
                .orElseThrow(() -> new IllegalArgumentException("Unsupported connector service: " + conn.getService()));

        boolean ok = connector.testConnection(decryptedKey);
        conn.setStatus(ok ? "CONNECTED" : "ERROR");
        connectionRepository.save(conn);

        Map<String, Object> result = new HashMap<>();
        result.put("service", conn.getService());
        result.put("ok", ok);
        result.put("message", ok
                ? conn.getService() + " credentials are valid."
                : conn.getService() + " credentials could not be validated.");
        return result;
    }

    @Transactional
    public Connection rotateKey(Long projectId, Long connectionId, String newApiKey) {
        if (newApiKey == null || newApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("A new API key is required.");
        }
        Connection conn = requireConnectionInProject(projectId, connectionId);

        Connector connector = connectorFactory.getConnector(conn.getService())
                .orElseThrow(() -> new IllegalArgumentException("Unsupported connector service: " + conn.getService()));
        if (!connector.testConnection(newApiKey)) {
            throw new IllegalArgumentException("Failed to validate the new " + conn.getService() + " key.");
        }

        String last4 = newApiKey.length() > 4 ? newApiKey.substring(newApiKey.length() - 4) : newApiKey;
        conn.setEncryptedApiKey(encryptionUtil.encrypt(newApiKey));
        conn.setKeyLast4(last4);
        conn.setStatus("CONNECTED");
        return connectionRepository.save(conn);
    }

    @Transactional
    public Connection createConnection(Long projectId, String service, String apiKey, String scopes) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        Connector connector = connectorFactory.getConnector(service)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported connector service: " + service));

        if (!connector.testConnection(apiKey)) {
            throw new IllegalArgumentException("Failed to validate " + service + " API credentials.");
        }

        String encryptedKey = encryptionUtil.encrypt(apiKey);
        String last4 = apiKey.length() > 4 ? apiKey.substring(apiKey.length() - 4) : apiKey;

        Connection connection = new Connection(project, service.toUpperCase(), encryptedKey, last4, scopes);
        return connectionRepository.save(connection);
    }

    @Transactional
    public void deleteConnection(Long projectId, Long connectionId) {
        Connection conn = requireConnectionInProject(projectId, connectionId);
        connectionRepository.delete(conn);
    }

    @Transactional
    public Map<String, Object> syncProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        List<Connection> connections = connectionRepository.findByProjectId(projectId);
        int totalIngested = 0;

        for (Connection conn : connections) {
            String decryptedKey = encryptionUtil.decrypt(conn.getEncryptedApiKey());
            Connector connector = connectorFactory.getConnector(conn.getService()).orElse(null);

            if (connector != null) {
                List<Connector.SyncedItem> items = connector.sync(decryptedKey);
                for (Connector.SyncedItem item : items) {
                    TaskItem task = new TaskItem(
                            project,
                            item.getTitle(),
                            item.getStatus(),
                            item.getOwner(),
                            item.getDeadline(),
                            item.getSourceUrl(),
                            item.isOverdue(),
                            item.isUnowned()
                    );
                    taskRepository.save(task);
                }
                totalIngested += items.size();

                conn.setLastSyncedAt(LocalDateTime.now());
                conn.setStatus("CONNECTED");
                connectionRepository.save(conn);

                SyncRun run = new SyncRun(conn, items.size(), "SUCCESS", "Synced " + items.size() + " items successfully.");
                syncRunRepository.save(run);
            }
        }

        // Run AI Analysis
        List<TaskItem> allTasks = taskRepository.findByProjectId(projectId);
        List<DocumentChunk> allChunks = chunkRepository.findByDocumentProjectId(projectId);

        AIService.AnalysisResultData analysisData = aiService.analyzeProjectProgress(project, allTasks, allChunks);

        AnalysisResult result = new AnalysisResult(
                project,
                analysisData.getCompletedCount(),
                analysisData.getWipCount(),
                analysisData.getBlockedCount(),
                analysisData.getUnownedCount(),
                analysisData.getOverdueCount(),
                analysisData.getConflictsSummary(),
                analysisData.getExecutiveSummary()
        );
        analysisRepository.save(result);

        Map<String, Object> resp = new HashMap<>();
        resp.put("itemsSynced", totalIngested);
        resp.put("analysis", result);
        return resp;
    }
}
