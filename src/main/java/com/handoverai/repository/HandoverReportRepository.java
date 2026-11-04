package com.handoverai.repository;

import com.handoverai.entity.HandoverReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HandoverReportRepository extends JpaRepository<HandoverReport, Long> {
    List<HandoverReport> findByProjectIdOrderByGeneratedAtDesc(Long projectId);
    Optional<HandoverReport> findTopByProjectIdAndStatusOrderByGeneratedAtDesc(Long projectId, String status);
}
