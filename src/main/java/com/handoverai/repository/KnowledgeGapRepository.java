package com.handoverai.repository;

import com.handoverai.entity.KnowledgeGap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KnowledgeGapRepository extends JpaRepository<KnowledgeGap, Long> {
    List<KnowledgeGap> findByProjectId(Long projectId);
}
