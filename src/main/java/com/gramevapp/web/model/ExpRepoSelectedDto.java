package com.gramevapp.web.model;

public class ExpRepoSelectedDto {
    private Long id;
    private String name;
    private String description;
    //private String grammarName;
    //private String expDataTypeName;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*public String getGrammarName() {
        return grammarName;
    }

    public void setGrammarName(String grammarName) {
        this.grammarName = grammarName;
    }

    public String getExpDataTypeName() {
        return expDataTypeName;
    }

    public void setExpDataTypeName(String expDataTypeName) {
        this.expDataTypeName = expDataTypeName;
    }*/
}
