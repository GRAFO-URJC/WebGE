package com.gramevapp.web.model;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

public class ExperimentDataTypeDto {
    public enum Type {validation, test, training}

    private Long dataTypeId;
    @NotNull
    @Size(min = 1)
    private String dataTypeName;
    @NotNull
    @Size(min = 1)
    private String info;
    @NotNull
    @Size(min = 1)
    private String dataTypeDescription; // status
    @NotNull
    @Size(min = 1)
    private Type dataTypeType;        // Validation, test, training

    // https://softwareyotrasdesvirtudes.com/2012/09/20/anotaciones-en-jpa-para-sobrevivir-a-una-primera-persistenica/
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate = null;

    public ExperimentDataTypeDto() {
    }

    public Type getDataTypeType() {
        return dataTypeType;
    }

    public void setDataTypeType(Type dataTypeType) {
        this.dataTypeType = dataTypeType;
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

    public Long getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(Long dataTypeId) {
        this.dataTypeId = dataTypeId;
    }
}
