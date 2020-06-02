package com.gramevapp.web.service;

import com.gramevapp.web.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleService{
    private RoleRepository roleRepository;

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
}