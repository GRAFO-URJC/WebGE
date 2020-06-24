package com.gramevapp.web.model;

import com.gramevapp.web.other.DateFormat;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "dataset")
@DynamicUpdate
public class Dataset {

    @Id
    @Column(name = "experimentdatatype_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

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

    @Column(name = "fold_size")
    private Integer foldSize = 0;

    // https://softwareyotrasdesvirtudes.com/2012/09/20/anotaciones-en-jpa-para-sobrevivir-a-una-primera-persistenica/
    @Column(name = "creation_date")
    private Timestamp creationDate = null;

    public Dataset() {

    }


    public String getDataTypeType() {
        return dataTypeType;
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

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Long getUserIdUserId() {
        return userIdUserId;
    }

    public void setUserIdUserId(Long userIdUserId) {
        this.userIdUserId = userIdUserId;
    }

    public String getCreationDateFormated() {
        return DateFormat.formatDate(creationDate);
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getFoldSize() {
        if (foldSize == null) {
            this.foldSize = 0;
        }
        return foldSize;
    }

    public void setFoldSize(int foldSize) {
        this.foldSize = foldSize;
    }
}