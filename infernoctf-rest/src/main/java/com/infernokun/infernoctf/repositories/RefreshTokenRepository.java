package com.infernokun.infernoctf.repositories;

import com.infernokun.infernoctf.models.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserId(String id);
    Optional<RefreshToken> deleteByUserId(String id);
}
