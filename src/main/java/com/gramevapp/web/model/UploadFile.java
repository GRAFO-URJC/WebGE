package com.gramevapp.web.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "UPLOAD_FILE")
public class UploadFile implements Serializable {

    @Id
    @Column(name = "UPLOAD_FILE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String fileName;

    private String filePath;

    public UploadFile() {
        /*Do nothing*/
    }

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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}