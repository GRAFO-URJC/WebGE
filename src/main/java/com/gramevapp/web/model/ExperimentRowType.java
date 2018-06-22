package com.gramevapp.web.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Entity
@Table(name="experimentRowType")
public class ExperimentRowType {
    @Id
    @Column(name = "EXPERIMENTROWTYPE_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // https://www.thoughts-on-java.org/hibernate-tips-map-bidirectional-many-one-association/
    @PrimaryKeyJoinColumn
    private ExperimentDataType expDataTypeId;

    private ArrayList<String> dataRow;

    public ExperimentRowType() {
    }

    public ArrayList<String> getDataRow() {
        return dataRow;
    }

    public void setDataRow(ArrayList<String> dataRow) {
        this.dataRow = dataRow;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExperimentDataType getExpDataTypeId() {
        return expDataTypeId;
    }

    public void setExpDataTypeId(ExperimentDataType expDataType) {
        if(sameAs(expDataType))
            return ;
        ExperimentDataType oldExperimentDataType = this.expDataTypeId;
        this.expDataTypeId = expDataType;

        if(oldExperimentDataType!=null)
            oldExperimentDataType.removeExperimentRowType(this);
        if(expDataTypeId!=null)
            expDataTypeId.addExperimentRowType(this);

        this.expDataTypeId = expDataType;
    }

    private boolean sameAs(ExperimentDataType newExpDataType) {
        return this.expDataTypeId==null? newExpDataType == null : expDataTypeId.equals(newExpDataType);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        Iterator<String> it = this.dataRow.iterator();

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