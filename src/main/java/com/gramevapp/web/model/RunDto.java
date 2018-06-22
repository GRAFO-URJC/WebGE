package com.gramevapp.web.model;

public class RunDto {
    private Long id;
    private Long idExperiment;
    private String status;
    private String iniDate;
    private String lastDate;
    private String experimentName;
    private String experimentDescription;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIniDate() {
        return iniDate;
    }

    public void setIniDate(String iniDate) {
        this.iniDate = iniDate;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public Long getIdExperiment() {
        return idExperiment;
    }

    public void setIdExperiment(Long idExperiment) {
        this.idExperiment = idExperiment;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getExperimentDescription() {
        return experimentDescription;
    }

    public void setExperimentDescription(String experimentDescription) {
        this.experimentDescription = experimentDescription;
    }
}
