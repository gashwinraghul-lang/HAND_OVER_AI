package com.handoverai.ai;

import com.handoverai.entity.Project;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MockAIServiceTest {

    private final MockAIService aiService = new MockAIService();

    @Test
    void offTopicQuestionsReplyExactlyItsIrrelevant() {
        AIService.ChatResult result = aiService.answerProjectQuery(
                new Project("VPS Infrastructure Renewal", "Demo", "outgoing@handoverai.com"),
                "What is the capital of France?",
                List.of(), List.of(), List.of(), List.of());

        assertEquals("its irrelevant", result.getAnswer());
        assertTrue(result.getCitations().isEmpty());
    }

    @Test
    void blankQuestionsAreAlsoIrrelevant() {
        AIService.ChatResult result = aiService.answerProjectQuery(
                new Project("VPS Infrastructure Renewal", "Demo", "outgoing@handoverai.com"),
                "   ",
                List.of(), List.of(), List.of(), List.of());

        assertEquals("its irrelevant", result.getAnswer());
        assertTrue(result.getCitations().isEmpty());
    }

    @Test
    void projectQuestionsReturnAnswersWithCitations() {
        AIService.ChatResult result = aiService.answerProjectQuery(
                new Project("VPS Infrastructure Renewal", "Demo", "outgoing@handoverai.com"),
                "When is the VPS renewal deadline?",
                List.of(), List.of(), List.of(), List.of());

        assertNotEquals("its irrelevant", result.getAnswer());
        assertFalse(result.getCitations().isEmpty());
        assertTrue(result.getAnswer().contains("30 June 2025"));
        assertTrue(result.getAnswer().contains("25 June 2025"));
        assertTrue(result.getCitations().stream()
                .anyMatch(c -> c.getSource().contains("VPS Renewal SOP")));
    }

    @Test
    void missingEvidenceQuestionIsFlaggedInsteadOfInvented() {
        AIService.ChatResult result = aiService.answerProjectQuery(
                new Project("VPS Infrastructure Renewal", "Demo", "outgoing@handoverai.com"),
                "Where is the renewal receipt?",
                List.of(), List.of(), List.of(), List.of());

        assertTrue(result.getAnswer().toLowerCase().contains("missing"));
        assertTrue(result.getCitations().isEmpty());
    }
}
