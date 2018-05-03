package com.gramevapp.web.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name="grammar")
public class Grammar {

    @Id
    @Column(name = "GRAMMAR_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator="native") // Efficiency  -> https://vladmihalcea.com/why-should-not-use-the-auto-jpa-generationtype-with-mysql-and-hibernate/
    @GenericGenerator(
            name = "native",
            strategy = "native")
    private Long id;

    @OneToOne(cascade=CascadeType.ALL)
    private User userId;

    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "grammar_list",
            joinColumns = {
                    @JoinColumn(name = "GRAMMAR_ID", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
            }
    )
    private Experiment experimentId;

    @Column
    private String grammarName;

    @Column
    private String grammarDescription;

    //@Lob
    //@Column(name="CONTENT", length=512)
    @Column(columnDefinition = "TEXT") // https://stackoverflow.com/questions/31833337/hibernate-could-not-execute-statement-sql-n-a-saving-nested-object
    private String fileText; // This is the text on the file - That's written in a areaText - So we can take it as a String

    public Grammar(){
        experimentId = new Experiment();
    }

    public Grammar(User userId, String grammarName, String grammarDescription, String fileText) {
        this.userId = userId;
        this.grammarName = grammarName;
        this.grammarDescription = grammarDescription;
        this.fileText = fileText;

        experimentId = new Experiment();
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

    public void setExperimentId(Experiment experimentId) {
        this.experimentId = experimentId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
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
}
