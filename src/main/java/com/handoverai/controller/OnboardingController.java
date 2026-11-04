package com.handoverai.controller;

import com.handoverai.service.ProjectService;
import com.handoverai.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class OnboardingController {

    private final ReportService reportService;
    private final ProjectService projectService;

    public OnboardingController(ReportService reportService, ProjectService projectService) {
        this.reportService = reportService;
        this.projectService = projectService;
    }

    @GetMapping("/incoming-view")
    public ResponseEntity<Map<String, Object>> getIncomingView(@PathVariable Long projectId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : "";
        projectService.assertProjectAccess(projectId, email);
        return ResponseEntity.ok(reportService.getIncomingView(projectId));
    }
}
