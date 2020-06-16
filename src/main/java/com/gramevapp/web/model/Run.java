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
    private Status status;

    @Column(name = "ini_date")
    private Timestamp iniDate;

    @Column(name = "modification_date")
    private Timestamp modificationDate;

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

    public DiagramData getDiagramData() {
        return diagramData;
    }

    public void setDiagramData(DiagramData diagramData) {
        this.diagramData = diagramData;
    }
}