package com.handoverai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handoverai.dto.AppDTOs.CitationDto;
import com.handoverai.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * OpenAI-compatible live adapter. Enable with MOCK_AI_MODE=false and a real
 * LLM_API_KEY. The mock service stays the default so demos work offline.
 */
@Service
@ConditionalOnProperty(name = "handoverai.mock-ai", havingValue = "false")
public class LiveAIService implements AIService {

    private final String apiKey;
    private final String endpoint;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public LiveAIService(@Value("${handoverai.llm.api-key}") String apiKey,
                         @Value("${handoverai.llm.endpoint}") String endpoint,
                         @Value("${handoverai.llm.model}") String model) {
        this.apiKey = apiKey;
        this.endpoint = endpoint;
        this.model = model;
    }

    @Override
    public boolean isMockMode() {
        return false;
    }

    @Override
    public ChatResult answerProjectQuery(Project project, String query, List<DocumentChunk> chunks,
                                         List<TaskItem> tasks, List<DecisionItem> decisions, List<RiskItem> risks) {
        if (query == null || query.trim().isEmpty() || !isProjectRelated(query)) {
            return new ChatResult("its irrelevant", List.of());
        }

        List<CitationDto> citations = collectCitations(chunks, tasks, decisions, risks);
        String context = buildContext(chunks, tasks, decisions, risks);

        String systemPrompt = "You are HandOver.AI, a project handover assistant. Answer ONLY from the supplied "
                + "project context. If the answer is not present, say the information is missing and suggest which "
                + "connector or document would provide it. Never invent facts. Keep the answer concise. "
                + "Do not reveal API keys or secrets.";
        String userPrompt = "Project: " + project.getName() + "\n\nContext:\n" + context
                + "\n\nQuestion: " + query;

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("temperature", 0.2);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String response = restTemplate.postForObject(endpoint, new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response == null ? "{}" : response);
            String answer = root.path("choices").path(0).path("message").path("content").asText("");
            if (answer.isBlank()) {
                answer = "The live AI provider returned an empty answer. Please try again.";
            }
            return new ChatResult(answer, citations);
        } catch (Exception e) {
            return new ChatResult("The live AI provider is unavailable right now. "
                    + "Check LLM_API_KEY/LLM_API_ENDPOINT or switch MOCK_AI_MODE=true.", citations);
        }
    }

    @Override
    public AnalysisResultData analyzeProjectProgress(Project project, List<TaskItem> tasks, List<DocumentChunk> chunks) {
        int completed = 0, wip = 0, blocked = 0, unowned = 0, overdue = 0;
        for (TaskItem task : tasks) {
            if ("COMPLETED".equalsIgnoreCase(task.getStatus())) completed++;
            else if ("IN_PROGRESS".equalsIgnoreCase(task.getStatus())) wip++;
            else if ("BLOCKED".equalsIgnoreCase(task.getStatus())) blocked++;
            if (task.isUnowned()) unowned++;
            if (task.isOverdue()) overdue++;
        }

        String conflicts = "Review the supplied sources for conflicting dates or values before approval.";
        String summary = "Live analysis ingested " + tasks.size() + " tasks and " + chunks.size()
                + " document chunks. " + blocked + " blocked, " + overdue + " overdue and "
                + unowned + " unowned items need attention.";
        return new AnalysisResultData(completed, wip, blocked, unowned, overdue, conflicts, summary);
    }

    private boolean isProjectRelated(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        return List.of("vps", "renewal", "deadline", "payment", "globalpay", "access", "receipt",
                        "invoice", "task", "github", "trello", "infra", "ssl", "database", "hikari",
                        "risk", "decision", "progress", "handover", "status", "meeting", "sop",
                        "server", "cloud", "project")
                .stream().anyMatch(q::contains);
    }

    private List<CitationDto> collectCitations(List<DocumentChunk> chunks, List<TaskItem> tasks,
                                               List<DecisionItem> decisions, List<RiskItem> risks) {
        List<CitationDto> citations = new ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            String source = chunk.getSourceCitation() != null ? chunk.getSourceCitation()
                    : chunk.getDocument().getFileName();
            citations.add(new CitationDto(source, "Page " + chunk.getPageNumber(), null));
            if (citations.size() >= 4) break;
        }
        for (TaskItem task : tasks) {
            if (task.getSourceUrl() != null) {
                citations.add(new CitationDto("Connected task", task.getTitle(), task.getSourceUrl()));
            }
            if (citations.size() >= 6) break;
        }
        for (DecisionItem decision : decisions) {
            citations.add(new CitationDto("Decision record", decision.getSourceCitation(), null));
        }        for (RiskItem risk : risks) {
            citations.add(new CitationDto("Risk register", risk.getDescription(), null));
        }
        return citations.stream().distinct().limit(6).toList();
    }

    private String buildContext(List<DocumentChunk> chunks, List<TaskItem> tasks,
                                List<DecisionItem> decisions, List<RiskItem> risks) {
        StringBuilder sb = new StringBuilder();
        for (DocumentChunk chunk : chunks) {
            sb.append("- [").append(chunk.getSourceCitation() != null ? chunk.getSourceCitation() : "Document")
                    .append("] ").append(chunk.getContent()).append('\n');
        }
        for (TaskItem task : tasks) {
            sb.append("- [Task] ").append(task.getTitle()).append(" | status=").append(task.getStatus())
                    .append(" | owner=").append(task.getOwner() == null ? "unowned" : task.getOwner())
                    .append(" | deadline=").append(task.getDeadline()).append('\n');
        }
        for (DecisionItem decision : decisions) {
            sb.append("- [Decision] ").append(decision.getSummary()).append('\n');
        }
        for (RiskItem risk : risks) {
            sb.append("- [Risk] ").append(risk.getDescription()).append(" | severity=")
                    .append(risk.getSeverity()).append('\n');
        }
        return sb.length() == 0 ? "No project evidence is available." : sb.toString();
    }
}
