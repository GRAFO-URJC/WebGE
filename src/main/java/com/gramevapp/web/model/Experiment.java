package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.gramevapp.web.other.BeanUtil;
import com.gramevapp.web.other.DateFormat;
import com.gramevapp.web.service.ExperimentService;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.context.ApplicationContext;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "experiment")
@DynamicUpdate
public class Experiment {

    @Id
    @Column(name = "experiment_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonManagedReference
    @ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_experiments",
            joinColumns = {
                    @JoinColumn(name = "experiment_id", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
            }
    )
    private User userId;

    @Column(name = "EXPERIMENT_NAME") // Reference for user relation and ExpDataType and Grammar
    private String experimentName;

    @Column(name = "EXPERIMENT_DESCRIPTION") // Reference for user relation and ExpDataType and Grammar
    private String experimentDescription;

    @JsonBackReference
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ManyToMany(fetch = FetchType.LAZY,
            mappedBy = "listExperiment")
    private List<Grammar> idGrammarList;

    @Column
    private Long defaultGrammar;

    @JsonBackReference
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ManyToMany(fetch = FetchType.LAZY,
            mappedBy = "experimentList")
    private List<ExperimentDataType> idExpDataTypeList;

    @Column
    private Long defaultExpDataType;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            mappedBy = "experimentId",
            orphanRemoval = true)
    private List<Run> idRunList;

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

    @Column(name = "creationDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = null;

    @Column(name = "updateDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate = null;

    public Experiment() {
        this.idExpDataTypeList = new ArrayList<>();
        this.idGrammarList = new ArrayList<>();
        this.idRunList = new ArrayList<>();
    }

    public Experiment(User userId, String experimentName, List<Grammar> idGrammarList, List<ExperimentDataType> idExpDataTypeList, List<Run> idRunList) {
        this.userId = userId;
        this.experimentName = experimentName;
        this.idGrammarList = idGrammarList;
        this.idExpDataTypeList = idExpDataTypeList;
        this.idRunList = idRunList;
    }

    public Experiment(User userId, String experimentName, String experimentDescription, Integer generations, Integer populationSize, Integer maxWraps, Integer tournament, Double crossoverProb, Double mutationProb, String initialization, String results, Integer numCodons, Integer numberRuns, String objective, Date creationDate, Date modificationDate) {
        this.userId = userId;
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
        this.objective = objective;

        this.idExpDataTypeList = new ArrayList<>();
        this.idGrammarList = new ArrayList<>();
        this.idRunList = new ArrayList<>();

        this.modificationDate = modificationDate;
        this.creationDate = creationDate;
    }

    public Experiment(User userId, String experimentName, String experimentDescription, List<Grammar> idGrammarList, List<ExperimentDataType> idExpDataTypeList, List<Run> idRunList, Integer generations, Integer populationSize, Integer maxWraps, Integer tournament, Double crossoverProb, Double mutationProb, String initialization, String results, Integer numCodons, Integer numberRuns, String objective, Date creationDate, Date modificationDate) {
        this.userId = userId;
        this.experimentName = experimentName;
        this.experimentDescription = experimentDescription;
        this.idGrammarList = idGrammarList;
        this.idExpDataTypeList = idExpDataTypeList;
        this.idRunList = idRunList;
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
        this.objective = objective;
        this.modificationDate = modificationDate;
        this.creationDate = creationDate;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public Long getDefaultGrammar() {
        return defaultGrammar;
    }

    public void setDefaultGrammar(Long defaultGrammarId) {
        this.defaultGrammar = defaultGrammarId;
        ApplicationContext context = BeanUtil.getAppContext();
        ExperimentService experimentService = (ExperimentService) context.getBean("experimentService");

        Grammar defaultGrammar = experimentService.findGrammarById(defaultGrammarId);
        if (defaultGrammar != null)
            defaultGrammar.addExperimentId(this);
    }

    public Long getDefaultExpDataType() {
        return defaultExpDataType;
    }

    public void setDefaultExpDataType(Long defaultExpDataTypeId) {
        this.defaultExpDataType = defaultExpDataTypeId;
        ApplicationContext context = BeanUtil.getAppContext();
        ExperimentService experimentService = (ExperimentService) context.getBean("experimentService");

        ExperimentDataType defaultExpDType = experimentService.findExperimentDataTypeById(defaultExpDataType);
        if (defaultExpDType != null)
            defaultExpDType.addExperimentList(this);
    }

    public List<Run> getIdRunList() {
        return idRunList;
    }

    public void setIdRunList(List<Run> idRunList) {
        this.idRunList = idRunList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumberRuns() {
        return numberRuns;
    }

    public void setNumberRuns(Integer numberRuns) {
        this.numberRuns = numberRuns;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
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

    //https://github.com/SomMeri/org.meri.jpa.tutorial/blob/master/src/main/java/org/meri/jpa/relationships/entities/bestpractice/SafePerson.java
    public void addRun(Run run) {
        if (this.idRunList.contains(run))
            return;
        this.idRunList.add(run);
        run.setExperimentId(this);
    }

    public void removeRun(Run run) {
        if (!idRunList.contains(run))
            return;
        idRunList.remove(run);
        run.setExperimentId(null);
    }

    public void addGrammar(Grammar grammar) {
        if (this.idGrammarList.contains(grammar))
            return;
        this.idGrammarList.add(grammar);
        grammar.addExperimentId(this);
    }

    public void removeGrammar(Grammar grammar) {
        if (!idGrammarList.contains(grammar))
            return;
        idGrammarList.remove(grammar);
        grammar.deleteExperimentId(this);
    }

    public void addExperimentDataType(ExperimentDataType expData) {
        if (this.idExpDataTypeList.contains(expData))
            return;
        this.idExpDataTypeList.add(expData);
        expData.addExperimentList(this);
    }

    public void removeExperimentDataType(ExperimentDataType expData) {
        if (!idExpDataTypeList.contains(expData))
            return;
        idExpDataTypeList.remove(expData);
        expData.deleteExperimentInList(this);
    }

    public List<ExperimentDataType> getIdExpDataTypeList() {
        return idExpDataTypeList;
    }

    public void setIdExpDataTypeList(List<ExperimentDataType> idExpDataTypeList) {
        this.idExpDataTypeList = idExpDataTypeList;
    }

    public Integer getNumCodons() {
        return numCodons;
    }

    public void setNumCodons(Integer numCodons) {
        this.numCodons = numCodons;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Long getDefaultRunId() {
        return defaultRunId;
    }

    public void setDefaultRunId(Long defaultRunId) {
        this.defaultRunId = defaultRunId;
    }

    public void updateExperiment(Long grammar, Long expDataType, String experimentName, String experimentDescription, Integer generations, Integer populationSize, Integer maxWraps, Integer tournament, Double crossoverProb, Double mutationProb, String initialization, String results, Integer numCodons, Integer numberRuns, String objective, Date modificationDate) {
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
        this.modificationDate = modificationDate;
        this.objective = objective;
    }

    @Override
    public String toString() {
        return String.valueOf(this.id);
    }

    public Experiment clone() {
        Experiment experiment = new Experiment();
        experiment.userId = this.userId;
        experiment.experimentName = this.experimentName;
        experiment.experimentDescription = this.experimentDescription;
        experiment.defaultGrammar = this.defaultGrammar;
        experiment.defaultExpDataType = this.defaultExpDataType;
        experiment.generations = this.generations;
        experiment.populationSize = this.populationSize;
        experiment.maxWraps = this.maxWraps;
        experiment.tournament = this.tournament;
        experiment.crossoverProb = this.crossoverProb;
        experiment.mutationProb = this.mutationProb;
        experiment.initialization = this.initialization;
        experiment.objective = this.objective;
        experiment.results = this.results;
        experiment.numCodons = this.numCodons;
        experiment.numberRuns = this.numberRuns;

        return experiment;
    }

    public String getCreationDateFormated() {
        return DateFormat.formatDate(creationDate);
    }

    public String getModificationDateFormated() {
        return DateFormat.formatDate(modificationDate);
    }
}