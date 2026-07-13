package com.recruitment.auth.repository;

import com.recruitment.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID>,
        JpaSpecificationExecutor<Role> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

}