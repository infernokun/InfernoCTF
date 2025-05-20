package com.infernokun.infernoctf.repositories;

import com.infernokun.infernoctf.models.entities.AnsweredCTFEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnsweredCTFEntityRepository extends JpaRepository<AnsweredCTFEntity, String> {
    Optional<AnsweredCTFEntity> findByUserIdAndCtfEntityId(String userId, String ctfEntityId);
}
