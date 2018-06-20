package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@Entity
@Table(name="DIAGRAM_PAIR")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class DiagramPair
{
    @Id
    @Column(name = "DIAGRAM_PAIR_ID", updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // @JsonIgnore
    // @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
            name = "diagram_pair_list",
            joinColumns = {
                    @JoinColumn(name = "DIAGRAM_PAIR_ID", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "DIAGRAM_DATA_ID", referencedColumnName = "DIAGRAM_DATA_ID")
            }
    )
    private DiagramData diagramDataId;

    @Column
    private double bestIndividual;
    @Column
    private int currentGeneration;

    public DiagramPair() {
    }

    public DiagramPair(double bestIndividual, int currentGeneration) {
        this.bestIndividual = bestIndividual;
        this.currentGeneration = currentGeneration;
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

    public int getCurrentGeneration() {
        return currentGeneration;
    }

    public void setCurrentGeneration(int currentGeneration) {
        this.currentGeneration = currentGeneration;
    }

    public DiagramData getDiagramData() {
        return diagramDataId;
    }

    public void setDiagramData(DiagramData diagramDataId) {
        this.diagramDataId = diagramDataId;
    }
}