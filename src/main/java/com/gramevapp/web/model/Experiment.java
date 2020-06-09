package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.gramevapp.web.other.BeanUtil;
import com.gramevapp.web.other.DateFormat;
import com.gramevapp.web.service.ExperimentService;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.context.ApplicationContext;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
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

    @Column
    private String defaultGrammar;

    @JsonBackReference
    @ManyToMany
    @JoinTable(
            name = "dataset_list",
            joinColumns = {
                    @JoinColumn(name = "experiment_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "experimentdatatype_id", referencedColumnName = "experimentdatatype_id")
            }
    )
    private List<Dataset> idExpDataTypeList;

    @Column
    private Long defaultExpDataType;

    @Column
    private Long defaultTestExpDataTypeId;

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

    @Column(name = "creation_date")
    private Timestamp creationDate = null;

    @Column(name = "update_date")
    private Timestamp modificationDate = null;

    public Experiment() {
        this.idExpDataTypeList = new ArrayList<>();
        this.idRunList = new ArrayList<>();
    }

    public Experiment(User userId, String experimentName, String experimentDescription, Integer generations, Integer populationSize,
                      Integer maxWraps, Integer tournament, Double crossoverProb,
                      Double mutationProb, String initialization, String results, Integer numCodons,
                      Integer numberRuns, String objective, Timestamp creationDate, Timestamp modificationDate) {
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
        this.idRunList = new ArrayList<>();

        this.modificationDate = modificationDate;
        this.creationDate = creationDate;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getDefaultGrammar() {
        return defaultGrammar;
    }

    public void setDefaultGrammar(String defaultGrammar) {
        this.defaultGrammar = defaultGrammar;
    }

    public Long getDefaultExpDataType() {
        return defaultExpDataType;
    }

    public void setDefaultExpDataType(Long defaultExpDataTypeId) {
        this.defaultExpDataType = defaultExpDataTypeId;
        ApplicationContext context = BeanUtil.getAppContext();
        ExperimentService experimentService = (ExperimentService) context.getBean("experimentService");
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

    public void addExperimentDataType(Dataset expData) {
        if (this.idExpDataTypeList.contains(expData))
            return;
        this.idExpDataTypeList.add(expData);
    }

    public void removeExperimentDataType(Dataset expData) {
        if (!idExpDataTypeList.contains(expData))
            return;
        idExpDataTypeList.remove(expData);
    }

    public List<Dataset> getIdExpDataTypeList() {
        return idExpDataTypeList;
    }

    public void setIdExpDataTypeList(List<Dataset> idExpDataTypeList) {
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

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Timestamp getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Timestamp modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Long getDefaultRunId() {
        return defaultRunId;
    }

    public void setDefaultRunId(Long defaultRunId) {
        this.defaultRunId = defaultRunId;
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

    public Long getDefaultTestExpDataTypeId() {
        return defaultTestExpDataTypeId;
    }

    public void setDefaultTestExpDataTypeId(Long defaultTestExpDataTypeId) {
        this.defaultTestExpDataTypeId = defaultTestExpDataTypeId;
    }
}