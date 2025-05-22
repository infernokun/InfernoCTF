package com.infernokun.infernoctf.repositories;

import com.infernokun.infernoctf.models.enums.Role;
import com.infernokun.infernoctf.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameIgnoreCase(@Param("username") String username);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    boolean existsByUsernameIgnoreCase(@Param("username") String username);

    Optional<List<User>> findByRole(Role role);
    Optional<User> findByEmail(String email);
}
