package com.gramevapp.web.repository;

import com.gramevapp.web.model.ExperimentDataType;
import com.gramevapp.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentDataTypeRepository extends JpaRepository<ExperimentDataType, Long> {
    ExperimentDataType findById(Long id);
    ExperimentDataType findByUserIdAndDataTypeName(User user, String name);
}
