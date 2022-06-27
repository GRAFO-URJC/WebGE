package com.gramevapp.web.service.rabbitmq;

import com.engine.algorithm.RunnableExpGramEv;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueueRabbitMqMessage {
    private WebGERunnableUtils runnable;
    private Long expId, runId;
    private String code;

    public QueueRabbitMqMessage(
            @JsonProperty("runnable") WebGERunnableUtils runnable,
            @JsonProperty("expId") Long expId,
            @JsonProperty("runId") Long runId,
            @JsonProperty("code") String code) {
        this.runnable = runnable;
        this.expId = expId;
        this.runId = runId;
        this.code = code;
    }

    public void setRunnable(WebGERunnableUtils runnable) {
        this.runnable = runnable;
    }

    public void setExpId(Long expId) {
        this.expId = expId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public WebGERunnableUtils getRunnable() {
        return runnable;
    }

    public Long getExpId() {
        return expId;
    }

    public Long getRunId() {
        return runId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
