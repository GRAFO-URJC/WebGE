package com.gramevapp.web.repository;

import com.gramevapp.web.model.DiagramData;
import com.gramevapp.web.model.DiagramPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagramPairRepository extends JpaRepository<DiagramPair, Long> {
    List<DiagramPair> findByDiagramDataId(Long diagramId);
    void deleteAllByDiagramDataId(DiagramData diagramId);
}
