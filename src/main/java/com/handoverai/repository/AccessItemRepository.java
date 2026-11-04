package com.handoverai.repository;

import com.handoverai.entity.AccessItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccessItemRepository extends JpaRepository<AccessItem, Long> {
    List<AccessItem> findByProjectId(Long projectId);
}
