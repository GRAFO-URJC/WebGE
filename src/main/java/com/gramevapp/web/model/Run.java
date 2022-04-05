package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gramevapp.web.other.DateFormat;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "run")
@DynamicUpdate
public class Run {
    public enum Status {INITIALIZING, WAITING, RUNNING, STOPPED, FINISHED, FAILED, CANCELLED}


    @Id
    @Column(name = "RUN_ID", updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Experiment experimentId;

    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "runId",fetch = FetchType.EAGER)
    @OrderBy("current_generation ASC")
    private List<DiagramData> diagramData = new ArrayList<>();

    /*@Column
    private Long threaId;*/

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

    @Column
    private String execReport = "";

    public Run() {
        /*Do nothing*/
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

    /*public Long getThreaId() {
        return threaId;
    }

    public void setThreaId(Long threaId) {
        this.threaId = threaId;
    }*/

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public DiagramData getDiagramData() {
        if (diagramData.isEmpty()) {
            return null;
        }
        return diagramData.get(diagramData.size() - 1);
    }

    public List<DiagramData> getDiagramDataList() {
        return diagramData;
    }

    public void addDiagramData(DiagramData diagramData) {
        this.diagramData.add(diagramData);
    }

    public String getExecReport() {
        return execReport;
    }

    public void setExecReport(String execReport) {
        this.execReport = execReport;
    }
}