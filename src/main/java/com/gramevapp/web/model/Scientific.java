package com.gramevapp.web.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "scientific")
public class Scientific {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "SCIENTIFIC_ID")
    private Long id;

    @Column
    private String name;

    @Column
    private String surname;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "scientific_experiment",
            joinColumns = { @JoinColumn(name = "scientific_id")},
            inverseJoinColumns = { @JoinColumn(name = "experiment_id") }
    )
    private Set<Experiment> listExperiments;

    public Scientific() {
    }

    public Scientific(String name, String surname) {
        this.name = name;
        this.surname = surname;
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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}