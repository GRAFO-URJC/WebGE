package com.gramevapp.web.service.rabbitmq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gramevapp.web.model.Dataset;
import com.gramevapp.web.model.Run;

import java.util.Properties;

public class WebGERunnableUtils {
    private Properties properties;
    private Run runElement;
    private Dataset experimentDataType;
    private int crossRunIdentifier;
    private String objective;
    private boolean de;

    public WebGERunnableUtils(
            @JsonProperty("properties")Properties properties,
            @JsonProperty("runElement")Run runElement,
            @JsonProperty("experimentDataType") Dataset experimentDataType,
            @JsonProperty("crossRunIdentifier")int crossRunIdentifier,
            @JsonProperty("objective")String objective,
            @JsonProperty("de") boolean de) {
        this.properties = properties;
        this.runElement = runElement;
        this.experimentDataType = experimentDataType;
        this.crossRunIdentifier = crossRunIdentifier;
        this.objective = objective;
        this.de = de;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Run getRunElement() {
        return runElement;
    }

    public void setRunElement(Run runElement) {
        this.runElement = runElement;
    }

    public Dataset getExperimentDataType() {
        return experimentDataType;
    }

    public void setExperimentDataType(Dataset experimentDataType) {
        this.experimentDataType = experimentDataType;
    }

    public int getCrossRunIdentifier() {
        return crossRunIdentifier;
    }

    public void setCrossRunIdentifier(int crossRunIdentifier) {
        this.crossRunIdentifier = crossRunIdentifier;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public boolean getDe() {
        return de;
    }

    public void setDe(boolean de) {
        this.de = de;
    }
}
