package com.gramevapp.web.model;

import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

public class ExperimentDataTypeDto {
    private Long dataTypeId;
    @NotEmpty
    private String dataTypeName;
    @NotEmpty
    private String dataTypeDescription; // status
    @NotEmpty
    private String dataTypeType;        // Validation, test, training

    // https://softwareyotrasdesvirtudes.com/2012/09/20/anotaciones-en-jpa-para-sobrevivir-a-una-primera-persistenica/
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate = null;

    public ExperimentDataTypeDto() {
    }

    public String getDataTypeType() {
        return dataTypeType;
    }

    public void setDataTypeType(String dataTypeType) {
        this.dataTypeType = dataTypeType;
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

    public Long getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(Long dataTypeId) {
        this.dataTypeId = dataTypeId;
    }
}
