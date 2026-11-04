package com.handoverai.controller;

import com.handoverai.dto.AppDTOs.CreateProjectRequest;
import com.handoverai.entity.Project;
import com.handoverai.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : "";
        return ResponseEntity.ok(projectService.getProjectsForUser(email));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = auth != null ? auth.getName() : "unknown";
        Project project = projectService.createProject(request.getName(), request.getDescription(), createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProjectDetails(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : "";
        projectService.assertProjectAccess(id, email);
        return ResponseEntity.ok(projectService.getProjectDetails(id, email));
    }

    @GetMapping("/{id}/readiness")
    public ResponseEntity<Map<String, Object>> getReadiness(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : "";
        projectService.assertProjectAccess(id, email);
        return ResponseEntity.ok(projectService.calculateReadiness(id));
    }
}
