package com.handoverai.repository;

import com.handoverai.entity.UserFaceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserFaceProfileRepository extends JpaRepository<UserFaceProfile, Long> {
    Optional<UserFaceProfile> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
