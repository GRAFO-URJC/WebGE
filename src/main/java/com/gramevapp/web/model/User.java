package com.gramevapp.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @Column(name = "USER_ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String email;

    @Column
    private Boolean enabled = true;
    @Column
    private Integer failedLoginAttempts = 0;
    @Column
    private String institution;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL,
            mappedBy = "user")
    private UserDetails userDetails;

    /*develop
    //cascade delete
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "userId")
    private List<Experiment> experimentList;
    //cascade delete
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "userId")
    private List<Grammar> grammarList;
    //cascade delete
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "userIdUserId")
    private List<Dataset> datasetList;
*/
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = Role.class)
    @JoinTable(
            name = "users_roles",
            joinColumns = {
                    @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
                    @JoinColumn(name = "username", referencedColumnName = "username"),
                    @JoinColumn(name = "email", referencedColumnName = "email")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "role_id", referencedColumnName = "role_id"),
                    @JoinColumn(name = "role_name", referencedColumnName = "role")
            })
    private List<Role> roles;

    public User() {
        /*Do nothing*/
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }
}