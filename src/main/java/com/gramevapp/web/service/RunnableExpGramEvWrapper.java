package com.gramevapp.web.service;

import com.engine.algorithm.RunnableExpGramEv;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gramevapp.web.model.Run;

public class RunnableExpGramEvWrapper {
    private RunnableExpGramEv runnable;
    private Long expId, runId;

    public RunnableExpGramEvWrapper(
            @JsonProperty("runnable") RunnableExpGramEv runnable,
            @JsonProperty("expId") Long expId,
            @JsonProperty("runId") Long runId) {
        this.runnable = runnable;
        this.expId = expId;
        this.runId = runId;
    }

    public void setRunnable(RunnableExpGramEv runnable) {
        this.runnable = runnable;
    }

    public void setExpId(Long expId) {
        this.expId = expId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public RunnableExpGramEv getRunnable() {
        return runnable;
    }

    public Long getExpId() {
        return expId;
    }

    public Long getRunId() {
        return runId;
    }
}
