package com.gramevapp.web.service;

import com.gramevapp.web.model.IRunDto;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.repository.ExperimentRepository;
import com.gramevapp.web.repository.RunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("runService")
public class RunService {
    @Autowired
    RunRepository runRepository;

    @Autowired
    ExperimentRepository experimentRepository;


    public Run saveRun(Run run) {
        return runRepository.save(run);
    }

    public Run findByRunId(Long runId) {
        if (runId == null)
            return null;
        Optional<Run> check = runRepository.findById(runId);
        return check.orElse(null);
    }

    public void deleteRun(Run run) {
        runRepository.delete(run);
    }

    public List<IRunDto> findRunsByExpId(Long expId) {
        return runRepository.findRunDTOsByExpId(expId);
    }

    public void updateExecutionReport(Long runId, String report) {
        runRepository.updateExecutionReport(runId,report);
    }
}
