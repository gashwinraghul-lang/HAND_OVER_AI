package com.handoverai.service;

import com.handoverai.entity.*;
import com.handoverai.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final TaskItemRepository taskRepository;
    private final DocumentRepository documentRepository;
    private final RiskItemRepository riskRepository;
    private final DecisionItemRepository decisionRepository;
    private final KnowledgeGapRepository gapRepository;
    private final AnalysisResultRepository analysisRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository memberRepository,
                          TaskItemRepository taskRepository,
                          DocumentRepository documentRepository,
                          RiskItemRepository riskRepository,
                          DecisionItemRepository decisionRepository,
                          KnowledgeGapRepository gapRepository,
                          AnalysisResultRepository analysisRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.taskRepository = taskRepository;
        this.documentRepository = documentRepository;
        this.riskRepository = riskRepository;
        this.decisionRepository = decisionRepository;
        this.gapRepository = gapRepository;
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /** Projects the signed-in user is allowed to see. Admins see everything. */
    public List<Project> getProjectsForUser(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return List.of();
        User user = userOpt.get();
        if ("ROLE_ADMIN".equals(user.getRole())) return projectRepository.findAll();

        Set<Long> memberProjectIds = memberRepository.findByUserId(user.getId()).stream()
                .map(m -> m.getProject().getId())
                .collect(Collectors.toSet());

        return projectRepository.findAll().stream()
                .filter(p -> memberProjectIds.contains(p.getId()) || email.equalsIgnoreCase(p.getCreatedBy()))
                .toList();
    }

    public void assertProjectAccess(Long projectId, String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) throw new AccessDeniedException("Not authorised for this project.");
        User user = userOpt.get();
        if ("ROLE_ADMIN".equals(user.getRole())) return;

        boolean member = memberRepository.findByProjectIdAndUserId(projectId, user.getId()).isPresent();
        boolean creator = projectRepository.findById(projectId)
                .map(p -> email.equalsIgnoreCase(p.getCreatedBy()))
                .orElse(false);
        if (!member && !creator) throw new AccessDeniedException("Not authorised for this project.");
    }

    public boolean isIncomingUser(String email) {
        return userRepository.findByEmail(email)
                .map(u -> "ROLE_INCOMING".equals(u.getRole()))
                .orElse(false);
    }

    /**
     * Incoming employees only see the approved handover and onboarding plan.
     * They must not read raw source documents, tasks, connections or chat data.
     */
    public void assertWorkspaceAccess(Long projectId, String email) {
        assertProjectAccess(projectId, email);
        if (isIncomingUser(email)) {
            throw new AccessDeniedException("Incoming employees can only view the approved handover and onboarding plan.");
        }
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    @Transactional
    public Project createProject(String name, String description, String createdBy) {
        Project project = new Project(name, description, createdBy);
        project.setReadinessScore(78);
        project = projectRepository.save(project);

        // Give the creator a project-level role so they can keep access to it.
        User creator = userRepository.findByEmail(createdBy).orElse(null);
        if (creator != null) {
            String roleInProject = switch (creator.getRole()) {
                case "ROLE_INCOMING" -> "INCOMING_SUCCESSOR";
                case "ROLE_OUTGOING" -> "OUTGOING_OWNER";
                default -> "REVIEWING_MANAGER";
            };
            memberRepository.save(new ProjectMember(project, creator, roleInProject));
        }
        return project;
    }

    public Map<String, Object> calculateReadiness(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        List<TaskItem> tasks = taskRepository.findByProjectId(projectId);
        List<KnowledgeGap> gaps = gapRepository.findByProjectId(projectId);
        List<RiskItem> risks = riskRepository.findByProjectId(projectId);

        // Core score calculation
        int totalItems = tasks.size() + gaps.size() + risks.size();
        int score = project.getReadinessScore();
        if (score == 0 && totalItems > 0) {
            long resolvedTasks = tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
            score = (int) Math.round(((double) resolvedTasks / Math.max(1, tasks.size())) * 100);
        }
        if (score == 0) score = 78; // Default demo score

        int missingPercentage = 100 - score;
        int itemsNeedingAttention = (int) tasks.stream().filter(t -> t.isOverdue() || t.isUnowned()).count() + gaps.size();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("projectId", projectId);
        metrics.put("projectName", project.getName());
        metrics.put("readinessScore", score);
        metrics.put("missingPercentage", missingPercentage);
        metrics.put("itemsNeedingAttention", Math.max(itemsNeedingAttention, 3));
        metrics.put("openRisksCount", risks.size());
        metrics.put("tasksCount", tasks.size());
        metrics.put("status", project.getStatus());

        return metrics;
    }

    public Map<String, Object> getProjectDetails(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        Map<String, Object> result = new HashMap<>();
        result.put("project", project);
        result.put("readiness", calculateReadiness(projectId));

        if (isIncomingUser(email)) {
            // Role-scoped view: no raw documents, tasks, decisions or risks.
            result.put("tasks", List.of());
            result.put("documents", List.of());
            result.put("decisions", List.of());
            result.put("risks", List.of());
            result.put("knowledgeGaps", List.of());
            result.put("latestAnalysis", null);
            return result;
        }

        result.put("tasks", taskRepository.findByProjectId(projectId));
        result.put("documents", documentRepository.findByProjectId(projectId));
        result.put("decisions", decisionRepository.findByProjectId(projectId));
        result.put("risks", riskRepository.findByProjectId(projectId));
        result.put("knowledgeGaps", gapRepository.findByProjectId(projectId));
        result.put("latestAnalysis", analysisRepository.findTopByProjectIdOrderByAnalyzedAtDesc(projectId).orElse(null));

        return result;
    }
}
