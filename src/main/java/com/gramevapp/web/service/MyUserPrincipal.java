package com.gramevapp.web.service;

import com.gramevapp.web.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

// http://www.baeldung.com/spring-security-authentication-with-a-database
public class MyUserPrincipal implements UserDetails {
    private User user;

    // https://springframework.guru/spring-boot-web-application-part-6-spring-security-with-dao-authentication-provider/
    private Collection<SimpleGrantedAuthority> authorities;
    private String username;
    private String password;
    private String firstName;
    private Boolean enabled = true;

    public MyUserPrincipal(){}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setAuthorities(Collection<SimpleGrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public MyUserPrincipal(User user) {
        this.user = user;
    }
}