package com.gramevapp.web.service.experimentrunnerservice;

import com.gramevapp.web.model.Run;

import java.util.List;
import java.util.concurrent.Future;

public interface ExperimentRunner {

    Future<Void> accept(Run run, String propPath, int crossRunIdentifier, String objective, boolean de, Long expId);

    // legacy
    void accept(List<Thread> threadList, Run run, String propPath, int crossRunIdentifier, String objective, boolean de);
}
