package com.aerotech.ced_ops_backend.role.service;

import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.role.entity.Role;
import com.aerotech.ced_ops_backend.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService{

    private final RoleRepository roleRepository;

    public Role getRolebyName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(()-> new ResourceNotFoundException("Role Not Found"));
    }
}
