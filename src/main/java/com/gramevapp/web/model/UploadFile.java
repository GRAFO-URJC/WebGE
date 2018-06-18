package com.gramevapp.web.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Entity
@Table(name = "UPLOAD_FILE")
public class UploadFile {
    private final String DEFAULT_PROFILE_PIC_PATH = "."+File.separator+"main"+File.separator+"resources"+File.separator+"static"+File.separator+"images"+File.separator+"index"+File.separator+"profile_default.png";

    @Id
    @Column(name = "UPLOAD_FILE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String fileName;

    private String filePath;

    public UploadFile() {
        this.fileName = "Default profile picture";
        this.filePath = DEFAULT_PROFILE_PIC_PATH;
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