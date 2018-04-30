package com.gramevapp.web.model;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import javax.persistence.*;

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

    //@CsvBindByName("Y")
    @CsvBindByPosition(position = 1)
    @Column
    private String y;

    //@CsvBindByName
    @CsvBindByPosition(position = 2)
    @Column
    private String x1;

    //@CsvBindByName
    @CsvBindByPosition(position = 3)
    @Column
    private String x2;

    //@CsvBindByName
    @CsvBindByPosition(position = 4)
    @Column
    private String x3;

    //@CsvBindByName
    @CsvBindByPosition(position = 5)
    @Column
    private String x4;

    //@CsvBindByName
    @CsvBindByPosition(position = 6)
    @Column
    private String x5;

    //@CsvBindByName
    @CsvBindByPosition(position = 7)
    @Column
    private String x6;

    //@CsvBindByName
    @CsvBindByPosition(position = 8)
    @Column
    private String x7;

    //@CsvBindByName
    @CsvBindByPosition(position = 9)
    @Column
    private String x8;

    //@CsvBindByName
    @CsvBindByPosition(position = 10)
    @Column
    private String x9;

    //@CsvBindByName
    @CsvBindByPosition(position = 11)
    @Column
    private String x10;

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

    public String getYCustom() {
        return y;
    }

    public void setYCustom(String yCustom) {
        this.y = yCustom;
    }

    public String getX1() {
        return x1;
    }

    public void setX1(String x1) {
        this.x1 = x1;
    }

    public String getX2() {
        return x2;
    }

    public void setX2(String x2) {
        this.x2 = x2;
    }

    public String getX3() {
        return x3;
    }

    public void setX3(String x3) {
        this.x3 = x3;
    }

    public String getX4() {
        return x4;
    }

    public void setX4(String x4) {
        this.x4 = x4;
    }

    public String getX5() {
        return x5;
    }

    public void setX5(String x5) {
        this.x5 = x5;
    }

    public String getX6() {
        return x6;
    }

    public void setX6(String x6) {
        this.x6 = x6;
    }

    public String getX7() {
        return x7;
    }

    public void setX7(String x7) {
        this.x7 = x7;
    }

    public String getX8() {
        return x8;
    }

    public void setX8(String x8) {
        this.x8 = x8;
    }

    public String getX9() {
        return x9;
    }

    public void setX9(String x9) {
        this.x9 = x9;
    }

    public String getX10() {
        return x10;
    }

    public void setX10(String x10) {
        this.x10 = x10;
    }
}