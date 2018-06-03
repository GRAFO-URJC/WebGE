package com.gramevapp.web.model;

import javax.persistence.*;
import java.util.List;

// idRow - idData - txtFile(one row of the ExperimentDataType file)

@Entity
@Table(name="experimentRowType")
public class ExperimentRowType {
    @Id
    //@CsvBindByName(column = "id")
    //@CsvBindByPosition(position = 0)
    @Column(name = "EXPERIMENTROWTYPE_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //@CsvBindByName(column = "expDataTypeId"/*, required = true*/)
    //@CsvBindByPosition(position = 1)
    @ManyToOne(cascade=CascadeType.ALL) // https://www.thoughts-on-java.org/hibernate-tips-map-bidirectional-many-one-association/
    @PrimaryKeyJoinColumn
    private ExperimentDataType expDataTypeId;

    @ElementCollection
    @CollectionTable(name="COLUMN_LIST", joinColumns=@JoinColumn(name="EXPERIMENTROWTYPE_ID"))
    @Column(name = "COLUMNS")
    private List<String> columnList;

    public ExperimentRowType() {
    }

    public List<String> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<String> columnList) {
        this.columnList = columnList;
    }

/*public List<Attributes> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Attributes> columnList) {
        this.columnList = columnList;
    }*/

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


    /*@Override
    public String toString() {
        return  y + ";" + x1 + ";" + x2 + ";" + x3 + ";" + x4 + ";" + x5 + ";" + x6 + ";" + x7 + ";" + x8 + ";" + x9 + ";" + x10 + "\n";
    }*/

    public class Attributes{
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

