package com.gramevapp.web.model;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ConfigExperimentDto {
    private Long id;

    private String experimentName;
    private String experimentDescription;
    @Min(value=100)
    @Max(value=100000)
    @NotNull
    private Integer generations = 1000;
    @Min(value=0)
    @Max(value=100000)
    @NotNull
    private Integer populationSize = 50;
    @Min(value=0)
    @Max(value=1000)
    @NotNull
    private Integer maxWraps = 3;
    @Min(value=0)
    @Max(value=1000)
    @NotNull
    private Integer tournament = 2;
    @Min(value=0)
    @Max(value=100)
    @NotNull
    private Double crossoverProb = 0.5;
    @Min(value=0)
    @Max(value=100)
    @NotNull
    private Double mutationProb = 0.5;
    @NotEmpty(message = "Initialization cannot be empty")
    private String initialization = " ";       // Random OR Sensible
    @NotEmpty(message = "Results cannot be empty")
    private String results = " ";             // Text file with the results of the experiments
    @Min(value=0)
    @Max(value=100)
    @NotNull
    private Integer numCodons =10;
    @Min(value=0)
    @Max(value=100)
    @NotNull
    private Integer numberRuns = 1;
    private Long defaultGrammarId;
    private Long defaultExpDataTypeId;
    private Long testDefaultExpDataTypeId;
    private String grammarName;
    private String grammarDescription;
    @NotEmpty(message = "Grammar area text cannot be empty")
    private String fileText; // This is the text on the file - That's written in an areaText - So we can take it as a String
    private String dataTypeType = "training";   // Validation, test, training

    private Long diagramDataId;
    private Long defaultRunId;

    /**
     * 0 -> Root Mean Squared Error (RMSE)
     1 -> Clarke Error Grid (CEG)
     2 -> Bi-objective: RMSE & CEG
     3 -> R Square (R^2)
     4 -> Absolute Error (Abs. Error)
     5 -> Mean Absolute Relative Deviation (MARD)
     */
    @NotEmpty
    private String objective;
    private MultipartFile typeFile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDiagramDataId() {
        return diagramDataId;
    }

    public void setDiagramDataId(Long diagramDataId) {
        this.diagramDataId = diagramDataId;
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

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public MultipartFile getTypeFile() {
        return typeFile;
    }

    public void setTypeFile(MultipartFile typeFile) {
        this.typeFile = typeFile;
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


    public String getGrammarName() {
        return grammarName;
    }

    public void setGrammarName(String grammarName) {
        this.grammarName = grammarName;
    }

    public String getGrammarDescription() {
        return grammarDescription;
    }

    public void setGrammarDescription(String grammarDescription) {
        this.grammarDescription = grammarDescription;
    }

    public String getFileText() {
        return fileText;
    }

    public void setFileText(String fileText) {
        this.fileText = fileText;
    }


    public String getDataTypeType() {
        return dataTypeType;
    }

    public void setDataTypeType(String dataTypeType) {
        this.dataTypeType = dataTypeType;
    }

    public Long getDefaultRunId() {
        return defaultRunId;
    }

    public void setDefaultRunId(Long defaultRunId) {
        this.defaultRunId = defaultRunId;
    }

    public Long getTestDefaultExpDataTypeId() {
        return testDefaultExpDataTypeId;
    }

    public void setTestDefaultExpDataTypeId(Long testDefaultExpDataTypeId) {
        this.testDefaultExpDataTypeId = testDefaultExpDataTypeId;
    }

}