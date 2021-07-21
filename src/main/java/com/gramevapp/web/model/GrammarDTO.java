package com.gramevapp.web.model;

import com.gramevapp.web.other.DateFormat;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * This class is devoted to follow the advice avoid persistent entities
 * to be used as arguments of "@RequestMapping" methods, since this is a vulnerability.
 */
public class GrammarDTO {

    private String grammarName;
    private String grammarDescription;
    private Timestamp creationDate = null;
    private String fileText; // This is the text on the file - That's written in a areaText - So we can take it as a String
    private Long userId;

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public GrammarDTO() {
    }

    public GrammarDTO(String grammarName, String grammarDescription, String fileText) {
        this.creationDate = new Timestamp(new Date().getTime());
        this.grammarName=grammarName;
        this.grammarDescription=grammarDescription;
        this.fileText=fileText;
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
