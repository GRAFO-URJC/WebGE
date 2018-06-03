package com.gramevapp.web.model;

import javax.persistence.*;

@Entity
@Table(name = "expProperties")
public class ExpProperties {

    private static final String LOGGER_BASE_PATH = "resources/files/logs/population";
    private static final String WORK_DIR = "resources/files";
    private static final String CLASS_PATH_SEPARATOR = "\\;";

    // @org.hibernate.annotations.Type(type = "pg-uuid")
    // @Column(columnDefinition="uuid")
    /*@GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "BINARY(16)", name = "PROPERTIES_ID", nullable = false, updatable= false)*/
    // private UUID id;

    /*@Id
    @Column(columnDefinition="uuid")
    @org.hibernate.annotations.Type(type="pg-uuid")*/

    /*@Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)", name = "PROPERTIES_ID")*/

    @Id
    @Column(name = "PROPERTIES_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    //private UUID id = UUID.randomUUID();

    @OneToOne(cascade=CascadeType.ALL)
    private User userId;

    @Column
    private Long idExp;

    @Column
    private Long idRun;

    @Column
    private String uuidPropDto;

    @Column
    private String loggerBasePath;
    @Column
    private String trainingPath;    // resources/files/dataType/training/poly10-data-custom.csv
    @Column
    private String validationPath;  // resources/files/dataType/validation/poly10-data-custom.csv
    @Column
    private String testPath;        // resources/files/dataType/test/poly10-data-custom.csv
    @Column
    private Double errorThreshold;
    @Column
    private Integer tournamentSize;
    @Column
    private String workDir = WORK_DIR;
    @Column
    private Integer realDataCopied;
    @Column
    private Double crossoverProb;
    @Column
    private String bnfPathFile;     // = "resources/files/grammar/poli10varopt.bnf"; -- Grammar file
    @Column
    private Integer objectives;
    @Column
    private String classPathSeparator = CLASS_PATH_SEPARATOR;
    @Column
    private Integer executions;
    @Column
    private String loggerLevel;
    @Column
    private Double mutationProb;
    @Column
    private Boolean normalizedData;
    @Column
    private Integer logPopulation;
    @Column
    private Integer chromosomeLength;   // NumCodons
    @Column
    private Integer numIndividuals;     // Population value - Population size
    @Column
    private Integer numGenerations;     // Num generations - Generations
    @Column
    private Boolean viewResults;
    @Column
    private Integer maxWraps;
    @Column
    private Integer modelWidth;

    @Column
    private String experimentName;
    @Column
    private String experimentDescription;

    @Column
    private String initialization;      // Random OR Sensible  -- SensibleInitialization
    @Column
    private String results;             // Text file with the results of the experiments
    @Column
    private Integer numberRuns;         // Num executions. Execute the experiment N times and obtain N solutions.

    public ExpProperties(){
    }

    public ExpProperties(Long id){
        this.id = id;
    }

    public ExpProperties(Long idRun, Long idExp, String loggerBasePath, String trainingPath, String validationPath, String testPath, Double errorThreshold, Integer tournamentSize, String workDir, Integer realDataCopied, Double crossoverProb, String bnfPathFile, Integer objectives, String classPathSeparator, Integer executions, String loggerLevel, Double mutationProb, Boolean normalizedData, Integer logPopulation, Integer chromosomeLength, Integer numIndividuals, Integer numGenerations, Boolean viewResults, Integer maxWraps, Integer modelWidth, String experimentName, String experimentDescription, String initialization, String results, Integer numberRuns) {
        this.idRun = idRun;
        this.idExp = idExp;
        this.loggerBasePath = loggerBasePath;
        this.trainingPath = trainingPath;
        this.validationPath = validationPath;
        this.testPath = testPath;
        this.errorThreshold = errorThreshold;
        this.tournamentSize = tournamentSize;
        this.workDir = workDir;
        this.realDataCopied = realDataCopied;
        this.crossoverProb = crossoverProb;
        this.bnfPathFile = bnfPathFile;
        this.objectives = objectives;
        this.classPathSeparator = classPathSeparator;
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
        this.results = results;
        this.numberRuns = numberRuns;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdExp() {
        return idExp;
    }

    public void setIdExp(Long idExp) {
        this.idExp = idExp;
    }

    public String getLoggerBasePath() {
        return loggerBasePath;
    }

    public void setLoggerBasePath(String loggerBasePath) {
        this.loggerBasePath = loggerBasePath;
    }

    public String getTrainingPath() {
        return trainingPath;
    }

    public void setTrainingPath(String trainingPath) {
        this.trainingPath = trainingPath;
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

    public Long getIdRun() {
        return idRun;
    }

    public void setIdRun(Long idRun) {
        this.idRun = idRun;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public String getUuidPropDto() {
        return uuidPropDto;
    }

    public void setUuidPropDto(String uuidPropDto) {
        this.uuidPropDto = uuidPropDto;
    }
}