package com.handoverai.repository;

import com.handoverai.entity.SyncRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SyncRunRepository extends JpaRepository<SyncRun, Long> {
    List<SyncRun> findByConnectionIdOrderBySyncedAtDesc(Long connectionId);
}
