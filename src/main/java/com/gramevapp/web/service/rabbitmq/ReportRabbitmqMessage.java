package com.gramevapp.web.service.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReportRabbitmqMessage {
    private Long runId;
    private Exception exception;
    private String code;

    public ReportRabbitmqMessage(
            @JsonProperty("runId") Long runId,
            @JsonProperty("exception") Exception exception,
            @JsonProperty("code") String code) {
        this.runId = runId;
        this.exception = exception;
        this.code = code;
    }

    public Long getRunId() {
        return this.runId;
    }

    public Exception getException() {
        return this.exception;
    }

    public String getCode() {
        return code;
    }
}
