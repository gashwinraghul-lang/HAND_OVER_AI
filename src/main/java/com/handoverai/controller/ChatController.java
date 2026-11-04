package com.handoverai.controller;

import com.handoverai.dto.AppDTOs.ChatRequest;
import com.handoverai.dto.AppDTOs.ChatResponseDto;
import com.handoverai.dto.AppDTOs.FlagChatRequest;
import com.handoverai.entity.ChatMessage;
import com.handoverai.entity.User;
import com.handoverai.repository.UserRepository;
import com.handoverai.service.ChatService;
import com.handoverai.service.ProjectService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    public ChatController(ChatService chatService, UserRepository userRepository, ProjectService projectService) {
        this.chatService = chatService;
        this.userRepository = userRepository;
        this.projectService = projectService;
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "";
    }

    @GetMapping
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long projectId) {
        projectService.assertWorkspaceAccess(projectId, currentEmail());
        return ResponseEntity.ok(chatService.getProjectHistory(projectId));
    }

    @PostMapping
    public ResponseEntity<ChatResponseDto> sendQuery(@PathVariable Long projectId,
                                                     @RequestBody ChatRequest request,
                                                     HttpSession session) {
        projectService.assertWorkspaceAccess(projectId, currentEmail());

        Long userId = (Long) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        // Fallback to SecurityContext if session attrs not set
        if (userId == null || userName == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    userId = user.getId();
                    userName = user.getName();
                }
            }
        }

        if (userId == null) {
            userId = 0L;
            userName = "Anonymous";
        }

        ChatResponseDto response = chatService.processQuery(projectId, userId, userName, request.getQuery());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{messageId}/flag")
    public ResponseEntity<?> flagMessage(@PathVariable Long projectId,
                                         @PathVariable Long messageId,
                                         @RequestBody FlagChatRequest request) {
        projectService.assertWorkspaceAccess(projectId, currentEmail());
        chatService.flagMessage(projectId, messageId, request.getReason());
        return ResponseEntity.ok(Map.of("message", "Message flagged successfully."));
    }
}
