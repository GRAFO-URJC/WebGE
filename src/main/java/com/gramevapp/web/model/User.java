package com.gramevapp.web.model;

import com.gramevapp.web.abstractClasses.AbstractDomainClass;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
//@Table(uniqueConstraints = @UniqueConstraint(columnNames = "username"), name = "user")
@Table(name = "user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(nullable = true)    //  True because the real user will be the email
    private String username;
    @Column(nullable = false)
    private String password;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column(nullable = false)
    private String email;
    @Column
    private String address;
    @Column
    private Integer phone;
    @Column
    private Boolean enabled = true;
    @Column
    private Integer failedLoginAttempts = 0;

    @ManyToMany(cascade=CascadeType.PERSIST, fetch=FetchType.EAGER, targetEntity=Role.class)
    @JoinTable(
            name = "users_roles",
            joinColumns ={
                    @JoinColumn(name = "user_id", referencedColumnName = "id"),
                    @JoinColumn(name = "username", referencedColumnName = "username"),
                    @JoinColumn(name = "email", referencedColumnName = "email")
                    //@JoinColumn(name = "password", referencedColumnName = "password")
            },
            inverseJoinColumns ={
                    @JoinColumn(name = "role_id", referencedColumnName = "id"),
                    @JoinColumn(name = "role_name", referencedColumnName = "role")
            })
    private List<Role> roles;

    public User(){ }

    public User(Long id) {
        this.id = id;
    }

    public User(Long id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(String username, String password, String email, List<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }

    public User(String username, String password, String firstName, String lastname, String email, String address, Integer phone) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastname;
        this.email = email;
        this.address = address;
        this.phone = phone;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getId() { return id; }

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastname) {
        this.lastName = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPhone() {
        return phone;
    }

    public void setPhone(Integer phone) {
        this.phone = phone;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role){
        if(!this.roles.contains(role)){
            this.roles.add(role);
        }

        /*if(!role.getUsers().contains(this)){
            role.getUsers().add(this);
        }*/
    }

    public void removeRole(Role role){
        this.roles.remove(role);
        //role.getUsers().remove(this);
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + "*********" + '\'' +
                ", roles=" + roles +
                '}';
    }
}