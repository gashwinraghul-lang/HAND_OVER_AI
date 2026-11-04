package com.handoverai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handoverai.ai.AIService;
import com.handoverai.dto.AppDTOs.ChatResponseDto;
import com.handoverai.entity.*;
import com.handoverai.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ProjectRepository projectRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TaskItemRepository taskRepository;
    private final DecisionItemRepository decisionRepository;
    private final RiskItemRepository riskRepository;
    private final AIService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ChatMessageRepository chatMessageRepository,
                       ProjectRepository projectRepository,
                       DocumentChunkRepository chunkRepository,
                       TaskItemRepository taskRepository,
                       DecisionItemRepository decisionRepository,
                       RiskItemRepository riskRepository,
                       AIService aiService) {
        this.chatMessageRepository = chatMessageRepository;
        this.projectRepository = projectRepository;
        this.chunkRepository = chunkRepository;
        this.taskRepository = taskRepository;
        this.decisionRepository = decisionRepository;
        this.riskRepository = riskRepository;
        this.aiService = aiService;
    }

    public List<ChatMessage> getProjectHistory(Long projectId) {
        return chatMessageRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
    }

    @Transactional
    public ChatResponseDto processQuery(Long projectId, Long userId, String userName, String query) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        List<DocumentChunk> chunks = chunkRepository.findByDocumentProjectId(projectId);
        List<TaskItem> tasks = taskRepository.findByProjectId(projectId);
        List<DecisionItem> decisions = decisionRepository.findByProjectId(projectId);
        List<RiskItem> risks = riskRepository.findByProjectId(projectId);

        AIService.ChatResult result = aiService.answerProjectQuery(project, query, chunks, tasks, decisions, risks);

        String citationsJson = "[]";
        try {
            citationsJson = objectMapper.writeValueAsString(result.getCitations());
        } catch (Exception ignored) {}

        ChatMessage msg = new ChatMessage(project, userId, userName, query, result.getAnswer(), citationsJson);
        msg = chatMessageRepository.save(msg);

        return new ChatResponseDto(msg.getId(), msg.getUserQuery(), msg.getAiResponse(), result.getCitations(), msg.isFlaggedWrong());
    }

    @Transactional
    public void flagMessage(Long projectId, Long messageId, String reason) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
        if (!msg.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Message does not belong to this project.");
        }
        msg.setFlaggedWrong(true);
        msg.setFlagReason(reason);
        chatMessageRepository.save(msg);
    }
}
