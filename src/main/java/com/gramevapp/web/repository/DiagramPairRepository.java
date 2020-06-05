package com.gramevapp.web.repository;

import com.gramevapp.web.model.DiagramPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagramPairRepository extends JpaRepository<DiagramPair, Long> {
}
