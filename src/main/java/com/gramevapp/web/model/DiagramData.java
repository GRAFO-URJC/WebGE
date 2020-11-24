package com.gramevapp.web.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "diagram_data")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@DynamicUpdate
public class DiagramData {

    @Id
    @Column(name = "diagram_data_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Double bestIndividual = 0.0;  // Best solution


    @OneToOne(targetEntity = Run.class, fetch = FetchType.LAZY)
    private Run runId;

    @Column
    private Integer currentGeneration = 0;

    @Column
    private Boolean finished = false;

    @Column
    private Boolean stopped = false;

    @Column
    private Boolean failed = false;

    public DiagramData() {
        //Empty constructor
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getBestIndividual() {
        return bestIndividual;
    }

    public void setBestIndividual(double bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    public Run getRunId() {
        return runId;
    }

    public void setRunId(Run run) {
        this.runId = run;
        if (run != null)
            run.addDiagramData(this);
    }

    public Integer getCurrentGeneration() {
        return currentGeneration;
    }

    public void setCurrentGeneration(Integer currentGeneration) {
        this.currentGeneration = currentGeneration;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public void setBestIndividual(Double bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    public Boolean getStopped() {
        return stopped;
    }

    public void setStopped(Boolean stopped) {
        this.stopped = stopped;
    }

    public Boolean getFailed() {
        return failed;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
    }

}