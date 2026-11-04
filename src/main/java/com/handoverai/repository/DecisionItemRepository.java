package com.handoverai.repository;

import com.handoverai.entity.DecisionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DecisionItemRepository extends JpaRepository<DecisionItem, Long> {
    List<DecisionItem> findByProjectId(Long projectId);
}
