package com.handoverai.controller;

import com.handoverai.dto.AppDTOs.ApprovalRequest;
import com.handoverai.entity.HandoverReport;
import com.handoverai.entity.User;
import com.handoverai.repository.UserRepository;
import com.handoverai.service.ProjectService;
import com.handoverai.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;
    private final ProjectService projectService;

    public ReportController(ReportService reportService, UserRepository userRepository, ProjectService projectService) {
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.projectService = projectService;
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "";
    }

    @GetMapping
    public ResponseEntity<List<HandoverReport>> listReports(@PathVariable Long projectId) {
        projectService.assertProjectAccess(projectId, currentEmail());
        return ResponseEntity.ok(reportService.getProjectReports(projectId));
    }

    @PostMapping("/generate")
    public ResponseEntity<HandoverReport> generateReport(@PathVariable Long projectId) {
        projectService.assertProjectAccess(projectId, currentEmail());
        HandoverReport report = reportService.generateReport(projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/{reportId}/approve")
    public ResponseEntity<HandoverReport> reviewReport(@PathVariable Long projectId,
                                                       @PathVariable Long reportId,
                                                       @RequestBody ApprovalRequest request,
                                                       HttpSession session) {
        projectService.assertProjectAccess(projectId, currentEmail());
        reportService.assertReportInProject(projectId, reportId);

        String reviewerName = (String) session.getAttribute("userName");

        // Fallback to SecurityContext
        if (reviewerName == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    reviewerName = user.getName();
                }
            }
        }

        if (reviewerName == null) {
            reviewerName = "Unknown Reviewer";
        }

        HandoverReport report = reportService.reviewReport(reportId, reviewerName, request.getDecision(), request.getComments());
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{reportId}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long projectId,
                                              @PathVariable Long reportId) {
        projectService.assertProjectAccess(projectId, currentEmail());
        reportService.assertReportInProject(projectId, reportId);
        byte[] pdfBytes = reportService.downloadPdf(reportId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "handover-report-" + reportId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
