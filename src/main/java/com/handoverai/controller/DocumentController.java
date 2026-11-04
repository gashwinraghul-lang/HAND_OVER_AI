package com.handoverai.controller;

import com.handoverai.entity.Document;
import com.handoverai.service.DocumentService;
import com.handoverai.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final ProjectService projectService;

    public DocumentController(DocumentService documentService, ProjectService projectService) {
        this.documentService = documentService;
        this.projectService = projectService;
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "";
    }

    @GetMapping
    public ResponseEntity<List<Document>> listDocuments(@PathVariable Long projectId) {
        projectService.assertWorkspaceAccess(projectId, currentEmail());
        return ResponseEntity.ok(documentService.getProjectDocuments(projectId));
    }

    @PostMapping
    public ResponseEntity<Document> uploadDocument(@PathVariable Long projectId,
                                                   @RequestParam("file") MultipartFile file) {
        projectService.assertWorkspaceAccess(projectId, currentEmail());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String uploadedBy = auth != null ? auth.getName() : "unknown";
        Document doc = documentService.uploadDocument(projectId, file, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }
}
