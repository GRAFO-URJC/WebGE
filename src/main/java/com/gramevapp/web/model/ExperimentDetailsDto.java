package com.gramevapp.web.model;

public class ExperimentDetailsDto {

    private Long experimentId;
    private Long runId;
    private Long diagramDataId;

    private String experimentName;
    private String experimentDescription;
    private Integer generations = 1000;
    private Integer populationSize = 100;
    private Integer maxWraps = 3;
    private Integer tournament = 2;
    private Double crossoverProb = 0.5;
    private Double mutationProb = 0.5;
    private String initialization = "";      // Random OR Sensible
    private String results = "";             // Text file with the results of the experiments
    private Integer numCodons = 100;
    private Integer numberRuns = 1;
    private String defaultGrammar = "";     // Text area with the grammar
    private String defaultExpDataType = ""; // Name of experiment data type file

    private Run.Status status;
    private String iniDate;
    private String lastDate;
    private String runName;
    private String runDescription;

    private Double bestIndividual;
    private Integer currentGeneration;

    public ExperimentDetailsDto() {
    }

    public ExperimentDetailsDto(Long experimentId, Long runId, Long diagramDataId, String experimentName, String experimentDescription, Integer generations, Integer populationSize, Integer maxWraps, Integer tournament, Double crossoverProb, Double mutationProb, String initialization, String results, Integer numCodons, Integer numberRuns, String defaultGrammar, String defaultExpDataType, Run.Status status, String iniDate, String lastDate, String runName, String runDescription, Double bestIndividual, Integer currentGeneration) {
        this.experimentId = experimentId;
        this.runId = runId;
        this.diagramDataId = diagramDataId;
        this.experimentName = experimentName;
        this.experimentDescription = experimentDescription;
        this.generations = generations;
        this.populationSize = populationSize;
        this.maxWraps = maxWraps;
        this.tournament = tournament;
        this.crossoverProb = crossoverProb;
        this.mutationProb = mutationProb;
        this.initialization = initialization;
        this.results = results;
        this.numCodons = numCodons;
        this.numberRuns = numberRuns;
        this.defaultGrammar = defaultGrammar;
        this.defaultExpDataType = defaultExpDataType;
        this.status = status;
        this.iniDate = iniDate;
        this.lastDate = lastDate;
        this.runName = runName;
        this.runDescription = runDescription;
        this.bestIndividual = bestIndividual;
        this.currentGeneration = currentGeneration;
    }

    public Long getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Long experimentId) {
        this.experimentId = experimentId;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public Long getDiagramDataId() {
        return diagramDataId;
    }

    public void setDiagramDataId(Long diagramDataId) {
        this.diagramDataId = diagramDataId;
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

    public String getInitialization() {
        return initialization;
    }

    public void setInitialization(String initialization) {
        this.initialization = initialization;
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

    public String getDefaultGrammar() {
        return defaultGrammar;
    }

    public void setDefaultGrammar(String defaultGrammar) {
        this.defaultGrammar = defaultGrammar;
    }

    public String getDefaultExpDataType() {
        return defaultExpDataType;
    }

    public void setDefaultExpDataType(String defaultExpDataType) {
        this.defaultExpDataType = defaultExpDataType;
    }

    public Run.Status getStatus() {
        return status;
    }

    public void setStatus(Run.Status status) {
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

    public Double getBestIndividual() {
        return bestIndividual;
    }

    public void setBestIndividual(Double bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    public Integer getCurrentGeneration() {
        return currentGeneration;
    }

    public void setCurrentGeneration(Integer currentGeneration) {
        this.currentGeneration = currentGeneration;
    }
}
