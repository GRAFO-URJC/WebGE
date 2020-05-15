package com.gramevapp.web.service;

import com.gramevapp.web.model.*;
import com.gramevapp.web.other.UserToUserDetails;
import com.gramevapp.web.repository.UserDetailsRepository;
import com.gramevapp.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null){
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles()));
    }

    public User saveUser(UserRegistrationDto registration){
        User user = new User();
        com.gramevapp.web.model.UserDetails userDetails = new com.gramevapp.web.model.UserDetails();

        user.setUserDetails(userDetails);

        user.getUserDetails().setFirstName(registration.getFirstName());
        user.getUserDetails().setLastName(registration.getLastName());

        user.setEmail(registration.getEmail());
        user.setPassword(passwordEncoder.encode(registration.getPassword()));
        user.setUsername(registration.getUsername().toLowerCase());
        user.setInstitution(registration.getInstitution());

        user.setRoles(Arrays.asList(new Role("ROLE_USER")));    // Later we can change the role of the user

        userDetailsRepository.save(userDetails);
        user = userRepository.save(user);

        userDetails.setUser(user);

        return user;
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles){
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }


    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }

    public User findByEmail(String email){  return userRepository.findByEmail(email); }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //@Override
    public List<?> listAll() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add); //fun with Java 8
        return users;
    }

    public User getById(Long id) {
        return userRepository.findById(id).get();
    }

    //@Override
    public User saveOrUpdate(User domainObject) {
        if(domainObject.getPassword() != null){
            domainObject.setPassword(domainObject.getPassword());
        }
        return userRepository.save(domainObject);
    }

    public User save(User s) {
        userDetailsRepository.save(s.getUserDetails());
        return userRepository.save(s);
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

        User user = findByUsername(authentication.getName());
        return user;
    }

    public void updateProfilePicture(User user, String profilePicture) {
        this.userRepository.updateFilePath(user.getUsername(), profilePicture);
    }

    public void updateUser(){
        this.userRepository.flush();
    }
}