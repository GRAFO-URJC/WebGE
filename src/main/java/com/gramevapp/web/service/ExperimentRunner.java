package com.gramevapp.web.service;

import com.gramevapp.web.model.Run;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface ExperimentRunner {
    Future<?> accept(ExecutorService tPool, Run run, String propPath, int crossRunIdentifier, String objective, boolean de, Long expId);

    // legacy
    void accept(List<Thread> threadList, Run run, String propPath, int crossRunIdentifier, String objective, boolean de);
}
