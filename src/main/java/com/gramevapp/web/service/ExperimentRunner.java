package com.gramevapp.web.service;

import com.gramevapp.web.model.Run;

public interface ExperimentRunner {
    void accept(Run run, String propPath, int crossRunIdentifier, String objective, boolean de);
}
