package com.ontop.wms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ontop.wms.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}