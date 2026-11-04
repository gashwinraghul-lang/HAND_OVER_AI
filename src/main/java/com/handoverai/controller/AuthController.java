package com.handoverai.controller;

import com.handoverai.dto.AuthDTOs.FaceEnrollRequest;
import com.handoverai.dto.AuthDTOs.FaceVerifyRequest;
import com.handoverai.dto.AuthDTOs.LoginRequest;
import com.handoverai.dto.AuthDTOs.SignupRequest;
import com.handoverai.dto.AuthDTOs.UserResponse;
import com.handoverai.entity.AuditLog;
import com.handoverai.entity.User;
import com.handoverai.repository.AuditLogRepository;
import com.handoverai.repository.UserRepository;
import com.handoverai.security.FaceAuthService;
import com.handoverai.security.RateLimitingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RateLimitingService rateLimitingService;
    private final FaceAuthService faceAuthService;
    private final AuditLogRepository auditLogRepository;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          RateLimitingService rateLimitingService,
                          FaceAuthService faceAuthService,
                          AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.rateLimitingService = rateLimitingService;
        this.faceAuthService = faceAuthService;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/csrf")
    public ResponseEntity<?> csrf(CsrfToken csrfToken) {
        // Resolving CsrfToken here forces Spring Security to write the XSRF-TOKEN cookie,
        // so the login/signup pages can send it with their first POST.
        return ResponseEntity.ok(Map.of(
                "token", csrfToken.getToken(),
                "headerName", csrfToken.getHeaderName()
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < 12) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 12 characters long."));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already registered."));
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getName(), request.getEmail(), hashedPassword, "ROLE_OUTGOING");
        user.setEmailVerified(false);
        userRepository.save(user);

        // Simulate email verification link
        String verificationToken = UUID.randomUUID().toString();
        System.out.println("========================================");
        System.out.println("[EMAIL VERIFICATION] User: " + request.getEmail());
        System.out.println("[EMAIL VERIFICATION] Link: http://localhost:8080/api/auth/verify?token=" + verificationToken);
        System.out.println("========================================");

        // Auto-verify for demo purposes
        user.setEmailVerified(true);
        userRepository.save(user);

        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), false);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        // Rate limiting check
        if (rateLimitingService.isAccountLocked(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Account is temporarily locked due to too many failed attempts. Please try again later."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            // Reset failed attempts on successful login
            rateLimitingService.resetFailedAttempts(request.getEmail());

            // Store user info in session
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());

            // Audit log
            AuditLog log = new AuditLog(user.getId(), user.getName(), "LOGIN", "USER", String.valueOf(user.getId()), "Successful login");
            auditLogRepository.save(log);

            boolean hasFace = user.getFaceProfile() != null;
            UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), hasFace);
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            rateLimitingService.recordFailedAttempt(request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password."));
        }
    }

    @PostMapping("/face/enroll")
    public ResponseEntity<?> enrollFace(@RequestBody FaceEnrollRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated."));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        faceAuthService.enrollFace(user, request.getEmbedding(), request.isConsent());
        return ResponseEntity.ok(Map.of("message", "Face biometric enrolled successfully."));
    }

    @PostMapping("/face/verify")
    public ResponseEntity<?> verifyFace(@RequestBody FaceVerifyRequest request, HttpSession session) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found."));
        }

        // Face is a second factor: the password must also be correct.
        if (request.getPassword() == null ||
                !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("verified", false, "error", "Invalid email, password, or face."));
        }

        if (!user.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("verified", false, "error", "Email address is not verified."));
        }

        if (user.getFaceProfile() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No face profile is enrolled for this account. Sign in with a password and enrol your face in Profile."));
        }

        boolean matched = faceAuthService.verifyFace(user, request.getEmbedding());
        if (matched) {
            // Face verification is a login factor: establish an authenticated session.
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());

            auditLogRepository.save(new AuditLog(user.getId(), user.getName(), "FACE_LOGIN", "USER",
                    String.valueOf(user.getId()), "Successful face login"));

            boolean hasFace = user.getFaceProfile() != null;
            UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), hasFace);
            return ResponseEntity.ok(Map.of("verified", true, "user", userResponse));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("verified", false, "error", "Face verification failed."));
        }
    }

    @DeleteMapping("/face")
    public ResponseEntity<?> deleteFace(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated."));
        }
        faceAuthService.deleteFaceProfile(userId);
        return ResponseEntity.ok(Map.of("message", "Face data deleted successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        String email = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            email = auth.getName();
        }

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated."));
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasFace = user.getFaceProfile() != null;
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), hasFace);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }
}
