package com.handoverai.ai;

import com.handoverai.dto.AppDTOs.CitationDto;
import com.handoverai.entity.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "handoverai.mock-ai", havingValue = "true", matchIfMissing = true)
public class MockAIService implements AIService {

    @Override
    public boolean isMockMode() {
        return true;
    }

    @Override
    public ChatResult answerProjectQuery(Project project, String query, List<DocumentChunk> chunks, List<TaskItem> tasks, List<DecisionItem> decisions, List<RiskItem> risks) {
        if (query == null || query.trim().isEmpty()) {
            return new ChatResult("its irrelevant", List.of());
        }

        String q = query.toLowerCase().trim();

        // 1. Off-topic filter: If question is not related to the project or its data, exactly "its irrelevant"
        boolean isProjectRelated = q.contains("vps") || q.contains("renewal") || q.contains("deadline") ||
                q.contains("date") || q.contains("payment") || q.contains("globalpay") || q.contains("access") ||
                q.contains("receipt") || q.contains("invoice") || q.contains("task") || q.contains("github") ||
                q.contains("trello") || q.contains("infra") || q.contains("ssl") || q.contains("database") ||
                q.contains("hikari") || q.contains("risk") || q.contains("decision") || q.contains("progress") ||
                q.contains("handover") || q.contains("status") || q.contains("meeting") || q.contains("sop") ||
                q.contains("server") || q.contains("cloud") || q.contains("project");

        if (!isProjectRelated) {
            return new ChatResult("its irrelevant", List.of());
        }

        List<CitationDto> citations = new ArrayList<>();

        // 2. Conflict query: Renewal deadline
        if (q.contains("deadline") || q.contains("renewal date") || q.contains("when") || q.contains("due date")) {
            citations.add(new CitationDto("VPS Renewal SOP.pdf", "Page 3, Section 2.1", "/api/documents/download/1"));
            citations.add(new CitationDto("Team Meeting Notes.txt", "Section 1", "/api/documents/download/2"));
            String answer = "⚠️ **Conflict Detected in Project Evidence:**\n\n" +
                    "- **Source 1:** `VPS Renewal SOP.pdf` (Page 3, Section 2.1) specifies the renewal deadline as **30 June 2025**.\n" +
                    "- **Source 2:** `Team Meeting Notes.txt` (Section 1) records the deadline as **25 June 2025**.\n\n" +
                    "Both values have been flagged for manager review to confirm whether early renewal on 25 June was agreed upon.";
            return new ChatResult(answer, citations);
        }

        // 3. Payment / Billing / GlobalPay
        if (q.contains("payment") || q.contains("pay") || q.contains("billing") || q.contains("globalpay") || q.contains("credit card") || q.contains("vendor")) {
            citations.add(new CitationDto("VPS Renewal SOP.pdf", "Page 3, Section 2.1", "/api/documents/download/1"));
            citations.add(new CitationDto("Trello Card", "TRELLO-42", "https://trello.com/c/vps-infra/42-audit-globalpay"));
            String answer = "Payment is processed via **GlobalPay** automated recurring billing portal. " +
                    "The card on file is masked as `acct_***924`.\n\n" +
                    "The ongoing audit of the recurring subscription is currently tracked on Trello Card TRELLO-42.";
            return new ChatResult(answer, citations);
        }

        // 4. Access / Credentials / Permissions
        if (q.contains("access") || q.contains("share") || q.contains("team") || q.contains("permission") || q.contains("credentials")) {
            citations.add(new CitationDto("VPS Renewal SOP.pdf", "Page 3, Section 2.1", "/api/documents/download/1"));
            String answer = "Access credentials are scheduled to be shared directly with the Infrastructure Team upon renewal completion. " +
                    "Multi-factor authentication must be updated to the incoming lead's hardware token.";
            return new ChatResult(answer, citations);
        }

        // 5. Missing evidence: Receipt / Invoice
        if (q.contains("receipt") || q.contains("invoice") || q.contains("proof")) {
            String answer = "⚠️ **Missing Evidence Detected:**\n\n" +
                    "The information is currently missing from the knowledge base. No renewal receipt or payment confirmation has been uploaded to the system yet.\n\n" +
                    "**Recommended Action:** Please upload the vendor renewal receipt PDF or connect the billing email source.";
            return new ChatResult(answer, List.of());
        }

        // 6. Tasks / Progress / Blocked / Overdue
        if (q.contains("task") || q.contains("issue") || q.contains("github") || q.contains("trello") || q.contains("blocked") || q.contains("overdue")) {
            citations.add(new CitationDto("GitHub Issue", "GH-104", "https://github.com/company/vps-infra/issues/104"));
            citations.add(new CitationDto("GitHub Issue", "GH-108", "https://github.com/company/vps-infra/issues/108"));
            citations.add(new CitationDto("Trello Card", "TRELLO-42", "https://trello.com/c/vps-infra/42-audit-globalpay"));
            String answer = "Current project work status:\n" +
                    "- **Completed:** `GH-108` (Update SSL certificates on edge load balancers).\n" +
                    "- **Blocked & Overdue (No Owner):** `GH-104` (Migrate production DB pool to HikariCP) — past due since 20 June 2025.\n" +
                    "- **In Progress:** `TRELLO-42` (Audit GlobalPay billing portal) — assigned to outgoing lead.";
            return new ChatResult(answer, citations);
        }

        // 7. General fallback for project query
        citations.add(new CitationDto("VPS Renewal SOP.pdf", "Page 1-3", "/api/documents/download/1"));
        String answer = "The VPS Infrastructure Renewal project encompasses server cluster migration, GlobalPay billing updates, and handover to the incoming infrastructure team. " +
                "Readiness score is currently at **78%**, with 1 unresolved conflict and 1 missing renewal receipt.";
        return new ChatResult(answer, citations);
    }

    @Override
    public AnalysisResultData analyzeProjectProgress(Project project, List<TaskItem> tasks, List<DocumentChunk> chunks) {
        int completed = 0;
        int wip = 0;
        int blocked = 0;
        int unowned = 0;
        int overdue = 0;

        for (TaskItem t : tasks) {
            if ("COMPLETED".equalsIgnoreCase(t.getStatus())) completed++;
            else if ("IN_PROGRESS".equalsIgnoreCase(t.getStatus())) wip++;
            else if ("BLOCKED".equalsIgnoreCase(t.getStatus())) blocked++;

            if (t.isUnowned()) unowned++;
            if (t.isOverdue()) overdue++;
        }

        String conflicts = "Conflict detected between VPS Renewal SOP.pdf (30 June 2025) and Team Meeting Notes.txt (25 June 2025).";
        String summary = "Automated AI Analysis completed. Ingested " + tasks.size() + " connected tasks and " +
                chunks.size() + " document knowledge chunks. 1 critical date discrepancy flagged for manager resolution.";

        return new AnalysisResultData(completed, wip, blocked, unowned, overdue, conflicts, summary);
    }
}
