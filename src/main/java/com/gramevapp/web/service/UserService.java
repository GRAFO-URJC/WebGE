package com.gramevapp.web.service;

import com.gramevapp.web.model.Role;
import com.gramevapp.web.model.User;
import com.gramevapp.web.model.UserRegistrationDto;
import com.gramevapp.web.repository.UserDetailsRepository;
import com.gramevapp.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Service = DAO
// This will work as an intermediate between the real data and the action we want to do with that - We are the modifier

@Service("userService")
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void saveUser(UserRegistrationDto registration) {
        User user = new User();
        com.gramevapp.web.model.UserDetails userDetails = new com.gramevapp.web.model.UserDetails();

        user.setUserDetails(userDetails);

        user.getUserDetails().setFirstName(registration.getFirstName());
        user.getUserDetails().setLastName(registration.getLastName());

        user.setEmail(registration.getEmail());
        user.setPassword(passwordEncoder.encode(registration.getPassword()));
        user.setUsername(registration.getUsername().toLowerCase());
        user.setInstitution(registration.getInstitution());

        user.setRoles(Collections.singletonList(new Role("ROLE_USER")));    // Later we can change the role of the user

        userDetailsRepository.save(userDetails);
        user = userRepository.save(user);

        userDetails.setUser(user);

    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getById(Long id) {
        Optional<User> check = userRepository.findById(id);
        return check.orElse(null);
    }

    public void save(User s) {
        userDetailsRepository.save(s.getUserDetails());
        userRepository.save(s);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {    // User not authenticated
            System.out.println("User not authenticated");
            return null;
        }

        return findByUsername(authentication.getName());
    }

    public void updateUser() {
        this.userRepository.flush();
    }

    public List<User> findAllUserWithoutAdmin() {
        User admin = this.getLoggedInUser();
        List<User> userList = userRepository.findAll();
        userList.remove(admin);
        return userList;
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}