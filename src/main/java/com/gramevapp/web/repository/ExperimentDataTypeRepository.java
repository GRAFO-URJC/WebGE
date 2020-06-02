package com.gramevapp.web.repository;

import com.gramevapp.web.model.ExperimentDataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperimentDataTypeRepository extends JpaRepository<ExperimentDataType, Long> {
    List<ExperimentDataType> findAllByUserIdUserId(Long userId);
}
