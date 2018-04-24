package com.gramevapp.web.service;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.ExperimentRepository;
import com.gramevapp.web.repository.RunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class  RunService {
    @Autowired
    RunRepository runRepository;

    @Autowired
    ExperimentRepository experimentRepository;

    public Run saveRun(Run run){
        return runRepository.save(run);
    }

    public List<Run> findAllByExperiment(Experiment exp){
        return runRepository.findAllByExperimentId(exp);
    }

    public List<Run> findAllByUserEmail(User user){
        return runRepository.findByUserId(user);
    }

    public Run findByUserIdAndId(User user, Long id){
        return  runRepository.findByUserIdAndId(user, id);
    }
}
