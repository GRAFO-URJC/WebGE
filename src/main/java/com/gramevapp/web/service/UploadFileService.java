package com.gramevapp.web.service;

import com.gramevapp.web.model.UploadFile;
import com.gramevapp.web.model.User;
import com.gramevapp.web.repository.UploadFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UploadFileService {

    @Autowired
    private UploadFileRepository uploadFileRepository;

    public UploadFile findUploadFileByUser(User user){
        return uploadFileRepository.findByUserId(user);
    }

    public void saveUploadFile(UploadFile uploadFile){
        uploadFileRepository.save(uploadFile);
    }

}
