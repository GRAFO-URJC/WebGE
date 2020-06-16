package com.gramevapp.web.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToMany(fetch = FetchType.EAGER,
            cascade = CascadeType.ALL,
            mappedBy = "diagramDataId",
            orphanRemoval = true,
            targetEntity = DiagramPair.class)
    private List<DiagramPair> listPair;   // To display diagram with one click.

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
        this.listPair = new ArrayList<>();
    }

    public DiagramData(Integer currentGeneration, double bestIndividual, Run runId) {
        this.bestIndividual = bestIndividual;
        this.runId = runId;
        this.currentGeneration = currentGeneration;

        this.listPair = new ArrayList<>();
        DiagramPair diagramPair = new DiagramPair();
        diagramPair.setBestIndividual(bestIndividual);
        diagramPair.setCurrentGeneration(currentGeneration);
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
            run.setDiagramData(this);
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

    public List<DiagramPair> getListPair() {
        return listPair;
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

    public void addListPair(double bestIndividual, int currentGeneration) {
        DiagramPair diagramPair = new DiagramPair(bestIndividual, currentGeneration);

        this.listPair.add(diagramPair);
    }

    //https://github.com/SomMeri/org.meri.jpa.tutorial/blob/master/src/main/java/org/meri/jpa/relationships/entities/bestpractice/SafePerson.java
    public void addListPair(DiagramPair diagramPair) {
        //prevent endless loop
        if (this.listPair.contains(diagramPair))
            return;
        this.listPair.add(diagramPair);
        diagramPair.setDiagramData(this);
    }

    public void setListPair(List<DiagramPair> listPair) {
        this.listPair = listPair;
    }

    public void removeListPair(DiagramPair diagramPair) {
        //prevent endless loop
        if (!listPair.contains(diagramPair))
            return;
        //remove the account
        listPair.remove(diagramPair);
        //remove myself from the twitter account
        diagramPair.setDiagramData(null);
    }
}