package com.handoverai.repository;

import com.handoverai.entity.ExitInterviewNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExitInterviewNoteRepository extends JpaRepository<ExitInterviewNote, Long> {
    List<ExitInterviewNote> findByProjectId(Long projectId);
}
