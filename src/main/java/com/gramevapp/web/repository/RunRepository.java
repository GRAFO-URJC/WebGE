package com.gramevapp.web.repository;

import com.gramevapp.web.model.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunRepository extends JpaRepository<Run, Long> {
}