package com.gramevapp.web.repository;

import com.gramevapp.web.model.Grammar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrammarRepository extends JpaRepository<Grammar, Long> {
    Grammar findGrammarById(Long grammarId);

    List<Grammar> findByUserId(Long id);
}
