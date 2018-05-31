package com.gramevapp.web.repository;

import com.gramevapp.web.model.ExpProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PropertiesRepository extends JpaRepository<ExpProperties, String> {
    ExpProperties findById(UUID id);
}
