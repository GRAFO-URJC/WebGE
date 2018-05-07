package com.gramevapp.web.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="experiment")
public class Experiment {

    @Id
    @Column(name = "EXPERIMENT_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator="native") // Efficiency  -> https://vladmihalcea.com/why-should-not-use-the-auto-jpa-generationtype-with-mysql-and-hibernate/
    @GenericGenerator(
            name = "native",
            strategy = "native")
    private Long id;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinTable(
            name = "users_experiments",
            joinColumns = {
                    @JoinColumn(name = "EXPERIMENT_ID", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
            }
    )
    private User userId;

    @Column(name="EXPERIMENT_NAME") // Reference for user relation and ExpDataType and Grammar
    private String experimentName;

    @Column(name="EXPERIMENT_DESCRIPTION") // Reference for user relation and ExpDataType and Grammar
    private String experimentDescription;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToMany(fetch=FetchType.LAZY,
            mappedBy = "experimentId")
    private List<Grammar> idGrammarList = new ArrayList<>();

    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "default_grammar")
    private Grammar defaultGrammar;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToMany(fetch=FetchType.LAZY,
            mappedBy = "experimentId")
    private List<ExperimentDataType> idExpDataTypeList = new ArrayList<>();

    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "default_dataType")
    private ExperimentDataType defaultExpDataType;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToMany(fetch=FetchType.LAZY,
            mappedBy = "experimentId")
    private List<Run> idRunList = new ArrayList<>();

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

    @Column(name="creationDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = null;

    @Column(name="updateDate")
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

    public Grammar getDefaultGrammar() {
        return defaultGrammar;
    }

    public void setDefaultGrammar(Grammar defaultGrammar) {
        this.defaultGrammar = defaultGrammar;
        defaultGrammar.setExperimentId(this);
    }

    public ExperimentDataType getDefaultExpDataType() {
        return defaultExpDataType;
    }

    public void setDefaultExpDataType(ExperimentDataType defaultExpDataType) {
        this.defaultExpDataType = defaultExpDataType;
        defaultExpDataType.setExperimentId(this);
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

    public Run addRun(Run run) {
        this.idRunList.add(run);
        run.setExperimentId(this);
        return run;
    }

    public Grammar addGrammar(Grammar grammar) {
        this.idGrammarList.add(grammar);
        grammar.setExperimentId(this);
        return grammar;
    }

    public ExperimentDataType addExperimentDataType(ExperimentDataType expData) {
        this.idExpDataTypeList.add(expData);
        expData.setExperimentId(this);
        return expData;
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

    public void updateExperiment(Grammar grammar, ExperimentDataType expDataType, String experimentName, String experimentDescription, Integer generations, Integer populationSize, Integer maxWraps, Integer tournament, Double crossoverProb, Double mutationProb, String initialization, String results, Integer numCodons, Integer numberRuns, Date modificationDate){
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
    }
}