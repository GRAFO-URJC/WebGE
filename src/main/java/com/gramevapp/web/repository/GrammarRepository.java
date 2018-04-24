package com.gramevapp.web.repository;

import com.gramevapp.web.model.Grammar;
import com.gramevapp.web.model.Role;
import com.gramevapp.web.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrammarRepository extends JpaRepository<Grammar, Long> {
    Grammar findById(Long id);
    // We cannot user findByUser(User user) -> Better idea is to add the email of the user in the Grammar ** Not implemented yet
    Grammar findByUserIdAndGrammarName(User userId, String name);
    Grammar findByGrammarName(String name);
}
