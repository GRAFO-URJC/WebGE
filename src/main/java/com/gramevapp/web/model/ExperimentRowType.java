package com.gramevapp.web.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    @ManyToOne // https://www.thoughts-on-java.org/hibernate-tips-map-bidirectional-many-one-association/
    @PrimaryKeyJoinColumn
    private ExperimentDataType expDataTypeId;

    private ArrayList<String> dataRow;

    /*@ElementCollection
    @CollectionTable(name="COLUMN_LIST", joinColumns=@JoinColumn(name="EXPERIMENTROWTYPE_ID"))
    @Column(name = "COLUMNS")
    private List<String> columnList;*/

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

    public void setExpDataTypeId(ExperimentDataType expDataTypeId) {
        this.expDataTypeId = expDataTypeId;
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