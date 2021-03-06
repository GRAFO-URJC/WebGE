package com.gramevapp.web.model;

public class DiagramDataDto {
    private Integer currentGeneration;
    private Double bestIndividual;
    private Boolean finished;
    private Boolean stopped;
    private Boolean failed;
    private String runExecutionReport;

    public DiagramDataDto(DiagramData diagramData) {
        this.currentGeneration = diagramData.getCurrentGeneration();
        this.bestIndividual = diagramData.getBestIndividual();
        this.finished = diagramData.getFinished();
        this.stopped = diagramData.getStopped();
        this.failed = diagramData.getFailed();
        this.runExecutionReport = diagramData.getRunId().getExecReport();
    }

    public Integer getCurrentGeneration() {
        return currentGeneration;
    }

    public void setCurrentGeneration(Integer currentGeneration) {
        this.currentGeneration = currentGeneration;
    }

    public Double getBestIndividual() {
        return bestIndividual;
    }

    public void setBestIndividual(Double bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Boolean getStopped() {
        return stopped;
    }

    public void setStopped(Boolean stopped) {
        this.stopped = stopped;
    }

    public Boolean getFailed() {
        return failed;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
    }

    public String getRunExecutionReport() {
        return runExecutionReport;
    }

    public void setRunExecutionReport(String runExecutionReport) {
        this.runExecutionReport = runExecutionReport;
    }
}
