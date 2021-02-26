package com.gramevapp.web.model;

public interface IRunDto {
    Integer getRunId();
    Run.Status getStatus();
    Double getBestIndividual();
    Integer getCurrentGeneration();
    String getModificationDateFormated();
    String getIniDateFormated();
    String getModel();
}
