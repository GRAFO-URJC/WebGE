package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "grammar")
@DynamicUpdate
public class Grammar {

    @Id
    @Column(name = "GRAMMAR_ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "grammar_list",
            joinColumns = {
                    @JoinColumn(name = "GRAMMAR_ID")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
            }
    )
    private List<Experiment> listExperiment=new ArrayList<>();

    @Column
    private Long runId;

    @Column
    private String grammarName;

    @Column
    private String grammarDescription;

    @Column(name = "creationDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = null;

    @Column(columnDefinition = "TEXT")
    // https://stackoverflow.com/questions/31833337/hibernate-could-not-execute-statement-sql-n-a-saving-nested-object
    private String fileText; // This is the text on the file - That's written in a areaText - So we can take it as a String

    @Column(name = "user_id")
    private Long userId;

    public Grammar(List<Experiment> exp) {
        this.listExperiment = exp;
    }

    /**
     * Copy constructor.
     */
    public Grammar(Grammar grammar) {
        this(grammar.getListExperiment(), grammar.getRunId(), grammar.getGrammarName(), grammar.getGrammarDescription(), grammar.getFileText(), grammar.getCreationDate());
        //no defensive copies are created here, since
        //there are no mutable object fields (String is immutable)
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Grammar() {
    }

    public Grammar(List<Experiment> experimentId, Long runId, String grammarName, String grammarDescription, String fileText, Date creationDate) {
        this.listExperiment = experimentId;
        this.runId = runId;
        this.grammarName = grammarName;
        this.grammarDescription = grammarDescription;
        this.fileText = fileText;
        this.creationDate = creationDate;
    }

    public Grammar(List<Experiment> exp, String grammarName, String grammarDescription, String fileText) {
        this.grammarName = grammarName;
        this.grammarDescription = grammarDescription;
        this.fileText = fileText;

        this.listExperiment = exp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Experiment> getListExperiment() {
        return listExperiment;
    }

    public void deleteExperimentId(Experiment experimentId) {
        if (listExperiment.contains(experimentId))
            this.listExperiment.remove(experimentId);
    }

    public void addExperimentId(Experiment experimentId) {
        if (!listExperiment.contains(experimentId))
            this.listExperiment.add(experimentId);
    }

    private boolean sameAs(Experiment newExperiment) {
        return this.listExperiment == null ? newExperiment == null : listExperiment.contains(newExperiment);
    }

    public String getGrammarName() {
        return grammarName;
    }

    public void setGrammarName(String grammarName) {
        this.grammarName = grammarName;
    }

    public String getGrammarDescription() {
        return grammarDescription;
    }

    public void setGrammarDescription(String grammarDescription) {
        this.grammarDescription = grammarDescription;
    }

    public String getFileText() {
        return fileText;
    }

    public void setFileText(String fileText) {
        this.fileText = fileText;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
