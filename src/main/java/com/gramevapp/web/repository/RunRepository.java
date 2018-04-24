package com.gramevapp.web.repository;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;

public interface RunRepository extends JpaRepository<Run, Long>, QueryByExampleExecutor<Run> {
    Run findById(Long id);
    List<Run> findAllByExperimentId(Experiment experimentId);
    List<Run> findByUserId(User user);
    Run findByUserIdAndId(User user, Long id);
    //List<Person> findByEmployedAndLastNameAndDob(boolean employed, String lastName, LocalDate dob);
}
