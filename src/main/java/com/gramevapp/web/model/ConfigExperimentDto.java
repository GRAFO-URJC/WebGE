package com.gramevapp.web.model;

import javax.validation.constraints.*;

public class ConfigExperimentDto {
    private Long id;
    private static final String EXPERIMENTNAME_PATTERN = "^[a-zA-Z0-9À-ÖØ-öø-ÿ \\[\\]()#+_-]{1,254}$";

    @Pattern(regexp = EXPERIMENTNAME_PATTERN, message = "Experiment name cannot have special characters. Only alphanumeric ones and []()#_- are allowed.")
    private String experimentName = "";
    private String experimentDescription = "";
    @Min(value = 1)
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

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @NotNull
    @Size(min = 1)
    private String algorithm = "";
    @NotNull
    @Size(min = 1)
    private String problem = "";
    //SGE
    @Min(value = 0)
    @Max(value = 100)
    @NotNull
    private Integer depthS = 10;

    @NotNull
    @Size(min = 1)
    private String crossoverSGE = "";

    @NotNull
    @Size(min = 1)
    private String mutationSGE = "";
    @Min(value = 0)
    @Max(value = 1)
    @NotNull
    private Double probChangeSGE = 0.0;

    //DSGE
    @Min(value = 0)
    @Max(value = 100)
    @NotNull
    private Integer depthD = 10;

    @NotNull
    @Size(min = 1)
    private String crossoverDSGE = "";

    @NotNull
    @Size(min = 1)
    private String mutationDSGE = "";
    @Min(value = 0)
    @Max(value = 1)
    @NotNull
    private Double probChangeDSGE = 0.0;

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
     * Root Mean Squared Error (RMSE)</option>
     * Mean Squared Error (MSE)
     * R-Squared (R2)
     * Absolute Error (ABS)
     * Relative Error (REL)
     */
    @NotNull
    @Size(min = 1)
    private String objective = "";
    private boolean de = false;
    @NotNull
    private Double lowerBoundDE = -1.0;
    @NotNull
    private Double upperBoundDE = 1.0;
    @NotNull
    @Min(value = 0)
    @Max(value = 100)
    private Double mutationFactorDE = 0.47;
    @NotNull
    @Min(value = 0)
    @Max(value = 100)
    private Double recombinationFactorDE = 0.88;

    private String tagsText = "";

    @Min(value = 4)
    @Max(value = 100000)
    private Integer populationDE = 20;

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

    public Integer getDepthS() {
        return depthS;
    }

    public void setDepthS(Integer depthS) {
        this.depthS = depthS;
    }

    public String getCrossoverSGE() {
        return crossoverSGE;
    }

    public void setCrossoverSGE(String CrossoverSGE) {
        this.crossoverSGE = CrossoverSGE;
    }

    public String getMutationSGE() {
        return mutationSGE;
    }

    public void setMutationSGE(String MutationSGE) {
        this.mutationSGE = MutationSGE;
    }

    public Double getProbChangeSGE() {
        return probChangeSGE;
    }

    public void setProbChangeSGE(Double probChangeSGE) {
        this.probChangeSGE = probChangeSGE;
    }

    public Integer getDepthD() {
        return depthD;
    }

    public void setDepthD(Integer depthD) {
        this.depthD = depthD;
    }

    public String getCrossoverDSGE() {
        return crossoverDSGE;
    }

    public void setCrossoverDSGE(String crossoverDSGE) {
        this.crossoverDSGE = crossoverDSGE;
    }

    public String getMutationDSGE() {
        return mutationDSGE;
    }

    public void setMutationDSGE(String mutationDSGE) {
        this.mutationDSGE = mutationDSGE;
    }

    public Double getProbChangeDSGE() {
        return probChangeDSGE;
    }

    public void setProbChangeDSGE(Double probChangeDSGE) {
        this.probChangeDSGE = probChangeDSGE;
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

    public Double getLowerBoundDE() {
        return lowerBoundDE;
    }

    public void setLowerBoundDE(Double lowerBoundDE) {
        this.lowerBoundDE = lowerBoundDE;
    }

    public Double getUpperBoundDE() {
        return upperBoundDE;
    }

    public void setUpperBoundDE(Double upperBoundDE) {
        this.upperBoundDE = upperBoundDE;
    }

    public Double getMutationFactorDE() {
        return mutationFactorDE;
    }

    public void setMutationFactorDE(Double mutationFactorDE) {
        this.mutationFactorDE = mutationFactorDE;
    }

    public Double getRecombinationFactorDE() {
        return recombinationFactorDE;
    }

    public void setRecombinationFactorDE(Double recombinationFactorDE) {
        this.recombinationFactorDE = recombinationFactorDE;
    }

    public String getTagsText() {
        return tagsText;
    }

    public void setTagsText(String tagsText) {
        this.tagsText = tagsText;
    }

    public Integer getPopulationDE() {
        return populationDE;
    }

    public void setPopulationDE(Integer populationDE) {
        this.populationDE = populationDE;
    }
}