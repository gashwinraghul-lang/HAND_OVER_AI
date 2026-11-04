package com.handoverai.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handoverai.entity.User;
import com.handoverai.entity.UserFaceProfile;
import com.handoverai.repository.UserFaceProfileRepository;
import com.handoverai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FaceAuthService {

    private final UserFaceProfileRepository faceProfileRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Standard face-api Euclidean distance threshold for match
    private static final double MATCH_THRESHOLD = 0.60;

    public FaceAuthService(UserFaceProfileRepository faceProfileRepository,
                           UserRepository userRepository,
                           EncryptionUtil encryptionUtil) {
        this.faceProfileRepository = faceProfileRepository;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
    }

    @Transactional
    public void enrollFace(User user, List<Double> embeddingVector, boolean consent) {
        if (!consent) {
            throw new IllegalArgumentException("Explicit consent is required to enroll biometric face data.");
        }
        if (embeddingVector == null || embeddingVector.size() < 16) {
            throw new IllegalArgumentException("Invalid face embedding vector dimension.");
        }

        try {
            String json = objectMapper.writeValueAsString(embeddingVector);
            String encrypted = encryptionUtil.encrypt(json);

            Optional<UserFaceProfile> existing = faceProfileRepository.findByUserId(user.getId());
            UserFaceProfile profile = existing.orElseGet(UserFaceProfile::new);
            profile.setUser(user);
            profile.setEncryptedEmbedding(encrypted);
            profile.setConsentGivenAt(LocalDateTime.now());
            profile.setEnrolledAt(LocalDateTime.now());

            faceProfileRepository.save(profile);
        } catch (Exception e) {
            throw new RuntimeException("Error enrolling face profile: " + e.getMessage(), e);
        }
    }

    public boolean verifyFace(User user, List<Double> incomingVector) {
        if (incomingVector == null || incomingVector.isEmpty()) return false;
        Optional<UserFaceProfile> profileOpt = faceProfileRepository.findByUserId(user.getId());
        if (profileOpt.isEmpty()) return false;

        try {
            String decryptedJson = encryptionUtil.decrypt(profileOpt.get().getEncryptedEmbedding());
            List<Double> storedVector = objectMapper.readValue(decryptedJson, new TypeReference<List<Double>>() {});

            if (storedVector.size() != incomingVector.size()) return false;

            double sumSquaredDiff = 0.0;
            for (int i = 0; i < storedVector.size(); i++) {
                double diff = storedVector.get(i) - incomingVector.get(i);
                sumSquaredDiff += diff * diff;
            }
            double distance = Math.sqrt(sumSquaredDiff);
            return distance <= MATCH_THRESHOLD;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void deleteFaceProfile(Long userId) {
        faceProfileRepository.deleteByUserId(userId);
    }
}
