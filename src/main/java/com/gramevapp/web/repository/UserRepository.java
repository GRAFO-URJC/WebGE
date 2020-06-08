package com.gramevapp.web.repository;

import com.gramevapp.web.model.User;
import com.gramevapp.web.model.UserRegistrationDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);

    User save(UserRegistrationDto userRegistrationDto); // Save the new user by the register form

    User save(User user);

}