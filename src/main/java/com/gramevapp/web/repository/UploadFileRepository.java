package com.gramevapp.web.repository;

import com.gramevapp.web.model.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
    // UploadFile findByUserId(User user);

}
