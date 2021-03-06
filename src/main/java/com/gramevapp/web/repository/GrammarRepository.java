package com.gramevapp.web.repository;

import com.gramevapp.web.model.Grammar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrammarRepository extends JpaRepository<Grammar, Long> {
    Grammar findGrammarByIdOrderByCreationDateDesc(Long grammarId);

    Grammar findGrammarByGrammarNameAndUserId(String name, Long userId);

    List<Grammar> findByUserId(Long id);

    @Query(value = "SELECT nextval('webge.hibernate_sequence');", nativeQuery =
            true)
    Long getNextValue();


}
