package com.gramevapp.web.service;

import com.gramevapp.web.model.Experiment;
import com.gramevapp.web.model.Role;
import com.gramevapp.web.model.User;
import com.gramevapp.web.model.UserRegistrationDto;
import com.gramevapp.web.other.UserToUserDetails;
import com.gramevapp.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// Service = DAO
// This will work as an intermediate between the real data and the action we want to do with that - We are the modifier

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

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

    public User save(UserRegistrationDto registration){
        User user = new User();
        user.setFirstName(registration.getFirstName());
        user.setLastName(registration.getLastName());
        user.setEmail(registration.getEmail());
        user.setPassword(passwordEncoder.encode(registration.getPassword()));
        user.setUsername(registration.getUsername().toLowerCase());
        /*user.setAddress(registration.getAddress());
        user.setCity(registration.getCity());
        user.setState(registration.getState());
        user.setZipcode(registration.getZipcode());
        user.setWorkInformation(registration.getWorkInformation());
        user.setStudyInformation(registration.getStudyInformation());
        user.setAboutMe(registration.getAboutMe());*/
        user.setRoles(Arrays.asList(new Role("ROLE_USER")));    // Later we can change the role of the user

        return userRepository.save(user);
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
        return userRepository.findOne(id);
    }

    //@Override
    public User saveOrUpdate(User domainObject) {
        if(domainObject.getPassword() != null){
            domainObject.setPassword(domainObject.getPassword());
        }
        return userRepository.save(domainObject);
    }

    public User save(User s) {
        return userRepository.save(s);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}