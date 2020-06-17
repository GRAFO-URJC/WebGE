package com.gramevapp.web.model;

public class RunDto {
    private Run.Status status;
    private Double bestIndividual;
    private int currentGeneration;
    private String modificationDateFormated;
    private String model;

    public RunDto(Run run) {
        this.status = run.getStatus();
        this.bestIndividual = run.getBestIndividual();
        this.currentGeneration = run.getCurrentGeneration();
        this.modificationDateFormated = run.getModificationDateFormated();
        this.model = run.getModel();

    }

    public Run.Status getStatus() {
        return status;
    }

    public void setStatus(Run.Status status) {
        this.status = status;
    }

    public Double getBestIndividual() {
        return bestIndividual;
    }

    public void setBestIndividual(Double bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    public int getCurrentGeneration() {
        return currentGeneration;
    }

    public void setCurrentGeneration(int currentGeneration) {
        this.currentGeneration = currentGeneration;
    }

    public String getModificationDateFormated() {
        return modificationDateFormated;
    }

    public void setModificationDateFormated(String modificationDateFormated) {
        this.modificationDateFormated = modificationDateFormated;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
