package com.gramevapp.web.repository;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.ExperimentDataType;
import com.gramevapp.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    Experiment findById(Long id);
    Experiment findByUserIdAndId(User user, Long id);
    Experiment findByUserIdAndExperimentName(User user, String name);
    List<Experiment> findByUserId(User user);
}
