package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.ResourceNotFoundException;
import com.infernokun.infernoctf.models.entities.AnsweredCTFEntity;
import com.infernokun.infernoctf.models.entities.CTFEntity;
import com.infernokun.infernoctf.models.dto.FlagAnswer;
import com.infernokun.infernoctf.models.entities.Flag;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.repositories.FlagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FlagService {
    private final CTFEntityService ctfEntityService;
    private final UserService userService;
    private final AnsweredCTFEntityService answeredCTFEntityService;
    private final FlagRepository flagRepository;

    public FlagService(CTFEntityService ctfEntityService, UserService userService,
                       AnsweredCTFEntityService answeredCTFEntityService, FlagRepository flagRepository) {
        this.ctfEntityService = ctfEntityService;
        this.userService = userService;
        this.answeredCTFEntityService = answeredCTFEntityService;
        this.flagRepository = flagRepository;
    }

    public Flag saveFlag(Flag flag) {
        return this.flagRepository.save(flag);
    }

    public boolean validateFlag(FlagAnswer flagAnswer) {
        CTFEntity ctfEntity =  this.ctfEntityService.findCTFEntityById(flagAnswer.getQuestionId());

        return ctfEntity.getFlags().stream().map(Flag::getFlag)
                .anyMatch(flag -> flag.equals(flagAnswer.getFlag()));
    }

    public AnsweredCTFEntity addAnsweredCTFEntity(String username, FlagAnswer flagAnswer, boolean correct) {
        User user = this.userService.findUserByUsername(username);
        CTFEntity ctfEntity = this.ctfEntityService.findCTFEntityById(flagAnswer.getQuestionId());

        AnsweredCTFEntity answeredCTFEntity = answeredCTFEntityService
                .findByUserIdAndCtfEntityIdOptional(user.getId(), ctfEntity.getId())
                .orElseGet(() -> AnsweredCTFEntity
                        .builder()
                        .user(user)
                        .ctfEntity(ctfEntity)
                        .correct(correct)
                        .answers(new ArrayList<>())
                        .times(new ArrayList<>())
                        .attempts(0)
                        .build());

        answeredCTFEntity.getAnswers().add(flagAnswer.getFlag());
        answeredCTFEntity.getTimes().add(LocalDateTime.now());
        answeredCTFEntity.setAttempts(answeredCTFEntity.getAttempts() + 1);
        answeredCTFEntity.setCorrect(correct);

        return answeredCTFEntityService.saveAnsweredCTFEntity(answeredCTFEntity);
    }

    public List<Flag> getFlagsByCtfEntityId(String ctfEntityId) {
        return flagRepository.getFlagsByCtfEntityId(ctfEntityId);
    }
}
