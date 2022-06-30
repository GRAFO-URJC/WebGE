package com.gramevapp.web.service.rabbitmq;

import com.engine.algorithm.CallableExpGramEv;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
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