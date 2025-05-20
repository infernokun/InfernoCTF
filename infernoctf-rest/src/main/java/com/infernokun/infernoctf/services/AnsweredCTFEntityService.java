package com.infernokun.services;

import com.infernokun.models.entities.AnsweredCTFEntity;
import com.infernokun.repositories.AnsweredCTFEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnsweredCTFEntityService {
    private final AnsweredCTFEntityRepository answeredCTFEntityRepository;

    public AnsweredCTFEntityService(AnsweredCTFEntityRepository answeredCTFEntityRepository) {
        this.answeredCTFEntityRepository = answeredCTFEntityRepository;
    }

    public Optional<AnsweredCTFEntity> findByUserIdAndCtfEntityId(Long userId, String ctfEntityId) {
        return this.answeredCTFEntityRepository.findByUserIdAndCtfEntityId(userId, ctfEntityId);
    }

    public Optional<AnsweredCTFEntity> saveAnsweredCTFEntity(AnsweredCTFEntity answeredCTFEntity) {
        return Optional.of(this.answeredCTFEntityRepository.save(answeredCTFEntity));
    }
}
