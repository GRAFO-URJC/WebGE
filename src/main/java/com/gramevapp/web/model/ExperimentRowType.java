package com.gramevapp.web.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import javax.persistence.*;

// idRow - idData - txtFile(one row of the ExperimentDataType file)

@Entity
@Table(name="experimentRowType")
public class ExperimentRowType {
    @Id
    @CsvBindByName(column = "id")
    //@CsvBindByPosition(position = 0)
    @Column(name = "EXPERIMENTROWTYPE_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //@CsvBindByName(column = "expDataTypeId"/*, required = true*/)
    //@CsvBindByPosition(position = 1)
    @ManyToOne(cascade=CascadeType.ALL) // https://www.thoughts-on-java.org/hibernate-tips-map-bidirectional-many-one-association/
    @PrimaryKeyJoinColumn
    private ExperimentDataType expDataTypeId;

    @CsvBindByName(column = "rowString")
    //@CsvBindByPosition(position = 2)
    @Column
    private String rowString;

    public ExperimentRowType() {
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

    public void setExpDataTypeId(ExperimentDataType expDataTypeId) {
        this.expDataTypeId = expDataTypeId;
    }

    public String getRowString() {
        return rowString;
    }

    public void setRowString(String rowString) {
        this.rowString = rowString;
    }
}
