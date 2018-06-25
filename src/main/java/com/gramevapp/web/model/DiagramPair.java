package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name="DIAGRAM_PAIR")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@DynamicUpdate
public class DiagramPair
{
    @Id
    @Column(name = "DIAGRAM_PAIR_ID", updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(targetEntity=DiagramData.class,fetch = FetchType.EAGER)
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

    // https://github.com/SomMeri/org.meri.jpa.tutorial/blob/master/src/main/java/org/meri/jpa/relationships/entities/bestpractice/SafeTwitterAccount.java
    public void setDiagramData(DiagramData diagramDataId) {
        //prevent endless loop
        if(sameAs(diagramDataId))
            return ;
        // set new diagramData
        DiagramData oldDiagramData = this.diagramDataId;
        this.diagramDataId = diagramDataId;
        // remove from the old diagram data
        if(oldDiagramData!=null)
            oldDiagramData.removeListPair(this);
        // set myself into new diagram data owner
        if(diagramDataId!=null)
            diagramDataId.addListPair(this);
    }

    private boolean sameAs(DiagramData newDiagramData) {
        return this.diagramDataId==null? newDiagramData == null : diagramDataId.equals(newDiagramData);
    }
}