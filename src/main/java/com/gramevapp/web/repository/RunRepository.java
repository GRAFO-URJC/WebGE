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
    Run findRunById(Long id);
    Run findByRunName(String runName);
    List<Run> findAllByExperimentId(Experiment experimentId);
    // List<Run> findByUserId(User user);
    // Run findByUserIdAndId(User user, Long id);
    Run findTop1ByOrderByIdDesc();
    //List<Person> findByEmployedAndLastNameAndDob(boolean employed, String lastName, LocalDate dob);
    void deleteByDefaultGrammar(Grammar grammar);
    void deleteByDefaultExpDataType(ExperimentDataType experimentDataType);
}