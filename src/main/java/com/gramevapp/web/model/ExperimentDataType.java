package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

// idData - userId - idExperimentRowType - name - description - updateDate - type (Validation, Test or Training)
@Entity
@Table(name="experimentDataType")
public class ExperimentDataType {

    @Id
    @Column(name = "EXPERIMENT_DATA_TYPE_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO) /* , generator="native") // Efficiency  -> https://vladmihalcea.com/why-should-not-use-the-auto-jpa-generationtype-with-mysql-and-hibernate/
    @GenericGenerator(
            name = "native",
            strategy = "native")*/
    private Long id;

    @OneToOne
    private User userId;

    @JsonManagedReference
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "exp_data_type_list",
            joinColumns = {
                    @JoinColumn(name = "EXPERIMENT_DATA_TYPE_ID", nullable = false)
                },
            inverseJoinColumns = {
                    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
            }
    )
    private Experiment experimentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "runs_data_type_list",
            joinColumns = {
                    @JoinColumn(name = "EXPERIMENT_DATA_TYPE_ID", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "RUN_ID", referencedColumnName = "RUN_ID")
            }
    )
    private Run runId;

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
    @OneToMany(mappedBy ="expDataTypeId", cascade= CascadeType.ALL)
    private List<ExperimentRowType> listRowsFile;

    public ExperimentDataType(){
        listRowsFile = new ArrayList<>();
        experimentId = new Experiment();
    }

    /**
     * Copy constructor.
     */
    public ExperimentDataType(ExperimentDataType eDType) {
        this(eDType.getUserId(), eDType.getExperimentId(), eDType.getRunId(), eDType.getDataTypeName(), eDType.getDataTypeDescription(), eDType.getDataTypeType(), eDType.getCreationDate(), eDType.getModificationDate(), eDType.getHeader(), eDType.getListRowsFile());
        //no defensive copies are created here, since
        //there are no mutable object fields (String is immutable)
    }

    public ExperimentDataType(User userId, Experiment experimentId, Run runId, String dataTypeName, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate, List<String> header, List<ExperimentRowType> listRowsFile) {
        this.userId = userId;
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

    public ExperimentDataType(User userId, String dataTypeName, String dataTypeDescription, String dataTypeType, Date creationDate, Date modificationDate, ArrayList<ExperimentRowType> listRowsFile) {
        this.userId = userId;
        this.dataTypeName = dataTypeName;
        this.dataTypeDescription = dataTypeDescription;
        this.dataTypeType = dataTypeType;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.listRowsFile = listRowsFile;
        experimentId = new Experiment();
    }

    public Run getRunId() {
        return runId;
    }

    public void setRunId(Run runId) {
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
        this.experimentId = experimentId;
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