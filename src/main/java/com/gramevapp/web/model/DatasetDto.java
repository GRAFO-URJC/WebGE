package com.gramevapp.web.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class DatasetDto {
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

    public DatasetDto() {
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

    public Long getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(Long dataTypeId) {
        this.dataTypeId = dataTypeId;
    }
}
