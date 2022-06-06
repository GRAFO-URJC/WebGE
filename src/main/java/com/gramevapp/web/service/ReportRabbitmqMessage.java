package com.gramevapp.web.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gramevapp.web.model.Run;

public class ReportRabbitmqMessage {
    private Run run;
    private Exception exception;
    private String code;

    public ReportRabbitmqMessage(
            @JsonProperty("run") Run run,
            @JsonProperty("exception") Exception exception,
            @JsonProperty("code") String code) {
        this.run = run;
        this.exception = exception;
        this.code = code;
    }

    public Run getRun() {
        return this.run;
    }

    public Exception getException() {
        return this.exception;
    }

    public String getCode() {
        return code;
    }
}
