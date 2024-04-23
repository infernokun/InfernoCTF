package com.infernokun.repositories;

import com.infernokun.models.entities.AnsweredCTFEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnsweredCTFEntityRepository extends JpaRepository<AnsweredCTFEntity, String> {
    Optional<AnsweredCTFEntity> findByUserIdAndCtfEntityId(Long userId, String ctfEntityId);
}
