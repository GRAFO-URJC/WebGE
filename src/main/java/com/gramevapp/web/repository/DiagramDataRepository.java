package com.gramevapp.web.repository;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagramDataRepository extends JpaRepository<DiagramData, Long> {
    DiagramData findById(Long id);
    DiagramData findByRunId(Run runId);
}
