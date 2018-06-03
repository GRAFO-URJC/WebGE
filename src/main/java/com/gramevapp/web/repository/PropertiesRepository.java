package com.gramevapp.web.repository;

import com.gramevapp.web.model.ExpProperties;
import com.gramevapp.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertiesRepository extends JpaRepository<ExpProperties, Long> {
    ExpProperties findById(Long id);
    ExpProperties findByUserIdAndId(User user, Long id);
    ExpProperties findByIdExpAndIdRun(Long idExp, Long idRun);
}
