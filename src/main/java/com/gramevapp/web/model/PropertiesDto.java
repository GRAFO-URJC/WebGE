package com.gramevapp.web.model;

public class PropertiesDto {
    /**
     * # PropertiesDto for Pancreas Model Tools
     #Thu Mar 22 12:55:57 CET 2018
     LoggerBasePath=resources/files/properties
     TrainingPath=resources/files/dataType/training/poly10-data-custom.csv
     ValidationPath=resources/files/dataType/validation/poly10-data-custom.csv
     TestPath=resources/files/dataType/test/poly10-data-custom.csv
     ErrorThreshold=0.0
     TournamentSize=2
     WorkDir=resources/files/properties
     RealDataCopied=0
     CrossoverProb=0.65
     BnfPathFile=resources/files/properties/poli10varopt.bnf    // Grammar file
     Objectives=0
     ClassPathSeparator=\:
     Executions=1
     LoggerLevel=INFO
     MutationProb=0.02
     NormalizeData=false
     LogPopulation=1
     ChromosomeLength=100
     NumIndividuals=100
     NumGenerations=300
     ViewResults=false
     MaxWraps=3
     ModelWidth=500
     */

    private Long idExp;

    private String LoggerBasePath = "resources/files/logs/population";
    private String trainingPath;    // resources/files/dataType/training/poly10-data-custom.csv
    private String validationPath;  // resources/files/dataType/validation/poly10-data-custom.csv
    private String testPath;        // resources/files/dataType/test/poly10-data-custom.csv
    private Double errorThreshold;
    private Integer tournamentSize;
    private String workDir = "resources/files";
    private Integer realDataCopied;
    private Double crossoverProb;
    private String bnfPathFile;     // = "resources/files/grammar/poli10varopt.bnf"; -- Grammar file
    private Integer objectives;
    private String classPathSeparator = "=\\:";
    private Integer executions;
    private String loggerLevel;
    private Double mutationProb;
    private Boolean normalizedData;
    private Integer logPopulation;
    private Integer chromosomeLength;   // NumCodons
    private Integer numIndividuals;     // Population value - Population size
    private Integer numGenerations;     // Num generations??
    private Boolean viewResults;
    private Integer maxWraps;
    private Integer modelWidth;

    private Boolean isTraining = false;
    private Boolean isValidation = false;
    private Boolean isTest = false;

    private String experimentName;
    private String experimentDescription;

    private String initialization;      // Random OR Sensible  -- SensibleInitialization
    private String results;             // Text file with the results of the experiments
    private Integer numberRuns;         // Num generations??

    public PropertiesDto() {
    }

    public PropertiesDto(Double errorThreshold, Integer tournamentSize, Integer realDataCopied, Double crossoverProb, String bnfPathFile, Integer objectives, Integer executions, Double mutationProb, Boolean normalizedData, Integer logPopulation, Integer chromosomeLength, Integer numIndividuals, Integer numGenerations, Boolean viewResults, Integer maxWraps, Integer modelWidth, String experimentName, String experimentDescription) {
        this.errorThreshold = errorThreshold;
        this.tournamentSize = tournamentSize;
        this.realDataCopied = realDataCopied;
        this.crossoverProb = crossoverProb;
        this.bnfPathFile = bnfPathFile;
        this.objectives = objectives;
        this.executions = executions;
        this.mutationProb = mutationProb;
        this.normalizedData = normalizedData;
        this.logPopulation = logPopulation;
        this.chromosomeLength = chromosomeLength;
        this.numIndividuals = numIndividuals;
        this.numGenerations = numGenerations;
        this.viewResults = viewResults;
        this.maxWraps = maxWraps;
        this.modelWidth = modelWidth;
        this.experimentName = experimentName;
        this.experimentDescription = experimentDescription;
    }

    public PropertiesDto(String trainingPath, Double errorThreshold, Integer tournamentSize, Integer realDataCopied, Double crossoverProb, String bnfPathFile, Integer objectives, Integer executions, String loggerLevel, Double mutationProb, Boolean normalizedData, Integer logPopulation, Integer chromosomeLength, Integer numIndividuals, Integer numGenerations, Boolean viewResults, Integer maxWraps, Integer modelWidth, String experimentName, String experimentDescription, String initialization, Integer numberRuns) {
        this.trainingPath = trainingPath;
        this.errorThreshold = errorThreshold;
        this.tournamentSize = tournamentSize;
        this.realDataCopied = realDataCopied;
        this.crossoverProb = crossoverProb;
        this.bnfPathFile = bnfPathFile;
        this.objectives = objectives;
        this.executions = executions;
        this.loggerLevel = loggerLevel;
        this.mutationProb = mutationProb;
        this.normalizedData = normalizedData;
        this.logPopulation = logPopulation;
        this.chromosomeLength = chromosomeLength;
        this.numIndividuals = numIndividuals;
        this.numGenerations = numGenerations;
        this.viewResults = viewResults;
        this.maxWraps = maxWraps;
        this.modelWidth = modelWidth;
        this.experimentName = experimentName;
        this.experimentDescription = experimentDescription;
        this.initialization = initialization;
        this.numberRuns = numberRuns;
    }

    public Long getIdExp() {
        return idExp;
    }

    public void setIdExp(Long idExp) {
        this.idExp = idExp;
    }

    public String getLoggerBasePath() {
        return LoggerBasePath;
    }

    public void setLoggerBasePath(String loggerBasePath) {
        LoggerBasePath = loggerBasePath;
    }

    public String getTrainingPath() {
        return trainingPath;
    }

    public void setTrainingPath(String trainingPath) {
        this.trainingPath = trainingPath;
    }

    public Double getErrorThreshold() {
        return errorThreshold;
    }

    public void setErrorThreshold(Double errorThreshold) {
        this.errorThreshold = errorThreshold;
    }

    public Integer getTournamentSize() {
        return tournamentSize;
    }

    public void setTournamentSize(Integer tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public Integer getRealDataCopied() {
        return realDataCopied;
    }

    public void setRealDataCopied(Integer realDataCopied) {
        this.realDataCopied = realDataCopied;
    }

    public Double getCrossoverProb() {
        return crossoverProb;
    }

    public void setCrossoverProb(Double crossoverProb) {
        this.crossoverProb = crossoverProb;
    }

    public String getBnfPathFile() {
        return bnfPathFile;
    }

    public void setBnfPathFile(String bnfPathFile) {
        this.bnfPathFile = bnfPathFile;
    }

    public Integer getObjectives() {
        return objectives;
    }

    public void setObjectives(Integer objectives) {
        this.objectives = objectives;
    }

    public String getClassPathSeparator() {
        return classPathSeparator;
    }

    public void setClassPathSeparator(String classPathSeparator) {
        this.classPathSeparator = classPathSeparator;
    }

    public Integer getExecutions() {
        return executions;
    }

    public void setExecutions(Integer executions) {
        this.executions = executions;
    }

    public String getLoggerLevel() {
        return loggerLevel;
    }

    public void setLoggerLevel(String loggerLevel) {
        this.loggerLevel = loggerLevel;
    }

    public Double getMutationProb() {
        return mutationProb;
    }

    public void setMutationProb(Double mutationProb) {
        this.mutationProb = mutationProb;
    }

    public Boolean getNormalizedData() {
        return normalizedData;
    }

    public void setNormalizedData(Boolean normalizedData) {
        this.normalizedData = normalizedData;
    }

    public Integer getLogPopulation() {
        return logPopulation;
    }

    public void setLogPopulation(Integer logPopulation) {
        this.logPopulation = logPopulation;
    }

    public Integer getChromosomeLength() {
        return chromosomeLength;
    }

    public void setChromosomeLength(Integer chromosomeLength) {
        this.chromosomeLength = chromosomeLength;
    }

    public Integer getNumIndividuals() {
        return numIndividuals;
    }

    public void setNumIndividuals(Integer numIndividuals) {
        this.numIndividuals = numIndividuals;
    }

    public Integer getNumGenerations() {
        return numGenerations;
    }

    public void setNumGenerations(Integer numGenerations) {
        this.numGenerations = numGenerations;
    }

    public Boolean getViewResults() {
        return viewResults;
    }

    public void setViewResults(Boolean viewResults) {
        this.viewResults = viewResults;
    }

    public Integer getMaxWraps() {
        return maxWraps;
    }

    public void setMaxWraps(Integer maxWraps) {
        this.maxWraps = maxWraps;
    }

    public Integer getModelWidth() {
        return modelWidth;
    }

    public void setModelWidth(Integer modelWidth) {
        this.modelWidth = modelWidth;
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

    public Integer getNumberRuns() {
        return numberRuns;
    }

    public void setNumberRuns(Integer numberRuns) {
        this.numberRuns = numberRuns;
    }

    public String getValidationPath() {
        return validationPath;
    }

    public void setValidationPath(String validationPath) {
        this.validationPath = validationPath;
    }

    public String getTestPath() {
        return testPath;
    }

    public void setTestPath(String testPath) {
        this.testPath = testPath;
    }

    public Boolean getTraining() {
        return isTraining;
    }

    public void setTraining(Boolean training) {
        isTraining = training;
    }

    public Boolean getValidation() {
        return isValidation;
    }

    public void setValidation(Boolean validation) {
        isValidation = validation;
    }

    public Boolean getTest() {
        return isTest;
    }

    public void setTest(Boolean test) {
        isTest = test;
    }
}