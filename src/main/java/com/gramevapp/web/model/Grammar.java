package com.gramevapp.web.model;

import com.gramevapp.web.other.DateFormat;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "grammar")
@DynamicUpdate
public class Grammar {

    @Id
    @Column(name = "GRAMMAR_ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String grammarName;

    @Column
    private String grammarDescription;

    @Column(name = "creation_date")
    private Timestamp creationDate = null;

    @Column(columnDefinition = "TEXT")
    // https://stackoverflow.com/questions/31833337/hibernate-could-not-execute-statement-sql-n-a-saving-nested-object
    private String fileText; // This is the text on the file - That's written in a areaText - So we can take it as a String

    @Column(name = "user_id")
    private Long userId;

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public Grammar() {
    }

    public Grammar(String grammarName, String grammarDescription, String fileText) {
        this.creationDate = new Timestamp(System.currentTimeMillis());
        this.grammarName=grammarName;
        this.grammarDescription=grammarDescription;
        this.fileText=fileText;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCreationDateFormated() {
        return DateFormat.formatDate(creationDate);
    }
}
