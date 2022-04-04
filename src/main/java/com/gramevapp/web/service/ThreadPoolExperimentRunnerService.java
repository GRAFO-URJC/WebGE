package com.gramevapp.web.service;

import com.gramevapp.web.model.Run;

import java.util.List;

public class ThreadPoolExperimentRunnerService implements ExperimentRunner{

    @Override
    public void accept(Run run, String propPath, int crossRunIdentifier, String objective, boolean de) {

    }

    @Override
    public void accept(List<Thread> threadList, Run run, String propPath, int crossRunIdentifier, String objective, boolean de) {

    }
}