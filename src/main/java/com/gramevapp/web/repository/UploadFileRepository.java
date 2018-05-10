package com.gramevapp.web.repository;

import com.gramevapp.web.model.Grammar;
import com.gramevapp.web.model.UploadFile;
import com.gramevapp.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadFileRepository extends JpaRepository<Grammar, Long> {
    void save(UploadFile uploadFile);

    UploadFile findByUserId(User user);

}
