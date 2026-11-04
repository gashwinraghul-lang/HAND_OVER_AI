package com.handoverai.repository;

import com.handoverai.entity.TaskItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {
    List<TaskItem> findByProjectId(Long projectId);
    List<TaskItem> findByProjectIdAndStatus(Long projectId, String status);
}
