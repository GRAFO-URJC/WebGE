package com.gramevapp.web.model;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.InputStream;

public class FileModelDto {

    @NotNull
    @Size(min = 1)
    MultipartFile typeFile = new MultipartFile() {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getOriginalFilename() {
            return null;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public byte[] getBytes() {
            return new byte[0];
        }

        @Override
        public InputStream getInputStream() {
            return null;
        }

        @Override
        public void transferTo(File file){
            //Transfer to file
        }
    };

    public MultipartFile getTypeFile() {
        return typeFile;
    }

    public void setTypeFile(MultipartFile typeFile) {
        this.typeFile = typeFile;
    }
}