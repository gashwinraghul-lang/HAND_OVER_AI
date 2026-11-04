package com.handoverai.service;

import com.handoverai.entity.*;
import com.handoverai.report.PdfReportGeneratorService;
import com.handoverai.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final HandoverReportRepository reportRepository;
    private final ReportApprovalRepository approvalRepository;
    private final ProjectRepository projectRepository;
    private final TaskItemRepository taskRepository;
    private final DecisionItemRepository decisionRepository;
    private final RiskItemRepository riskRepository;
    private final AccessItemRepository accessItemRepository;
    private final OnboardingPlanRepository onboardingPlanRepository;
    private final PdfReportGeneratorService pdfGenerator;

    public ReportService(HandoverReportRepository reportRepository,
                         ReportApprovalRepository approvalRepository,
                         ProjectRepository projectRepository,
                         TaskItemRepository taskRepository,
                         DecisionItemRepository decisionRepository,
                         RiskItemRepository riskRepository,
                         AccessItemRepository accessItemRepository,
                         OnboardingPlanRepository onboardingPlanRepository,
                         PdfReportGeneratorService pdfGenerator) {
        this.reportRepository = reportRepository;
        this.approvalRepository = approvalRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.decisionRepository = decisionRepository;
        this.riskRepository = riskRepository;
        this.accessItemRepository = accessItemRepository;
        this.onboardingPlanRepository = onboardingPlanRepository;
        this.pdfGenerator = pdfGenerator;
    }

    public List<HandoverReport> getProjectReports(Long projectId) {
        return reportRepository.findByProjectIdOrderByGeneratedAtDesc(projectId);
    }

    public HandoverReport getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
    }

    @Transactional(readOnly = true)
    public void assertReportInProject(Long projectId, Long reportId) {
        HandoverReport report = getReportById(reportId);
        if (!report.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Report does not belong to this project.");
        }
    }

    @Transactional
    public HandoverReport generateReport(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        List<TaskItem> tasks = taskRepository.findByProjectId(projectId);
        List<DecisionItem> decisions = decisionRepository.findByProjectId(projectId);
        List<RiskItem> risks = riskRepository.findByProjectId(projectId);

        String summary = "Automated Handover Report for " + project.getName() + ".\n" +
                "Readiness Score: " + project.getReadinessScore() + "%. " +
                tasks.size() + " total tasks tracked, " + decisions.size() + " documented architectural decisions, and " +
                risks.size() + " flagged risks.";

        HandoverReport report = new HandoverReport(project, "v1.0", summary, null);
        report.setCompletedWorkSummary("All edge load balancer SSL certificates updated and verified.");
        report.setPendingWorkSummary("DB connection pool migration to HikariCP (GH-104) is blocked and overdue. Trello card 42 GlobalPay audit in progress.");
        report.setRisksSummary("Date discrepancy between SOP (30 June) and Meeting Notes (25 June). Missing vendor renewal receipt.");
        report.setAccessInstructions("GlobalPay billing credentials registered under acct_***924. Infra access transfer scheduled upon sign-off.");
        report.setStatus("PENDING_APPROVAL");

        return reportRepository.save(report);
    }

    @Transactional
    public HandoverReport reviewReport(Long reportId, String reviewerName, String decision, String comments) {
        if (!"APPROVED".equalsIgnoreCase(decision) && !"REJECTED".equalsIgnoreCase(decision)) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED.");
        }
        HandoverReport report = getReportById(reportId);
        report.setStatus("APPROVED".equalsIgnoreCase(decision) ? "APPROVED" : "REJECTED");
        if ("APPROVED".equalsIgnoreCase(decision)) {
            report.setApprovedAt(LocalDateTime.now());
            report.getProject().setStatus("APPROVED");
            projectRepository.save(report.getProject());
        }

        ReportApproval approval = new ReportApproval(report, reviewerName, decision.toUpperCase(), comments);
        approvalRepository.save(approval);

        return reportRepository.save(report);
    }

    public byte[] downloadPdf(Long reportId) {
        HandoverReport report = getReportById(reportId);
        Project project = report.getProject();

        List<TaskItem> tasks = taskRepository.findByProjectId(project.getId());
        List<DecisionItem> decisions = decisionRepository.findByProjectId(project.getId());
        List<RiskItem> risks = riskRepository.findByProjectId(project.getId());
        List<AccessItem> accessItems = accessItemRepository.findByProjectId(project.getId());
        List<OnboardingPlan> plans = onboardingPlanRepository.findByProjectIdOrderByPhaseAsc(project.getId());

        return pdfGenerator.generateHandoverPdf(project, report, tasks, decisions, risks, accessItems, plans);
    }

    public Map<String, Object> getIncomingView(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        HandoverReport approvedReport = reportRepository.findTopByProjectIdAndStatusOrderByGeneratedAtDesc(projectId, "APPROVED")
                .orElse(null);

        List<OnboardingPlan> plans = onboardingPlanRepository.findByProjectIdOrderByPhaseAsc(projectId);
        List<AccessItem> accessItems = accessItemRepository.findByProjectId(projectId);

        Map<String, Object> view = new HashMap<>();
        view.put("project", project);
        view.put("approvedReport", approvedReport);
        view.put("onboardingPlans", plans);
        view.put("accessItems", accessItems);
        return view;
    }
}
