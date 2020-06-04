package com.gramevapp.web.model;

import javax.persistence.*;

@Entity
@Table(name = "run_execution_report")
public class RunExecutionReport {
    @Id
    @Column(name = "run_id")
    private Long id;

    @Column
    private String executionReport;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private Run run;

    public RunExecutionReport(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExecutionReport() {
        return executionReport;
    }

    public void setExecutionReport(String executionReport) {
        this.executionReport = executionReport;
    }
}
