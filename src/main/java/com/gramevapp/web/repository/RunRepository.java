package com.gramevapp.web.repository;

import com.gramevapp.web.model.IRunDto;
import com.gramevapp.web.model.Run;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RunRepository extends JpaRepository<Run, Long> {
    @Query(value = "select r.run_id as runId, r.current_generation as currentGeneration, r.model as model, r.status as status," +
            "r.best_individual as bestIndividual, r.ini_date as iniDateFormated, r.modification_date as modificationDateFormated " +
            "from webge.run r where r.experiment_id_experiment_id=?1",
            nativeQuery = true)
    List<IRunDto> findRunDTOsByExpId(Long expId);
}