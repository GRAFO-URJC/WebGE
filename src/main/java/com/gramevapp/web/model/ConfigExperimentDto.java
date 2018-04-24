package com.gramevapp.web.model;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ConfigExperimentDto {
    @NotEmpty(message = "Please, enter experiment name")
    private String experimentName="";
    @NotEmpty(message = "Enter experiment description")
    private String experimentDescription;
    @Min(value=0)
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
    @NotEmpty
    private String initialization = " ";       // Random OR Sensible
    @NotEmpty
    private String results = " ";             // Text file with the results of the experiments
    @Min(value=0)
    @Max(value=100)
    @NotNull
    private Integer numCodons =10;
    @Min(value=0)
    @Max(value=100)
    @NotNull
    private Integer numberRuns = 1;
    @NotEmpty
    private String defaultGrammar = " ";
    @NotEmpty
    private String defaultExpDataType = " ";
    @NotEmpty
    private String grammarName = " ";
    @NotEmpty
    private String grammarDescription = " ";
    @NotEmpty
    private String fileText = " "; // This is the text on the file - That's written in an areaText - So we can take it as a String
    @NotEmpty
    private String dataTypeName = " ";
    @NotEmpty
    private String dataTypeDescription = " "; // status
    @NotEmpty
    private String dataTypeType = " ";        // Validation, test, training

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

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public String getDataTypeDescription() {
        return dataTypeDescription;
    }

    public void setDataTypeDescription(String dataTypeDescription) {
        this.dataTypeDescription = dataTypeDescription;
    }

    public String getDataTypeType() {
        return dataTypeType;
    }

    public void setDataTypeType(String dataTypeType) {
        this.dataTypeType = dataTypeType;
    }
}