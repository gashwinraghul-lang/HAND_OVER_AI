package com.handoverai.repository;

import com.handoverai.entity.ReportApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportApprovalRepository extends JpaRepository<ReportApproval, Long> {
    List<ReportApproval> findByReportId(Long reportId);
}
