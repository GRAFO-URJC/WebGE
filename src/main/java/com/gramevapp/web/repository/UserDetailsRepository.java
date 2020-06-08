package com.gramevapp.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepository extends JpaRepository<com.gramevapp.web.model.UserDetails, Long> {
}
