package com.gramevapp.web.service;

import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.Grammar;
import com.gramevapp.web.model.Run;
import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.GrammarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
public class RecordAccessService {

    @Autowired
    private UserService userService;

    @Autowired
    private ExperimentService experimentService;

    @Autowired
    private RunService runService;

    @Autowired
    private GrammarRepository grammarRepository;

    public Dataset requireOwnedDataset(Long id) {
        Dataset dataset = id == null ? null : experimentService.findExperimentDataTypeById(id);
        if (dataset == null || !Objects.equals(dataset.getUserIdUserId(), currentUser().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return dataset;
    }

    public Experiment requireOwnedExperiment(Long id) {
        Experiment experiment = id == null ? null : experimentService.findExperimentById(id);
        if (experiment == null || !Objects.equals(experiment.getUserId(), currentUser().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return experiment;
    }

    public Run requireOwnedRun(Long id) {
        Run run = id == null ? null : runService.findByRunId(id);
        if (run == null || run.getExperimentId() == null
                || !Objects.equals(run.getExperimentId().getUserId(), currentUser().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return run;
    }

    public Grammar requireOwnedGrammar(Long id) {
        Grammar grammar = id == null ? null : grammarRepository.findGrammarByIdOrderByCreationDateDesc(id);
        if (grammar == null || !Objects.equals(grammar.getUserId(), currentUser().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return grammar;
    }

    private User currentUser() {
        User user = userService.getLoggedInUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return user;
    }
}
