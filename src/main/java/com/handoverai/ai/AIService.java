package com.handoverai.ai;

import com.handoverai.dto.AppDTOs.CitationDto;
import com.handoverai.entity.*;

import java.util.List;

public interface AIService {

    boolean isMockMode();

    class ChatResult {
        private final String answer;
        private final List<CitationDto> citations;

        public ChatResult(String answer, List<CitationDto> citations) {
            this.answer = answer;
            this.citations = citations;
        }

        public String getAnswer() { return answer; }
        public List<CitationDto> getCitations() { return citations; }
    }

    class AnalysisResultData {
        private final int completedCount;
        private final int wipCount;
        private final int blockedCount;
        private final int unownedCount;
        private final int overdueCount;
        private final String conflictsSummary;
        private final String executiveSummary;

        public AnalysisResultData(int completedCount, int wipCount, int blockedCount, int unownedCount, int overdueCount, String conflictsSummary, String executiveSummary) {
            this.completedCount = completedCount;
            this.wipCount = wipCount;
            this.blockedCount = blockedCount;
            this.unownedCount = unownedCount;
            this.overdueCount = overdueCount;
            this.conflictsSummary = conflictsSummary;
            this.executiveSummary = executiveSummary;
        }

        public int getCompletedCount() { return completedCount; }
        public int getWipCount() { return wipCount; }
        public int getBlockedCount() { return blockedCount; }
        public int getUnownedCount() { return unownedCount; }
        public int getOverdueCount() { return overdueCount; }
        public String getConflictsSummary() { return conflictsSummary; }
        public String getExecutiveSummary() { return executiveSummary; }
    }

    ChatResult answerProjectQuery(Project project, String query, List<DocumentChunk> chunks, List<TaskItem> tasks, List<DecisionItem> decisions, List<RiskItem> risks);

    AnalysisResultData analyzeProjectProgress(Project project, List<TaskItem> tasks, List<DocumentChunk> chunks);
}
