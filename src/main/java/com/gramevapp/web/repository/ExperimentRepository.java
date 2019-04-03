package com.gramevapp.web.repository;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.Grammar;
import com.gramevapp.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {
    Experiment findByUserIdAndId(User user, Long id);
    List<Experiment> findByUserId(User user);
    void deleteByUserIdAndId(User user, Long id);
    void deleteById(Long id);
}