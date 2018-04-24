package com.gramevapp.web.model;

public class GrammarDto {

    private String grammarName;

    private String grammarDescription;

    private String fileText; // This is the text on the file - That's written in an areaText - So we can take it as a String

    public GrammarDto() {
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