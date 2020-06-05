package com.gramevapp.web.service;

import com.gramevapp.web.model.ExpProperties;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.RunExecutionReport;
import com.gramevapp.web.repository.ExperimentRepository;
import com.gramevapp.web.repository.PropertiesRepository;
import com.gramevapp.web.repository.RunExecutionReportRepository;
import com.gramevapp.web.repository.RunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("runService")
public class RunService {
    @Autowired
    RunRepository runRepository;

    @Autowired
    ExperimentRepository experimentRepository;

    @Autowired
    PropertiesRepository expPropertiesRepository;

    @Autowired
    RunExecutionReportRepository runExecutionReportRepository;

    public Run saveRun(Run run){
        return runRepository.save(run);
    }

    public Run findByRunId(Long runId){
        if(runId==null)
            return null;
        return runRepository.findById(runId).get();
    }

    public void deleteRun(Run run){
        runRepository.delete(run);
    }

    public void deleteExpProperties(ExpProperties expProperties){
        expPropertiesRepository.delete(expProperties);
    }

    public RunExecutionReport getRunExecutionReport(Long id){
        return runExecutionReportRepository.findById(id).get();
    }

    public void saveRunExecutionReport(RunExecutionReport runExecutionReport){
        runExecutionReportRepository.save(runExecutionReport);
    }

    public void removeExecutionReport(RunExecutionReport runExecutionReport){
        runExecutionReportRepository.delete(runExecutionReport);
    }
}
