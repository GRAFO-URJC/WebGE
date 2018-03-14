package com.gramevapp.web.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name="Experiment")
public class Experiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EXPERIMENT_ID")
    private Long id;

    @Column
    private String name;

    @ManyToMany(mappedBy = "listExperiments")
    private Set<Scientific> listScientifics;

    public Experiment() {
    }

    public Experiment(Long id, String name, Set<Scientific> listScientifics){
        this.id = id;
        this.name = name;
        this.listScientifics = listScientifics;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Scientific> getListScientific() {
        return listScientifics;
    }

    public void setListScientific(Set<Scientific> listScientific) {
        this.listScientifics = listScientific;
    }

    @Override
    public String toString() {
        return "Experiment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", listScientific=" + listScientifics +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Experiment that = (Experiment) o;

        if (id != that.id) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return listScientifics != null ? listScientifics.equals(that.listScientifics) : that.listScientifics == null;
    }
}