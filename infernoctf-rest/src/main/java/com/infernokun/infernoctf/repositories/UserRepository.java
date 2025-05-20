package com.infernokun.infernoctf.repositories;

import com.infernokun.infernoctf.models.enums.Role;
import com.infernokun.infernoctf.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<List<User>> findByRole(Role role);
    Optional<User> findByEmail(String email);
}
