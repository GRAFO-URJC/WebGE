package com.gramevapp.web.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "FILES_UPLOAD")
public class UploadFile {

    @Id
    @Column(name = "FILE_UPLOAD_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator="native") // Efficiency  -> https://vladmihalcea.com/why-should-not-use-the-auto-jpa-generationtype-with-mysql-and-hibernate/
    @GenericGenerator(
            name = "native",
            strategy = "native")
    private long id;

    private String fileName;

    @Lob
    private byte[] data;

    private String bData;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "FILE_NAME")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "FILE_DATA")
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getBData() {
        return bData;
    }

    public void setBData(String bData) {
        this.bData = bData;
    }
}