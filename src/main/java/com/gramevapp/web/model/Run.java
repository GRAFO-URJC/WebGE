package com.gramevapp.web.model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "run")
public class Run
{
    public enum Status { INITIALIZING, RUNNING, PAUSED, CANCELED, FINISHED, FAILED; };

    @Id
    @Column(name="RUN_ID", updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(cascade=CascadeType.ALL)
    private User userId;

    //@JoinColumn(name = "EXPERIMENT_ID", unique = true)
    //@OneToOne(cascade=CascadeType.ALL)
    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="EXPERIENT_ID", nullable = false)*/
    /*@ManyToOne(cascade=CascadeType.ALL)  // https://www.thoughts-on-java.org/hibernate-tips-map-bidirectional-many-one-association/
    @JoinColumn(name = "EXPERIMENT_ID")*/
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
            name = "runs_list",
            joinColumns = {
                    @JoinColumn(name = "RUN_ID", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "EXPERIMENT_ID", referencedColumnName = "EXPERIMENT_ID")
            }
    )
    private Experiment experimentId;

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "default_grammar")
    private DiagramData diagramData;

    @Column
    private Long idProperties;

    @Column
    private Status status;
    @Column
    private String runName;
    @Column
    private String runDescription;

    @Column(name="iniDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date iniDate;

    @Column(name="endDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDate;

    public Run() {
    }

    public Run(User userId, Status status) {
        // DATE TIMESTAMP
        Calendar calendar = Calendar.getInstance();
        java.sql.Date currentTimestamp = new java.sql.Date(calendar.getTime().getTime());

        this.iniDate = currentTimestamp;
        this.lastDate = currentTimestamp;
        this.userId = userId;
        this.status = status;
    }

    public Run(User userId, Status status, String runName, String runDescription, Date iniDate, Date lastDate) {
        this.userId = userId;
        this.runName = runName;
        this.runDescription = runDescription;
        this.status = status;
        this.iniDate = iniDate;
        this.lastDate = lastDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Experiment getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Experiment experimentId) {
        this.experimentId = experimentId;
    }

    public Date getIniDate() {
        return iniDate;
    }

    public void setIniDate(Date iniDate) {
        this.iniDate = iniDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public String getRunName() {
        return runName;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public String getRunDescription() {
        return runDescription;
    }

    public void setRunDescription(String runDescription) {
        this.runDescription = runDescription;
    }

    public Long getIdProperties() {
        return idProperties;
    }

    public void setIdProperties(Long idProperties) {
        this.idProperties = idProperties;
    }
}
