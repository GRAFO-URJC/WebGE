package com.gramevapp.web.service;

import com.engine.algorithm.CallableExpGramEv;

public class CallableExpGramEvWrapper {
    private CallableExpGramEv callable;
    private Long expId, runId;

    public CallableExpGramEvWrapper(CallableExpGramEv callable, Long expId, Long runId) {
        this.callable = callable;
        this.expId = expId;
        this.runId = runId;
    }

    public CallableExpGramEv getCallable() {
        return callable;
    }

    public Long getExpId() {
        return expId;
    }

    public Long getRunId() {
        return runId;
    }
}
