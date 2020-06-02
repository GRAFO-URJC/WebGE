package com.gramevapp.web.repository;

import com.gramevapp.web.model.DiagramPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagramPairRepository extends JpaRepository<DiagramPair, Long> {
    List<DiagramPair> findByDiagramDataId(Long diagramId);
    /*@Modifying
    @Transactional
    // @Query("delete from Employee e where firstName = ?1")
    @Query(nativeQuery = true, value = "DELETE FROM test.diagram_pair_list WHERE diagram_data_id = ?1")
    void deleteListPair(Long diagramId);*/

    // void deleteAllByDiagramDataId(@Param("diagramId") Long diagramId);

    // @Query("DELETE FROM Tweetpost m WHERE m.createdAt < :date")

    // void deleteDiagramPairList(Long diagramId);
}
