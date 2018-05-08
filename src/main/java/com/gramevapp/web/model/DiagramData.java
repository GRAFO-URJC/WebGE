package com.gramevapp.web.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class DiagramData {

    @Id
    @Column(name = "DIAGRAM_DATA_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator="native") // Efficiency  -> https://vladmihalcea.com/why-should-not-use-the-auto-jpa-generationtype-with-mysql-and-hibernate/
    @GenericGenerator(
            name = "native",
            strategy = "native")
    private Long id;

    private Double bestIndividual;

    @OneToOne(cascade=CascadeType.ALL)
    private Run runId;

    @OneToOne(cascade=CascadeType.ALL)
    private User userId;

    public DiagramData() {
    }

    public DiagramData(Double bestIndividual) {
        this.bestIndividual = bestIndividual;
    }

    public DiagramData(User userId, Run runId) {
        this.userId = userId;
        this.runId = runId;
    }

    public DiagramData(Double bestIndividual, User userId, Run runId) {
        this.bestIndividual = bestIndividual;
        this.userId = userId;
        this.runId = runId;
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

    public Run getRunId() {
        return runId;
    }

    public void setRunId(Run runId) {
        this.runId = runId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }
}
