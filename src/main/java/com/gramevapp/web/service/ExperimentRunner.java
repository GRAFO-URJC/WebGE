package com.gramevapp.web.service;

import com.gramevapp.web.model.Run;

import java.util.List;

public interface ExperimentRunner {
    void accept(Run run, String propPath, int crossRunIdentifier, String objective, boolean de, Long expId);

    // legacy
    void accept(List<Thread> threadList, Run run, String propPath, int crossRunIdentifier, String objective, boolean de);
}
