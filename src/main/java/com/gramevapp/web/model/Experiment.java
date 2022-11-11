package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.gramevapp.web.other.DateFormat;
import org.hibernate.annotations.DynamicUpdate;

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

    @Column(name = "userId")
    private Long userId;

    @Column(name = "EXPERIMENT_NAME") // Reference for user relation and ExpDataType and Grammar
    private String experimentName;

    @Column(name = "EXPERIMENT_DESCRIPTION") // Reference for user relation and ExpDataType and Grammar
    private String experimentDescription;

    @Column(columnDefinition = "TEXT", name = "default_grammar")
    private String defaultGrammar;

    @Column
    private Boolean crossExperiment = false;

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
    @OrderColumn(name = "id")
    private List<Run> idRunList;

    @Column
    private Integer generations = 1000;
    @Column
    private Integer populationSize = 50;
    /*GE*/
    @Column
    private Integer maxWraps = 3;
    @Column
    private Integer tournament = 2;
    @Column
    private Double crossoverProb = 0.5;
    @Column
    private Double mutationProb = 0.5;
    @Column
    private String objective;
    @Column
    private Integer numCodons = 10;

    /*SGE*/
    @Column
    private String problem;
    //SGE
    @Column
    private Integer depthS = 10;

    @Column
    private String crossoverSGE;

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
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

    public void setCrossoverSGE(String crossoverSGE) {
        this.crossoverSGE = crossoverSGE;
    }

    public String getMutationSGE() {
        return mutationSGE;
    }

    public void setMutationSGE(String mutationSGE) {
        this.mutationSGE = mutationSGE;
    }

    public Double getProbChangeSGE() {
        return probChangeSGE;
    }

    public void setProbChangeSGE(Double probChangeSGE) { this.probChangeSGE = probChangeSGE; }

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

    @Column
    private String mutationSGE;
    @Column
    private Double probChangeSGE = 0.0;

    //DSGE
    @Column
    private Integer depthD = 10;

    @Column
    private String crossoverDSGE;

    @Column
    private String mutationDSGE;
    @Column
    private Double probChangeDSGE = 0.0;

    @Column
    private Integer numberRuns = 1;

    @Column(name = "creation_date")
    private Timestamp creationDate = null;

    @Column(name = "update_date")
    private Timestamp modificationDate = null;


    /*Differential Evolution*/
    @Column
    private Boolean de = false;

    /*Differential Evolution params*/
    @Column
    private Double lowerBoundDE = -1.0;
    @Column
    private Double upperBoundDE = 1.0;
    @Column
    private Double mutationFactorDE = 0.4717;
    @Column
    private Double recombinationFactorDE = 0.8803;
    @Column
    private String tags;
    @Column
    private Integer populationDE = 20;

    public Experiment() {
        this.idExpDataTypeList = new ArrayList<>();
        this.idRunList = new ArrayList<>();
    }

    public Experiment(User user, String experimentName, String experimentDescription, Integer generations, Integer populationSize,
                      Integer maxWraps, Integer tournament, Double crossoverProb,
                      Double mutationProb, Integer numCodons,

                      String problem, int depthS, int depthD, String mutationDSGE,
                      String mutationSGE, String crossoverDSGE, String CrossoverSGE, double probChangeDSGE,
                      double probChangeSGE,

                      Integer numberRuns, String objective, Timestamp creationDate, Timestamp modificationDate,
                      Boolean de, Double lowerBoundDE, Double upperBoundDE, Double recombinationFactorDE,
                      Double mutationFactorDE, String tagsText, Integer populationDE) {
        this.userId = user.getId();
        this.experimentName = experimentName;
        this.experimentDescription = experimentDescription;
        this.generations = generations;
        this.populationSize = populationSize;
        this.maxWraps = maxWraps;
        this.tournament = tournament;
        this.crossoverProb = crossoverProb;
        this.mutationProb = mutationProb;
        this.numCodons = numCodons;

        this.problem = problem;
        this.depthS = depthS;
        this.depthD = depthD;
        this.mutationDSGE = mutationDSGE;
        this.mutationSGE = mutationSGE;
        this.crossoverDSGE = crossoverDSGE;
        this.crossoverSGE = CrossoverSGE;
        this.probChangeDSGE = probChangeDSGE;
        this.probChangeSGE = probChangeSGE;

        this.numberRuns = numberRuns;
        this.objective = objective;
        this.idExpDataTypeList = new ArrayList<>();
        this.idRunList = new ArrayList<>();
        this.modificationDate = modificationDate;
        this.creationDate = creationDate;
        this.de = de;
        this.lowerBoundDE = lowerBoundDE;
        this.upperBoundDE = upperBoundDE;
        this.recombinationFactorDE = recombinationFactorDE;
        this.mutationFactorDE = mutationFactorDE;
        this.tags = tagsText;
        this.populationDE = populationDE;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(User user) {
        this.userId = user.getId();
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

    @Override
    public String toString() {
        return String.valueOf(this.id);
    }

    public Experiment copy() {
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
        experiment.objective = this.objective;
        experiment.numCodons = this.numCodons;

        experiment.problem = this.problem;
        experiment.depthS = this.depthS;
        experiment.depthD = this.depthD;
        experiment.mutationDSGE = this.mutationDSGE;
        experiment.mutationSGE = this.mutationSGE;
        experiment.crossoverSGE = this.crossoverSGE;
        experiment.crossoverDSGE = this.crossoverDSGE;
        experiment.probChangeDSGE = this.probChangeDSGE;
        experiment.probChangeSGE = this.probChangeSGE;

        experiment.numberRuns = this.numberRuns;
        experiment.de = this.de;
        experiment.lowerBoundDE = this.lowerBoundDE;
        experiment.upperBoundDE = this.upperBoundDE;
        experiment.recombinationFactorDE = this.recombinationFactorDE;
        experiment.mutationFactorDE = this.mutationFactorDE;
        experiment.tags = this.tags;
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

    public boolean isCrossExperiment() {
        if (crossExperiment == null) {
            crossExperiment = false;
        }
        return crossExperiment;
    }

    public void setCrossExperiment(boolean crossExperiment) {
        this.crossExperiment = crossExperiment;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getPopulationDE() {
        return populationDE;
    }

    public void setPopulationDE(Integer populationDE) {
        this.populationDE = populationDE;
    }
}