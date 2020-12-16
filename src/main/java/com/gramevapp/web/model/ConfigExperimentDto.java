package com.gramevapp.web.model;

import javax.validation.constraints.*;

public class ConfigExperimentDto {
    private Long id;
    private static final String EXPERIMENTNAME_PATTERN = "^[a-zA-Z0-9 \\[\\]()#_-]{1,254}$";

    @Pattern(regexp = EXPERIMENTNAME_PATTERN, message = "Experiment name cannot have special characters. Only alphanumeric ones and []()#_- are allowed.")
    private String experimentName = "";
    private String experimentDescription = "";
    @Min(value = 100)
    @Max(value = 100000)
    @NotNull
    private Integer generations = 1000;
    @Min(value = 0)
    @Max(value = 100000)
    @NotNull
    private Integer populationSize = 50;
    @Min(value = 0)
    @Max(value = 1000)
    @NotNull
    private Integer maxWraps = 3;
    @Min(value = 0)
    @Max(value = 1000)
    @NotNull
    private Integer tournament = 2;
    @Min(value = 0)
    @Max(value = 100)
    @NotNull
    private Double crossoverProb = 0.7;
    @Min(value = 0)
    @Max(value = 100)
    @NotNull
    private Double mutationProb = 0.1;
    @NotNull(message = "Results cannot be empty")
    @Size(min = 1)
    private String results = " ";             // Text file with the results of the experiments
    @Min(value = 0)
    @Max(value = 100)
    @NotNull
    private Integer numCodons = 10;
    @Min(value = 0)
    @Max(value = 100)
    @NotNull
    private Integer numberRuns = 1;
    private Long defaultExpDataTypeId;
    private Long testDefaultExpDataTypeId;
    @NotNull(message = "Grammar area text cannot be empty")
    @Size(min = 1)
    private String fileText = ""; // This is the text on the file - That's written in an areaText - So we can take it as a String
    private String dataTypeType = "training";   // Validation, test, training
    private String crossExperiment = "false";
    private boolean contentFold = false;

    /**
     * 0 -> Root Mean Squared Error (RMSE)
     * 1 -> Clarke Error Grid (CEG)
     * 2 -> Bi-objective: RMSE & CEG
     * 3 -> R Square (R^2)
     * 4 -> Absolute Error (Abs. Error)
     * 5 -> Mean Absolute Relative Deviation (MARD)
     */
    @NotNull
    @Size(min = 1)
    private String objective = "";

    private boolean de = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDefaultExpDataTypeId() {
        return defaultExpDataTypeId;
    }

    public void setDefaultExpDataTypeId(Long defaultExpDataTypeId) {
        this.defaultExpDataTypeId = defaultExpDataTypeId;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
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

    public Integer getGenerations() {
        return generations;
    }

    public void setGenerations(Integer generations) {
        this.generations = generations;
    }

    public Integer getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(Integer populationSize) {
        this.populationSize = populationSize;
    }

    public Integer getMaxWraps() {
        return maxWraps;
    }

    public void setMaxWraps(Integer maxWraps) {
        this.maxWraps = maxWraps;
    }

    public Integer getTournament() {
        return tournament;
    }

    public void setTournament(Integer tournament) {
        this.tournament = tournament;
    }

    public Double getCrossoverProb() {
        return crossoverProb;
    }

    public void setCrossoverProb(Double crossoverProb) {
        this.crossoverProb = crossoverProb;
    }

    public Double getMutationProb() {
        return mutationProb;
    }

    public void setMutationProb(Double mutationProb) {
        this.mutationProb = mutationProb;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public Integer getNumCodons() {
        return numCodons;
    }

    public void setNumCodons(Integer numCodons) {
        this.numCodons = numCodons;
    }

    public Integer getNumberRuns() {
        return numberRuns;
    }

    public void setNumberRuns(Integer numberRuns) {
        this.numberRuns = numberRuns;
    }


    public String getDataTypeType() {
        return dataTypeType;
    }

    public void setDataTypeType(String dataTypeType) {
        this.dataTypeType = dataTypeType;
    }

    public Long getTestDefaultExpDataTypeId() {
        return testDefaultExpDataTypeId;
    }

    public void setTestDefaultExpDataTypeId(Long testDefaultExpDataTypeId) {
        this.testDefaultExpDataTypeId = testDefaultExpDataTypeId;
    }

    public String getFileText() {
        return fileText;
    }

    public void setFileText(String fileText) {
        this.fileText = fileText;
    }

    public String getCrossExperiment() {
        return crossExperiment;
    }

    public void setCrossExperiment(String crossExperiment) {
        this.crossExperiment = crossExperiment;
    }

    public boolean isContentFold() {
        return contentFold;
    }

    public void setContentFold(boolean contentFold) {
        this.contentFold = contentFold;
    }

    public boolean isEmpty(String objective) {
        return objective.equals("");
    }

    public boolean isDe() {
        return de;
    }
    public void setDe(boolean de) {
       this.de = de;
    }

}