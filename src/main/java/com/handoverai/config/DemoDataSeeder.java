package com.handoverai.config;

import com.handoverai.entity.*;
import com.handoverai.repository.*;
import com.handoverai.security.EncryptionUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TaskItemRepository taskRepository;
    private final DecisionItemRepository decisionRepository;
    private final RiskItemRepository riskRepository;
    private final KnowledgeGapRepository gapRepository;
    private final AccessItemRepository accessItemRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final OnboardingPlanRepository onboardingPlanRepository;
    private final ConnectionRepository connectionRepository;
    private final UserFaceProfileRepository faceProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionUtil encryptionUtil;

    public DemoDataSeeder(UserRepository userRepository,
                          ProjectRepository projectRepository,
                          ProjectMemberRepository memberRepository,
                          DocumentRepository documentRepository,
                          DocumentChunkRepository chunkRepository,
                          TaskItemRepository taskRepository,
                          DecisionItemRepository decisionRepository,
                          RiskItemRepository riskRepository,
                          KnowledgeGapRepository gapRepository,
                          AccessItemRepository accessItemRepository,
                          AnalysisResultRepository analysisResultRepository,
                          OnboardingPlanRepository onboardingPlanRepository,
                          ConnectionRepository connectionRepository,
                          UserFaceProfileRepository faceProfileRepository,
                          PasswordEncoder passwordEncoder,
                          EncryptionUtil encryptionUtil) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.taskRepository = taskRepository;
        this.decisionRepository = decisionRepository;
        this.riskRepository = riskRepository;
        this.gapRepository = gapRepository;
        this.accessItemRepository = accessItemRepository;
        this.analysisResultRepository = analysisResultRepository;
        this.onboardingPlanRepository = onboardingPlanRepository;
        this.connectionRepository = connectionRepository;
        this.faceProfileRepository = faceProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionUtil = encryptionUtil;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println("[DemoDataSeeder] Users already exist. Skipping seed.");
            ensureDemoFaceProfiles();
            return;
        }

        System.out.println("========================================");
        System.out.println("[DemoDataSeeder] Seeding demo data...");
        System.out.println("========================================");

        // --- 1. Create Demo Users ---
        User admin = new User("Admin User", "admin@handoverai.com",
                passwordEncoder.encode("Admin@123456789"), "ROLE_ADMIN");
        admin = userRepository.save(admin);

        User manager = new User("Sarah Manager", "manager@handoverai.com",
                passwordEncoder.encode("Manager@12345"), "ROLE_MANAGER");
        manager = userRepository.save(manager);

        User outgoing = new User("John Outgoing", "outgoing@handoverai.com",
                passwordEncoder.encode("Outgoing@12345"), "ROLE_OUTGOING");
        outgoing = userRepository.save(outgoing);

        User incoming = new User("Emily Incoming", "incoming@handoverai.com",
                passwordEncoder.encode("Incoming@12345"), "ROLE_INCOMING");
        incoming = userRepository.save(incoming);

        // --- 2. Create Demo Project ---
        Project project = new Project("VPS Infrastructure Renewal",
                "End-to-end handover for the VPS Infrastructure Renewal project including vendor contracts, access credentials, task tracking, and onboarding plans.",
                "outgoing@handoverai.com");
        project.setReadinessScore(78);
        project = projectRepository.save(project);

        // --- 3. Create ProjectMember Entries ---
        memberRepository.save(new ProjectMember(project, admin, "REVIEWING_MANAGER"));
        memberRepository.save(new ProjectMember(project, manager, "REVIEWING_MANAGER"));
        memberRepository.save(new ProjectMember(project, outgoing, "OUTGOING_OWNER"));
        memberRepository.save(new ProjectMember(project, incoming, "INCOMING_SUCCESSOR"));

        // --- 4. Create Documents with Chunks ---

        // Document 1: VPS Renewal SOP.pdf
        Document sopDoc = new Document(project, "VPS Renewal SOP.pdf", "PDF", "uploads/vps_renewal_sop.pdf", 245000, "outgoing@handoverai.com");
        sopDoc = documentRepository.save(sopDoc);

        chunkRepository.save(new DocumentChunk(sopDoc, 0, 1, null,
                "VPS Infrastructure Renewal Standard Operating Procedure. This document outlines the step-by-step process...",
                "VPS Renewal SOP.pdf, Page 1"));

        chunkRepository.save(new DocumentChunk(sopDoc, 1, 2, null,
                "Renewal Checklist: 1. Verify current service level agreements...",
                "VPS Renewal SOP.pdf, Page 2"));

        chunkRepository.save(new DocumentChunk(sopDoc, 2, 3, "Section 2.1",
                "2.1 Renewal Timeline and Payment. The VPS cluster renewal deadline is 30 June 2025. Payment is processed via GlobalPay automated recurring billing. Upon renewal, access credentials are shared with the infrastructure team.",
                "VPS Renewal SOP.pdf, Page 3, Section 2.1"));

        // Document 2: Team Meeting Notes.txt
        Document meetingDoc = new Document(project, "Team Meeting Notes.txt", "TXT", "uploads/team_meeting_notes.txt", 12000, "outgoing@handoverai.com");
        meetingDoc = documentRepository.save(meetingDoc);

        chunkRepository.save(new DocumentChunk(meetingDoc, 0, 1, null,
                "Team Meeting Notes - Infrastructure Planning. Date: May 2025. Attendees: John, Sarah, Emily. Discussion: The team agreed to push the VPS renewal deadline to 25 June 2025 to allow for pre-renewal security audit.",
                "Team Meeting Notes.txt, Page 1"));

        // --- 5. Create TaskItems ---
        taskRepository.save(new TaskItem(project,
                "Migrate production DB connection pool to HikariCP",
                "BLOCKED", null, LocalDate.of(2025, 6, 20),
                "https://github.com/company/vps-infra/issues/104",
                true, true));

        taskRepository.save(new TaskItem(project,
                "Update SSL certificates on edge load balancers",
                "COMPLETED", "outgoing.dev", LocalDate.of(2025, 6, 15),
                null, false, false));

        // --- 6. Create DecisionItem ---
        decisionRepository.save(new DecisionItem(project,
                "Adopted HikariCP over Apache DBCP2 for production connection pooling based on performance benchmarks",
                "John Outgoing", LocalDate.of(2025, 5, 15),
                "Team Meeting Notes.txt, Section 1"));

        // --- 7. Create RiskItem ---
        riskRepository.save(new RiskItem(project,
                "No renewal receipt uploaded — unable to verify payment completion before handover",
                "HIGH",
                "Upload vendor renewal receipt PDF before June 28",
                "AI Analysis - Missing Evidence"));

        // --- 8. Create KnowledgeGap ---
        gapRepository.save(new KnowledgeGap(project,
                "Vendor renewal receipt and payment confirmation not yet uploaded",
                "PENDING",
                "Finance department or billing email"));

        // --- 9. Create AccessItems ---
        accessItemRepository.save(new AccessItem(project,
                "GlobalPay Billing Portal", "acct_***924", "Admin", "PENDING_REVOKE"));

        accessItemRepository.save(new AccessItem(project,
                "AWS VPS Console", "iam_***admin", "PowerUser", "PENDING_REVOKE"));

        // --- 10. Create AnalysisResult ---
        AnalysisResult analysis = new AnalysisResult(project,
                1,  // completedCount
                0,  // wipCount
                1,  // blockedCount
                1,  // unownedCount
                1,  // overdueCount
                "Date discrepancy detected: SOP states renewal deadline as 30 June 2025, but Team Meeting Notes indicate the team agreed to push deadline to 25 June 2025.",
                "VPS Infrastructure Renewal is 78% ready for handover. 1 of 2 tasks completed. 1 blocked task requires attention. Missing vendor renewal receipt flagged as HIGH risk.");
        analysisResultRepository.save(analysis);

        // --- 11. Create OnboardingPlans ---
        onboardingPlanRepository.save(new OnboardingPlan(project,
                "DAY_30", "Orientation & System Access",
                "Complete system access provisioning, review all handover documentation, shadow outgoing employee on critical processes, and verify all credentials work correctly.",
                null));

        onboardingPlanRepository.save(new OnboardingPlan(project,
                "DAY_60", "Independent Operations",
                "Begin independent management of VPS infrastructure, handle first renewal cycle independently with manager oversight, document any gaps found during transition.",
                null));

        onboardingPlanRepository.save(new OnboardingPlan(project,
                "DAY_90", "Full Ownership & Knowledge Transfer Complete",
                "Full ownership of all VPS infrastructure responsibilities, complete knowledge transfer sign-off, update all documentation with new contact information, final review with manager.",
                null));

        // --- 12. Create demo Connections ---
        String encryptedGithubKey = encryptionUtil.encrypt("ghp_demo1234567890");
        String githubLast4 = "7890";
        connectionRepository.save(new Connection(project, "GITHUB", encryptedGithubKey, githubLast4, "repo,issues"));

        String encryptedTrelloKey = encryptionUtil.encrypt("trello_demo_key1234");
        String trelloLast4 = "1234";
        connectionRepository.save(new Connection(project, "TRELLO", encryptedTrelloKey, trelloLast4, "read,write"));

        // --- 13. Demo face profiles (synthetic placeholder for no-camera demos) ---
        ensureDemoFaceProfiles();

        // --- Log Demo Credentials ---
        System.out.println("========================================");
        System.out.println("[DemoDataSeeder] DEMO DATA SEEDED SUCCESSFULLY");
        System.out.println("========================================");
        System.out.println("Demo Credentials:");
        System.out.println("  1. admin@handoverai.com    / Admin@123456789   (ROLE_ADMIN)");
        System.out.println("  2. manager@handoverai.com  / Manager@12345     (ROLE_MANAGER)");
        System.out.println("  3. outgoing@handoverai.com / Outgoing@12345    (ROLE_OUTGOING)");
        System.out.println("  4. incoming@handoverai.com / Incoming@12345    (ROLE_INCOMING)");
        System.out.println("========================================");
        System.out.println("Demo Project: VPS Infrastructure Renewal (ID: " + project.getId() + ")");
        System.out.println("  Readiness Score: 78%");
        System.out.println("  Documents: 2, Tasks: 2, Decisions: 1, Risks: 1");
        System.out.println("  Connectors: GitHub, Trello");
        System.out.println("========================================");
    }

    /**
     * Demo accounts get a synthetic 128-dimension face profile so the face-login
     * flow can be demonstrated without a camera. Real enrolment from Profile
     * replaces it with a genuine face-api descriptor (averaged over 3 frames).
     */
    private void ensureDemoFaceProfiles() {
        List<String> demoEmails = List.of(
                "admin@handoverai.com",
                "manager@handoverai.com",
                "outgoing@handoverai.com",
                "incoming@handoverai.com"
        );
        String zeroVectorJson = "[" + String.join(",", Collections.nCopies(128, "0.0")) + "]";

        for (String email : demoEmails) {
            userRepository.findByEmail(email).ifPresent(user -> {
                if (faceProfileRepository.findByUserId(user.getId()).isEmpty()) {
                    UserFaceProfile profile = new UserFaceProfile(
                            user,
                            encryptionUtil.encrypt(zeroVectorJson),
                            LocalDateTime.now()
                    );
                    faceProfileRepository.save(profile);
                    System.out.println("[DemoDataSeeder] Seeded synthetic demo face profile for " + email);
                }
            });
        }
    }
}
