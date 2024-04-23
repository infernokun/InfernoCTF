package com.infernokun.repositories;

import com.infernokun.models.enums.Role;
import com.infernokun.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<List<User>> findByRole(Role role);
    Optional<User> findByEmail(String email);
}
