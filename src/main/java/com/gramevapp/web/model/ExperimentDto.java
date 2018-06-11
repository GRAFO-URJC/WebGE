package com.gramevapp.web.model;


public class ExperimentDto {

    private Long id;

    private String experimentName;
    private String experimentDescription;
    private Integer generations = 1000;
    private Integer populationSize = 100;
    private Integer maxWraps = 3;
    private Integer tournament = 2;
    private Double crossoverProb = 0.5;
    private Double mutationProb = 0.5;
    private String initialization = "";       // Random OR Sensible
    private String results = "";             // Text file with the results of the experiments
    private Integer numCodons = 100;
    private Integer numberRuns = 1;
    private Long defaultGrammarId;
    private Long defaultExpDataTypeId;
    private Long defaultRunId;
    private ExpProperties properties;
    private Long diagramDataId;

    /**
     * 0 -> Root Mean Squared Error (RMSE)
     1 -> Clarke Error Grid (CEG)
     2 -> Bi-objective: RMSE & CEG
     3 -> R Square (R^2)
     4 -> Absolute Error (Abs. Error)
     5 -> Mean Absolute Relative Deviation (MARD)
     */
    private String objective;

    public ExperimentDto() {
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

    public Long getDefaultGrammarId() {
        return defaultGrammarId;
    }

    public void setDefaultGrammarId(Long defaultGrammarId) {
        this.defaultGrammarId = defaultGrammarId;
    }

    public Long getDefaultExpDataTypeId() {
        return defaultExpDataTypeId;
    }

    public void setDefaultExpDataTypeId(Long defaultExpDataTypeId) {
        this.defaultExpDataTypeId = defaultExpDataTypeId;
    }

    public Integer getNumberRuns() {
        return numberRuns;
    }

    public void setNumberRuns(Integer numberRuns) {
        this.numberRuns = numberRuns;
    }

    public Integer getNumCodons() {
        return numCodons;
    }

    public void setNumCodons(Integer numCodons) {
        this.numCodons = numCodons;
    }

    public String getExperimentDescription() {
        return experimentDescription;
    }

    public void setExperimentDescription(String experimentDescription) {
        this.experimentDescription = experimentDescription;
    }

    public String getInitialization() {
        return initialization;
    }

    public void setInitialization(String initialization) {
        this.initialization = initialization;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDefaultRunId() {
        return defaultRunId;
    }

    public void setDefaultRunId(Long defaultRunId) {
        this.defaultRunId = defaultRunId;
    }

    public ExpProperties getProperties() {
        return properties;
    }

    public void setProperties(ExpProperties properties) {
        this.properties = properties;
    }

    public Long getDiagramDataId() {
        return diagramDataId;
    }

    public void setDiagramDataId(Long diagramDataId) {
        this.diagramDataId = diagramDataId;
    }
}