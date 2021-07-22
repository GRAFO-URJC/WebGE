package com.gramevapp.web.model;

import com.gramevapp.web.other.DateFormat;
import java.sql.Timestamp;

/**
 * This class is devoted to follow the advice avoid persistent entities
 * to be used as arguments of "@RequestMapping" methods, since this is a vulnerability.
 */
public class DatasetDTO {

    private String dataTypeName;
    private String info;
    private String dataTypeDescription;
    private String dataTypeType;        // Validation, training, test
    private Long userIdUserId;
    private Integer foldSize = 0;
    private Timestamp creationDate = null;

    public DatasetDTO() {
        //Empty constructor
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