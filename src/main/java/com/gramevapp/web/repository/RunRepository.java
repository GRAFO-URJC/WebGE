package com.gramevapp.web.repository;

import com.gramevapp.web.model.*;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Repository
public interface RunRepository extends JpaRepository<Run, Long> {
    Run findById(Long id);
    List<Run> findAllByExperimentId(Experiment experimentId);
    Run findTop1ByOrderByIdDesc();
    void deleteByDefaultGrammar(Grammar grammar);
    void deleteByDefaultExpDataType(ExperimentDataType experimentDataType);
}