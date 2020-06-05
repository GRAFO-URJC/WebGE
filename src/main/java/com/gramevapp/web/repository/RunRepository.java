package com.gramevapp.web.repository;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunRepository extends JpaRepository<Run, Long> {
}