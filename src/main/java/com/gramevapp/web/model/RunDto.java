package com.gramevapp.web.model;

public class RunDto {
    private Long id;
    private Long idExperiment;
    private String status;
    private String iniDate;
    private String lastDate;
    private String runName;
    private String runDescription;

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

    public String getRunName() {
        return runName;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public String getRunDescription() {
        return runDescription;
    }

    public void setRunDescription(String runDescription) {
        this.runDescription = runDescription;
    }
}
