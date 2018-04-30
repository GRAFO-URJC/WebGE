package com.gramevapp.web.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// idData - userId - idExperimentRowType - name - description - updateDate - type (Validation, Test or Training)
@Entity
@Table(name="experimentDataType")
public class ExperimentDataType {

    @Id
    @Column(name = "EXPERIMENTDATATYPE_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator="native") // Efficiency  -> https://vladmihalcea.com/why-should-not-use-the-auto-jpa-generationtype-with-mysql-and-hibernate/
    @GenericGenerator(
            name = "native",
            strategy = "native")
    private Long id;

    @OneToOne(cascade=CascadeType.ALL)
    private User userId;

    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "expdatatype_list",
            joinColumns = {
                    @JoinColumn(name = "EXPERIMENTDATATYPE_ID", nullable = false)
                },
            inverseJoinColumns = {
                    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
            }
    )
    private Experiment experimentId;

    @Column
    private String dataTypeName;

    @Column
    private String dataTypeDescription; // status

    @Column
    private String dataTypeType;        // Validation, training, test

    // https://softwareyotrasdesvirtudes.com/2012/09/20/anotaciones-en-jpa-para-sobrevivir-a-una-primera-persistenica/
    @Column(name="creationDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = null;

    @Column(name="updateDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate = null;

    // https://www.thoughts-on-java.org/hibernate-tips-map-bidirectional-many-one-association/
    @OneToMany(mappedBy ="expDataTypeId", cascade= CascadeType.ALL)
    private List<ExperimentRowType> listRowsFile;

    public ExperimentDataType(){
        listRowsFile = new ArrayList<>();
        experimentId = new Experiment();
    }

    public ExperimentDataType(User userId, String dataTypeName, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate) {
        this.userId = userId;
        this.dataTypeName = dataTypeName;
        this.dataTypeDescription = dataTypeDescription;
        this.dataTypeType = dataTypeType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.listRowsFile = new ArrayList<>();
        experimentId = new Experiment();
    }

    public ExperimentDataType(User userId, String dataTypeName, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate, List<ExperimentRowType> listRowsFile) {
        this.userId = userId;
        this.dataTypeName = dataTypeName;
        this.dataTypeDescription = dataTypeDescription;
        this.dataTypeType = dataTypeType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.listRowsFile = listRowsFile;
        experimentId = new Experiment();
    }

    public Experiment getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Experiment experimentId) {
        this.experimentId = experimentId;
    }

    public String getDataTypeType() {
        return dataTypeType;
    }

    public List<ExperimentRowType> getListRowsFile() {
        return listRowsFile;
    }

    public void setListRowsFile(List<ExperimentRowType> listRowsFile) {
        this.listRowsFile = listRowsFile;
    }

    public void setDataTypeType(String dataTypeType) {
        this.dataTypeType = dataTypeType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public String getDataTypeDescription() {
        return dataTypeDescription;
    }

    public void setDataTypeDescription(String dataTypeDescription) {
        this.dataTypeDescription = dataTypeDescription;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ExperimentRowType addExperimentRowType(ExperimentRowType exp) {
        this.listRowsFile.add(exp);
        exp.setExpDataTypeId(this);
        return exp;
    }
}