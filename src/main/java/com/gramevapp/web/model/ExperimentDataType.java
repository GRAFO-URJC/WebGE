package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Entity
@Table(name="experimentDataType")
@DynamicUpdate
public class ExperimentDataType {

    @Id
    @Column(name = "EXPERIMENT_DATA_TYPE_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exp_data_type_list",
            joinColumns = {
                    @JoinColumn(name = "EXPERIMENT_DATA_TYPE_ID")
                },
            inverseJoinColumns = {
                    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
            }
    )
    private Experiment experimentId;

    @Column
    private Long runId;

    @Column
    private String dataTypeName;

    @Column
    private String dataTypeDescription;

    @Column
    private String dataTypeType;        // Validation, training, test

    // https://softwareyotrasdesvirtudes.com/2012/09/20/anotaciones-en-jpa-para-sobrevivir-a-una-primera-persistenica/
    @Column(name="creationDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = null;

    @Column(name="updateDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate = null;

    @ElementCollection
    private List<String> header;

    // https://www.thoughts-on-java.org/hibernate-tips-map-bidirectional-many-one-association/
    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToMany(cascade= CascadeType.ALL,
            fetch=FetchType.LAZY,
            mappedBy ="expDataTypeId")
    private List<ExperimentRowType> listRowsFile;

    public ExperimentDataType(Experiment exp){
        listRowsFile = new ArrayList<>();
        this.experimentId = exp;
    }

    /**
     * Copy constructor.
     */
    public ExperimentDataType(ExperimentDataType eDType) {
        this(eDType.getExperimentId(), eDType.getRunId(), eDType.getDataTypeName(), eDType.getDataTypeDescription(), eDType.getDataTypeType(), eDType.getCreationDate(), eDType.getModificationDate(), eDType.getHeader(), eDType.getListRowsFile());
    }

    public ExperimentDataType(){
        this.listRowsFile = new ArrayList<>();
    }

    public ExperimentDataType(Experiment experimentId, Long runId, String dataTypeName, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate, List<String> header, List<ExperimentRowType> listRowsFile) {
        this.experimentId = experimentId;
        this.runId = runId;
        this.dataTypeName = dataTypeName;
        this.dataTypeDescription = dataTypeDescription;
        this.dataTypeType = dataTypeType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.header = header;
        this.listRowsFile = listRowsFile;
    }

    public ExperimentDataType(Experiment exp, String dataTypeName, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate) {
        this.dataTypeName = dataTypeName;
        this.dataTypeDescription = dataTypeDescription;
        this.dataTypeType = dataTypeType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.listRowsFile = new ArrayList<>();
        this.experimentId = exp;
    }

    public ExperimentDataType(String dataTypeName, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate, ArrayList<ExperimentRowType> listRowsFile) {
        this.dataTypeName = dataTypeName;
        this.dataTypeDescription = dataTypeDescription;
        this.dataTypeType = dataTypeType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.listRowsFile = listRowsFile;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public List<String> getHeader() {
        return header;
    }

    public void setHeader(ArrayList<String> header) {
        this.header = header;
    }

    public Experiment getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Experiment experimentId) {
        if(sameAs(experimentId))
            return ;
        Experiment oldExperimentId = this.experimentId;
        this.experimentId = experimentId;

        if(oldExperimentId!=null)
            oldExperimentId.removeExperimentDataType(this);
        if(experimentId!=null)
            experimentId.addExperimentDataType(this);

        this.experimentId = experimentId;
    }

    private boolean sameAs(Experiment newExperiment) {
        return this.experimentId==null? newExperiment == null : experimentId.equals(newExperiment);
    }

    public String getDataTypeType() {
        return dataTypeType;
    }

    public List<ExperimentRowType> getListRowsFile() {
        return listRowsFile;
    }

    public void setListRowsFile(ArrayList<ExperimentRowType> listRowsFile) {
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

    // https://github.com/SomMeri/org.meri.jpa.tutorial/blob/master/src/main/java/org/meri/jpa/relationships/entities/bestpractice/SafePerson.java
    public void addExperimentRowType(ExperimentRowType expRowType) {
        if(this.listRowsFile.contains(expRowType))
            return ;
        this.listRowsFile.add(expRowType);
        expRowType.setExpDataTypeId(this);
    }

    public void removeExperimentRowType(ExperimentRowType expRowType){
        if(!listRowsFile.contains(expRowType))
            return ;
        listRowsFile.remove(expRowType);
        expRowType.setExpDataTypeId(null);
    }

    public String headerToString() {
        StringBuilder stringBuilder = new StringBuilder();

        Iterator<String> it = this.header.iterator();

        while(it.hasNext()){
            String column = it.next();
            if(!it.hasNext())
                stringBuilder.append(column + "\n");
            else
                stringBuilder.append(column + ";");
        }

        return stringBuilder.toString();
    }
}