package com.gramevapp.web.repository;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.ExperimentDataType;
import com.gramevapp.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.spi.LocaleNameProvider;

@Repository
public interface ExperimentDataTypeRepository extends JpaRepository<ExperimentDataType, Long> {
    ExperimentDataType findExperimentDataTypeByExperimentId(Long experimentId);

    List<ExperimentDataType> findAllByExperimentId(Experiment experimentId);

    List<ExperimentDataType> findAllByUserIdUserId(Long userId);
}
