package com.handoverai.security;

import com.handoverai.entity.User;
import com.handoverai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RateLimitingService {

    private final UserRepository userRepository;
    private final int maxAttempts;
    private final int lockoutMinutes;

    public RateLimitingService(UserRepository userRepository,
                               @Value("${handoverai.security.max-failed-attempts:5}") int maxAttempts,
                               @Value("${handoverai.security.lockout-minutes:15}") int lockoutMinutes) {
        this.userRepository = userRepository;
        this.maxAttempts = maxAttempts;
        this.lockoutMinutes = lockoutMinutes;
    }

    public boolean isAccountLocked(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        if (user.getLockedUntil() != null) {
            if (user.getLockedUntil().isAfter(LocalDateTime.now())) {
                return true;
            } else {
                // Lock has expired; reset
                user.setLockedUntil(null);
                user.setFailedAttempts(0);
                userRepository.save(user);
                return false;
            }
        }
        return false;
    }

    public void recordFailedAttempt(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= maxAttempts) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutMinutes));
            }
            userRepository.save(user);
        }
    }

    public void resetFailedAttempts(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }
}
