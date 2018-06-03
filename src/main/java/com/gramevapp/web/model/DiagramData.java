package com.gramevapp.web.model;


import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Calendar;

@Entity
public class DiagramData {

    @Id
    @Column(name = "DIAGRAM_DATA_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Double bestIndividual;

    @Column
    private Long longRunId;

    @Column
    private Long longUserId;

    @Column
    private Integer currentGeneration;

    @Column
    private Boolean finished = false;

    Calendar calendar = Calendar.getInstance();

    @Column(name="time")
    // @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="HH:mm:ss")
    java.sql.Timestamp time = new java.sql.Timestamp(calendar.getTime().getTime());

    public DiagramData() {
    }

    public DiagramData(Double bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    public DiagramData(Long longRunId, Long longUserId) {
        this.longRunId = longRunId;
        this.longUserId = longUserId;
    }

    public DiagramData(Integer currentGeneration, Double bestIndividual, Long longRunId, Long longUserId) {
        this.bestIndividual = bestIndividual;
        this.longRunId = longRunId;
        this.longUserId = longUserId;
        this.currentGeneration = currentGeneration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getBestIndividual() {
        return bestIndividual;
    }

    public void setBestIndividual(Double bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    public Long getLongRunId() {
        return longRunId;
    }

    public void setLongRunId(Long longRunId) {
        this.longRunId = longRunId;
    }

    public Long getLongUserId() {
        return longUserId;
    }

    public void setLongUserId(Long longUserId) {
        this.longUserId = longUserId;
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

    //@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="HH:mm:ss")
    public java.sql.Timestamp getTime() {
        return time;
    }

    public void setTime(java.sql.Timestamp time) {
        this.time = time;
    }
}
