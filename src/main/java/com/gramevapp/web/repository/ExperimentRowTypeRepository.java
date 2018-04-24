package com.gramevapp.web.repository;

import com.gramevapp.web.model.ExperimentRowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentRowTypeRepository extends JpaRepository<ExperimentRowType, Long> {
    ExperimentRowType findById(Long id);    // Id of the row
    ExperimentRowType save(ExperimentRowType expRowType);
}
