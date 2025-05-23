package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.ChallengeNotAnsweredException;
import com.infernokun.infernoctf.exceptions.ResourceNotFoundException;
import com.infernokun.infernoctf.models.entities.AnsweredCTFEntity;
import com.infernokun.infernoctf.repositories.AnsweredCTFEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnsweredCTFEntityService {
    private final AnsweredCTFEntityRepository answeredCTFEntityRepository;

    public Optional<AnsweredCTFEntity> findByUserIdAndCtfEntityIdOptional(String userId, String ctfEntityId) {
        return answeredCTFEntityRepository.findByUserIdAndCtfEntityId(userId, ctfEntityId);
    }

    public AnsweredCTFEntity findByUserIdAndCtfEntityId(String userId, String ctfEntityId) {
        return answeredCTFEntityRepository.findByUserIdAndCtfEntityId(userId, ctfEntityId)
                .orElseThrow(() -> new ChallengeNotAnsweredException("Challenge not yet answered"));
    }

    public AnsweredCTFEntity saveAnsweredCTFEntity(AnsweredCTFEntity answeredCTFEntity) {
        return answeredCTFEntityRepository.save(answeredCTFEntity);
    }
}
