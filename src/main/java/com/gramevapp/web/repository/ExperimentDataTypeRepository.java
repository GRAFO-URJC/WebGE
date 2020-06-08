package com.gramevapp.web.repository;

import com.gramevapp.web.model.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperimentDataTypeRepository extends JpaRepository<Dataset, Long> {
    List<Dataset> findAllByUserIdUserId(Long userId);
}
