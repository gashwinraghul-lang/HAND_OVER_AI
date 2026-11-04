package com.handoverai.service;

import com.handoverai.entity.Project;
import com.handoverai.entity.ProjectMember;
import com.handoverai.entity.User;
import com.handoverai.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Verifies that project permissions are enforced in the service layer rather
 * than only hidden in the UI.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServicePermissionTest {

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
    void adminCanAccessAnyProject() {
        User admin = new User("Admin", "admin@example.com", "hash", "ROLE_ADMIN");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        assertDoesNotThrow(() -> projectService.assertProjectAccess(1L, "admin@example.com"));
        verifyNoInteractions(memberRepository);
    }

    @Test
    void projectMemberCanAccessProject() {
        User employee = new User("Outgoing", "outgoing@example.com", "hash", "ROLE_OUTGOING");
        employee.setId(7L);
        Project project = new Project("Project", "Desc", "someone@example.com");
        project.setId(1L);

        when(userRepository.findByEmail("outgoing@example.com")).thenReturn(Optional.of(employee));
        when(memberRepository.findByProjectIdAndUserId(1L, 7L))
                .thenReturn(Optional.of(new ProjectMember(project, employee, "OUTGOING_OWNER")));

        assertDoesNotThrow(() -> projectService.assertProjectAccess(1L, "outgoing@example.com"));
    }

    @Test
    void projectCreatorCanAccessProject() {
        User employee = new User("Outgoing", "outgoing@example.com", "hash", "ROLE_OUTGOING");
        employee.setId(7L);
        Project project = new Project("Project", "Desc", "outgoing@example.com");
        project.setId(1L);

        when(userRepository.findByEmail("outgoing@example.com")).thenReturn(Optional.of(employee));
        when(memberRepository.findByProjectIdAndUserId(1L, 7L)).thenReturn(Optional.empty());
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertDoesNotThrow(() -> projectService.assertProjectAccess(1L, "outgoing@example.com"));
    }

    @Test
    void nonMemberIsDeniedAccess() {
        User outsider = new User("Outsider", "outsider@example.com", "hash", "ROLE_OUTGOING");
        outsider.setId(9L);
        Project project = new Project("Private Project", "Desc", "outgoing@example.com");
        project.setId(1L);

        when(userRepository.findByEmail("outsider@example.com")).thenReturn(Optional.of(outsider));
        when(memberRepository.findByProjectIdAndUserId(1L, 9L)).thenReturn(Optional.empty());
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(AccessDeniedException.class,
                () -> projectService.assertProjectAccess(1L, "outsider@example.com"));
    }

    @Test
    void unknownUserIsDeniedAccess() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> projectService.assertProjectAccess(1L, "ghost@example.com"));
    }
}
