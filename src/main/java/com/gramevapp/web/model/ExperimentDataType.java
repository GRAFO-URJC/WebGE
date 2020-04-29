package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Entity
@Table(name = "experiment_data_type")
@DynamicUpdate
public class ExperimentDataType {

    @Id
    @Column(name = "experimentdatatype_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonManagedReference
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exp_data_type_list",
            joinColumns = {
                    @JoinColumn(name = "experimentdatatype_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "experiment_id", referencedColumnName = "experiment_id")
            }
    )
    private List<Experiment> experimentList;

    @Column
    private Long runId;

    @Column
    private String dataTypeName;

    @Column
    private String info;

    @Column
    private String dataTypeDescription;

    @Column
    private String dataTypeType;        // Validation, training, test

    @Column
    private Long userIdUserId;

    // https://softwareyotrasdesvirtudes.com/2012/09/20/anotaciones-en-jpa-para-sobrevivir-a-una-primera-persistenica/
    @Column(name = "creationDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = null;

    @Column(name = "updateDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate = null;

    @ElementCollection
    private List<String> header;

    // https://www.thoughts-on-java.org/hibernate-tips-map-bidirectional-many-one-association/
    @GeneratedValue(strategy = GenerationType.AUTO)
    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "expDataTypeId")
    private List<ExperimentRowType> listRowsFile;

    public ExperimentDataType(Experiment exp) {
        listRowsFile = new ArrayList<>();
        this.experimentList = new ArrayList<>();
        experimentList.add(exp);
    }

    /**
     * Copy constructor.
     */
    public ExperimentDataType(ExperimentDataType eDType) {
        this(eDType.getExperimentList(), eDType.getRunId(), eDType.getDataTypeName(), eDType.getinfo(), eDType.getDataTypeDescription(), eDType.getDataTypeType(), eDType.getCreationDate(), eDType.getModificationDate(), eDType.getHeader(), eDType.getListRowsFile());
    }

    public ExperimentDataType() {
        this.listRowsFile = new ArrayList<>();
    }

    public ExperimentDataType(List<Experiment> experimentId, Long runId, String dataTypeName, String info, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate, List<String> header, List<ExperimentRowType> listRowsFile) {
        this.experimentList = experimentId;
        this.runId = runId;
        this.dataTypeName = dataTypeName;
        this.info = info;
        this.dataTypeDescription = dataTypeDescription;
        this.dataTypeType = dataTypeType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.header = header;
        this.listRowsFile = listRowsFile;
    }

    public ExperimentDataType(List<Experiment> exp, String dataTypeName, String info, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate) {
        this.dataTypeName = dataTypeName;
        this.info = info;
        this.dataTypeDescription = dataTypeDescription;
        this.dataTypeType = dataTypeType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.listRowsFile = new ArrayList<>();
        this.experimentList = exp;
    }

    public ExperimentDataType(String dataTypeName, String info, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate, ArrayList<ExperimentRowType> listRowsFile) {
        this.dataTypeName = dataTypeName;
        this.info = info;
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

    public Experiment getExperiment(Experiment experiment) {
        return experimentList.contains(experiment) ?
                experimentList.get(experimentList.indexOf(experiment)) : null;
    }

    public List<Experiment> getExperimentList() {
        return experimentList;
    }

    public void deleteExperimentInList(Experiment experimentId) {
        if(experimentList.contains(experimentId)){
            experimentList.remove(experimentId);
        }
    }

    public void deleteExperimentList() {
        this.experimentList = new ArrayList<>();
    }

    public void addExperimentList(Experiment experimentId) {
        if(!experimentList.contains(experimentId)){
            experimentList.add(experimentId);
        }
    }

    private boolean sameAs(Experiment newExperiment) {
        return this.experimentList == null ? newExperiment == null : experimentList.contains(newExperiment);
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

    public String getinfo() {
        return info;
    }

    public void setinfo(String info) {
        this.info = info;
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
        if (this.listRowsFile.contains(expRowType))
            return;
        this.listRowsFile.add(expRowType);
        expRowType.setExpDataTypeId(this);
    }

    public void removeExperimentRowType(ExperimentRowType expRowType) {
        if (!listRowsFile.contains(expRowType))
            return;
        listRowsFile.remove(expRowType);
        expRowType.setExpDataTypeId(null);
    }

    public String headerToString() {
        StringBuilder stringBuilder = new StringBuilder();

        Iterator<String> it = this.header.iterator();

        while (it.hasNext()) {
            String column = it.next();
            if (!it.hasNext())
                stringBuilder.append(column + "\n");
            else
                stringBuilder.append(column + ";");
        }

        return stringBuilder.toString();
    }

    public Long getUserIdUserId() {
        return userIdUserId;
    }

    public void setUserIdUserId(Long userIdUserId) {
        this.userIdUserId = userIdUserId;
    }

}