package com.gramevapp.web.service.ExperimentRunnerService;

import com.gramevapp.web.model.Run;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface ExperimentRunner {

    Future<Void> accept(Run run, String propPath, int crossRunIdentifier, String objective, boolean de, Long expId);

    // legacy
    void accept(List<Thread> threadList, Run run, String propPath, int crossRunIdentifier, String objective, boolean de);
}
