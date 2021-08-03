package com.gramevapp.web.repository;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    Experiment findByUserIdAndId(User user, Long id);

    List<Experiment> findByUserIdOrderByCreationDateDesc(Long userId);

    void deleteById(Long id);

    @Query(value = "SELECT * FROM webge.experiment e WHERE e.user_id=?1 ORDER BY creation_date DESC", nativeQuery = true)
    List<Experiment> findByUserIdOrderByCreationDateDescOptimized(Long id);

    @Query(value = "SELECT * FROM webge.experiment e WHERE e.user_id=?1 ORDER BY update_date DESC LIMIT 1", nativeQuery = true)
    Experiment findByUserIdMostRecentExperimentOptimized(Long id);
}