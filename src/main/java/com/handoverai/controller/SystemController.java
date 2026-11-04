package com.handoverai.controller;

import com.handoverai.ai.AIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Small public read-only endpoint that lets the UI show whether the AI layer is
 * live or simulated. No secrets are exposed.
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final AIService aiService;
    private final boolean faceLoginEnabled;

    public SystemController(AIService aiService,
                            @Value("${handoverai.face-login-enabled:true}") boolean faceLoginEnabled) {
        this.aiService = aiService;
        this.faceLoginEnabled = faceLoginEnabled;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "mockAi", aiService.isMockMode(),
                "aiMode", aiService.isMockMode() ? "SIMULATED" : "LIVE",
                "faceLoginEnabled", faceLoginEnabled
        ));
    }
}
