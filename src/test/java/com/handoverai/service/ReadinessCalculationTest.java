package com.handoverai.service;

import com.handoverai.entity.KnowledgeGap;
import com.handoverai.entity.Project;
import com.handoverai.entity.RiskItem;
import com.handoverai.entity.TaskItem;
import com.handoverai.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadinessCalculationTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository memberRepository;
    @Mock private TaskItemRepository taskRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private RiskItemRepository riskRepository;
    @Mock private DecisionItemRepository decisionRepository;
    @Mock private KnowledgeGapRepository gapRepository;
    @Mock private AnalysisResultRepository analysisRepository;
    @Mock private UserRepository userRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, memberRepository, taskRepository,
                documentRepository, riskRepository, decisionRepository, gapRepository,
                analysisRepository, userRepository);
    }

    @Test
    void returnsDemoReadinessWithGapAndAttentionItems() {
        Project project = demoProject();
        TaskItem overdueAndUnowned = new TaskItem(project, "Blocked DB migration", "BLOCKED",
                null, LocalDate.of(2025, 6, 20), "https://github.com/company/vps-infra/issues/104", true, true);
        TaskItem completed = new TaskItem(project, "SSL certificates", "COMPLETED",
                "outgoing.dev", LocalDate.of(2025, 6, 15), null, false, false);
        KnowledgeGap gap = new KnowledgeGap(project, "Missing renewal receipt", "PENDING", "Finance");
        RiskItem risk = new RiskItem(project, "No receipt", "HIGH", "Upload receipt", "AI Analysis");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of(overdueAndUnowned, completed));
        when(gapRepository.findByProjectId(1L)).thenReturn(List.of(gap));
        when(riskRepository.findByProjectId(1L)).thenReturn(List.of(risk));

        Map<String, Object> metrics = projectService.calculateReadiness(1L);

        assertEquals(78, metrics.get("readinessScore"));
        assertEquals(22, metrics.get("missingPercentage"));
        assertEquals(3, metrics.get("itemsNeedingAttention"));
        assertEquals(1, metrics.get("openRisksCount"));
        assertEquals(2, metrics.get("tasksCount"));
    }

    @Test
    void derivesScoreFromCompletedTasksWhenStoredScoreIsUnset() {
        Project project = demoProject();
        project.setReadinessScore(0);

        List<TaskItem> tasks = List.of(
                new TaskItem(project, "Done 1", "COMPLETED", "a", null, null, false, false),
                new TaskItem(project, "Done 2", "COMPLETED", "a", null, null, false, false),
                new TaskItem(project, "Open 1", "IN_PROGRESS", "b", null, null, false, false),
                new TaskItem(project, "Open 2", "BLOCKED", "b", null, null, false, false)
        );

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(1L)).thenReturn(tasks);
        when(gapRepository.findByProjectId(1L)).thenReturn(List.of());
        when(riskRepository.findByProjectId(1L)).thenReturn(List.of());

        Map<String, Object> metrics = projectService.calculateReadiness(1L);

        assertEquals(50, metrics.get("readinessScore"));
        assertEquals(50, metrics.get("missingPercentage"));
    }

    @Test
    void fallsBackToDemoScoreWhenNoDataExists() {
        Project project = demoProject();
        project.setReadinessScore(0);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(1L)).thenReturn(List.of());
        when(gapRepository.findByProjectId(1L)).thenReturn(List.of());
        when(riskRepository.findByProjectId(1L)).thenReturn(List.of());

        Map<String, Object> metrics = projectService.calculateReadiness(1L);

        assertEquals(78, metrics.get("readinessScore"));
        assertEquals(22, metrics.get("missingPercentage"));
    }

    private Project demoProject() {
        Project project = new Project("VPS Infrastructure Renewal", "Demo", "outgoing@handoverai.com");
        project.setId(1L);
        project.setReadinessScore(78);
        return project;
    }
}
