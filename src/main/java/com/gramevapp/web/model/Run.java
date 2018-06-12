package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "run")
public class Run implements Cloneable
{
    public enum Status { INITIALIZING, RUNNING, PAUSED, STOPPED, FINISHED, FAILED; };

    @Id
    @Column(name="RUN_ID", updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(cascade=CascadeType.ALL)
    private User userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "runs_list",
            joinColumns = {
                    @JoinColumn(name = "RUN_ID", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
            }
    )
    private Experiment experimentId;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "DIAGRAM_DATA", referencedColumnName = "DIAGRAM_DATA_ID")
    private DiagramData diagramData;

    @Column
    private Long threaId;

    @Column
    private Double bestIndividual = 0.0;  // Best solution

    @Column
    private Integer currentGeneration = 0;

    @Column
    private Long idProperties;

    @Column
    private Status status;
    @Column
    private String runName;
    @Column
    private String runDescription;

    @Column(name="iniDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date iniDate;

    @Column(name="endDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDate;

    @Column(name="EXPERIMENT_NAME") // Reference for user relation and ExpDataType and Grammar
    private String experimentName;

    @Column(name="EXPERIMENT_DESCRIPTION") // Reference for user relation and ExpDataType and Grammar
    private String experimentDescription;

    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToMany(fetch=FetchType.LAZY,
            mappedBy = "runId")
    private List<Grammar> idGrammarList;

    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "default_grammar")
    private Grammar defaultGrammar;

    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToMany(fetch=FetchType.LAZY,
            mappedBy = "runId")
    private List<ExperimentDataType> idExpDataTypeList;

    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "default_dataType")
    private ExperimentDataType defaultExpDataType;

    @Column
    private Long defaultRunId;

    @Column
    private Integer generations = 1000;
    @Column
    private Integer populationSize = 50;
    @Column
    private Integer maxWraps = 3;
    @Column
    private Integer tournament = 2;
    @Column
    private Double crossoverProb = 0.5;
    @Column
    private Double mutationProb = 0.5;
    @Column
    private String initialization = "";       // Random OR Sensible
    @Column
    private String objective;
    @Column
    private String results = "";             // Text file with the results of the experiments
    @Column
    private Integer numCodons =10;
    @Column
    private Integer numberRuns = 1;

    public Run(Run run){
        this(run.getUserId(), run.getExperimentId(), run.getDiagramData(), run.getBestIndividual(), run.getCurrentGeneration(), run.getIdProperties(), run.getStatus(), run.getRunName(), run.getRunDescription(), run.getIniDate(), run.getLastDate(), run.getExperimentName(), run.getExperimentDescription(), run.getDefaultRunId(), run.getGenerations(), run.getPopulationSize(), run.getMaxWraps(), run.getTournament(), run.getCrossoverProb(), run.getMutationProb(), run.getInitialization(), run.getObjective(), run.getResults(), run.getNumCodons(), run.getNumberRuns());
    }

    public Run(User userId, Experiment experimentId, DiagramData diagramData, Double bestIndividual, Integer currentGeneration, Long idProperties, Status status, String runName, String runDescription, Date iniDate, Date lastDate, String experimentName, String experimentDescription, Long defaultRunId, Integer generations, Integer populationSize, Integer maxWraps, Integer tournament, Double crossoverProb, Double mutationProb, String initialization, String objective, String results, Integer numCodons, Integer numberRuns) {
        this.userId = userId;
        this.experimentId = experimentId;
        this.diagramData = diagramData;
        this.bestIndividual = bestIndividual;
        this.currentGeneration = currentGeneration;
        this.idProperties = idProperties;
        this.status = status;
        this.runName = runName;
        this.runDescription = runDescription;
        this.iniDate = iniDate;
        this.lastDate = lastDate;
        this.experimentName = experimentName;
        this.experimentDescription = experimentDescription;
        this.defaultRunId = defaultRunId;
        this.generations = generations;
        this.populationSize = populationSize;
        this.maxWraps = maxWraps;
        this.tournament = tournament;
        this.crossoverProb = crossoverProb;
        this.mutationProb = mutationProb;
        this.initialization = initialization;
        this.objective = objective;
        this.results = results;
        this.numCodons = numCodons;
        this.numberRuns = numberRuns;
    }

    public Run() {
        this.idExpDataTypeList = new ArrayList<>();
        this.idGrammarList = new ArrayList<>();
    }

    public Run(User userId, Status status) {
        // DATE TIMESTAMP
        Calendar calendar = Calendar.getInstance();
        java.sql.Date currentTimestamp = new java.sql.Date(calendar.getTime().getTime());

        this.iniDate = currentTimestamp;
        this.lastDate = currentTimestamp;
        this.userId = userId;
        this.status = status;

        this.idExpDataTypeList = new ArrayList<>();
        this.idGrammarList = new ArrayList<>();
    }

    public Run(User userId, Status status, String runName, String runDescription, Date iniDate, Date lastDate) {
        this.userId = userId;
        this.runName = runName;
        this.runDescription = runDescription;
        this.status = status;
        this.iniDate = iniDate;
        this.lastDate = lastDate;

        this.idExpDataTypeList = new ArrayList<>();
        this.idGrammarList = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Experiment getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Experiment experimentId) {
        this.experimentId = experimentId;
    }

    public Date getIniDate() {
        return iniDate;
    }

    public void setIniDate(Date iniDate) {
        this.iniDate = iniDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
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

    public Long getIdProperties() {
        return idProperties;
    }

    public void setIdProperties(Long idProperties) {
        this.idProperties = idProperties;
    }

    public DiagramData getDiagramData() {
        return diagramData;
    }

    public void setDiagramData(DiagramData diagramData) {
        this.diagramData = diagramData;
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

    public List<Grammar> getIdGrammarList() {
        return idGrammarList;
    }

    public void setIdGrammarList(List<Grammar> idGrammarList) {
        this.idGrammarList = idGrammarList;
    }

    public Grammar getDefaultGrammar() {
        return defaultGrammar;
    }

    public void setDefaultGrammar(Grammar defaultGrammar) {
        this.defaultGrammar = defaultGrammar;
    }

    public List<ExperimentDataType> getIdExpDataTypeList() {
        return idExpDataTypeList;
    }

    public void setIdExpDataTypeList(List<ExperimentDataType> idExpDataTypeList) {
        this.idExpDataTypeList = idExpDataTypeList;
    }

    public ExperimentDataType getDefaultExpDataType() {
        return defaultExpDataType;
    }

    public void setDefaultExpDataType(ExperimentDataType defaultExpDataType) {
        this.defaultExpDataType = defaultExpDataType;
    }

    public Long getDefaultRunId() {
        return defaultRunId;
    }

    public void setDefaultRunId(Long defaultRunId) {
        this.defaultRunId = defaultRunId;
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

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
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

    public Long getThreaId() {
        return threaId;
    }

    public void setThreaId(Long threaId) {
        this.threaId = threaId;
    }

    public Grammar addGrammar(Grammar grammar) {
        this.idGrammarList.add(grammar);
        grammar.setExperimentId(experimentId);
        return grammar;
    }

    public ExperimentDataType addExperimentDataType(ExperimentDataType expData) {
        this.idExpDataTypeList.add(expData);
        expData.setExperimentId(experimentId);
        return expData;
    }

    public void updateRun(Grammar grammar, ExperimentDataType expDataType, String experimentName, String experimentDescription, Integer generations, Integer populationSize, Integer maxWraps, Integer tournament, Double crossoverProb, Double mutationProb, String initialization, String results, Integer numCodons, Integer numberRuns, String objective, Date modificationDate){
        this.defaultExpDataType = expDataType;
        this.defaultGrammar = grammar;
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
        this.lastDate = modificationDate;
        this.objective = objective;
    }
}