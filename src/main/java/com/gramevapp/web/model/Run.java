package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gramevapp.web.other.DateFormat;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "run")
@DynamicUpdate
public class Run {
    public enum Status {INITIALIZING, WAITING, RUNNING, STOPPED, FINISHED, FAILED}


    @Id
    @Column(name = "RUN_ID", updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "RUNS_LIST",
            joinColumns = {
                    @JoinColumn(name = "RUN_ID")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
            }
    )
    private Experiment experimentId;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL,
            mappedBy = "runId")
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
    @Column(name = "EXPERIMENT_NAME")
    private String experimentName;
    @Column(name = "EXPERIMENT_DESCRIPTION")
    private String experimentDescription;

    @Column(name = "ini_date")
    private Timestamp iniDate;

    @Column(name = "modification_date")
    private Timestamp modificationDate;

    @Column
    private Long defaultGrammarId;

    @Column
    private Long defaultExpDataTypeId;

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
    private Integer numCodons = 10;
    @Column
    private Integer numberRuns = 1;
    @Column
    private String model = "";
    @OneToOne(mappedBy = "run", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private RunExecutionReport RunExecutionReport;

    public Run() {
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

    // https://github.com/SomMeri/org.meri.jpa.tutorial/blob/master/src/main/java/org/meri/jpa/relationships/entities/bestpractice/SafeTwitterAccount.java
    public void setExperimentId(Experiment experimentId) {
        if (sameAs(experimentId))
            return;
        Experiment oldExperimentId = this.experimentId;
        this.experimentId = experimentId;

        if (oldExperimentId != null)
            oldExperimentId.removeRun(this);
        if (experimentId != null)
            experimentId.addRun(this);

        this.experimentId = experimentId;
    }

    private boolean sameAs(Experiment newExperiment) {
        return Objects.equals(experimentId, newExperiment);
    }

    public Timestamp getIniDate() {
        return iniDate;
    }

    public String getIniDateFormated() {
        return DateFormat.formatDate(iniDate);
    }

    public String getModificationDateFormated() {
        return DateFormat.formatDate(modificationDate);
    }


    public void setIniDate(Timestamp iniDate) {
        this.iniDate = iniDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Timestamp getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Timestamp modificationDate) {
        this.modificationDate = modificationDate;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public com.gramevapp.web.model.RunExecutionReport getRunExecutionReport() {
        return RunExecutionReport;
    }

    public void setRunExecutionReport(com.gramevapp.web.model.RunExecutionReport runExecutionReport) {
        RunExecutionReport = runExecutionReport;
    }
}