package com.gramevapp.web.service.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueueRabbitMqMessage {
    private WebGERunnableUtils runnable;
    private Long expId;
    private String code;

    public QueueRabbitMqMessage(
            @JsonProperty("runnable") WebGERunnableUtils runnable,
            @JsonProperty("expId") Long expId,
            @JsonProperty("code") String code) {
        this.runnable = runnable;
        this.expId = expId;
        this.code = code;
    }

    public void setRunnable(WebGERunnableUtils runnable) {
        this.runnable = runnable;
    }

    public void setExpId(Long expId) {
        this.expId = expId;
    }


    public WebGERunnableUtils getRunnable() {
        return runnable;
    }

    public Long getExpId() {
        return expId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
