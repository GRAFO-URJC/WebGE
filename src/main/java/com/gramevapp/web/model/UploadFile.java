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

    @Id
    @Column(name = "UPLOAD_FILE_ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator="native") // Efficiency  -> https://vladmihalcea.com/why-should-not-use-the-auto-jpa-generationtype-with-mysql-and-hibernate/
    @GenericGenerator(
            name = "native",
            strategy = "native")
    private long id;

    private String fileName;

    /*@Lob
    private byte[] data;*/

    private String filePath;

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

    /*@Column(name = "FILE_DATA")
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }*/

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}