package com.gramevapp.web.model;

import com.gramevapp.web.abstractClasses.AbstractDomainClass;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "role")
public class Role implements Serializable {
    // Admin, normal user, user with privilages (?) ....

    @Id
    @Column(name = "ROLE_ID", nullable = false, updatable= false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "role")
    private String role;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    public Role() {
    }

    public Role(String name) {
        this.role = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return role;
    }

    public void setName(String name) {
        this.role = name;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", role name='" + role + '\'' +
                '}';
    }


    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
}