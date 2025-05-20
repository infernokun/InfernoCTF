package com.infernokun.services;

import com.infernokun.models.entities.AnsweredCTFEntity;
import com.infernokun.models.entities.CTFEntity;
import com.infernokun.models.dto.FlagAnswer;
import com.infernokun.models.entities.Flag;
import com.infernokun.models.entities.User;
import com.infernokun.repositories.FlagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FlagService {
    private final CTFEntityService ctfEntityService;
    private final UserService userService;
    private final AnsweredCTFEntityService answeredCTFEntityService;
    private final FlagRepository flagRepository;

    private Logger LOGGER = LoggerFactory.getLogger(FlagService.class);

    public FlagService(CTFEntityService ctfEntityService, UserService userService, AnsweredCTFEntityService answeredCTFEntityService, FlagRepository flagRepository) {
        this.ctfEntityService = ctfEntityService;
        this.userService = userService;
        this.answeredCTFEntityService = answeredCTFEntityService;
        this.flagRepository = flagRepository;
    }

    public Flag saveFlag(Flag flag) {
        return this.flagRepository.save(flag);
    }

    public boolean validateFlag(FlagAnswer flagAnswer) {
        Optional<CTFEntity> ctfEntityOptional =  this.ctfEntityService.findCTFEntityById(flagAnswer.getQuestionId());
        if (ctfEntityOptional.isEmpty()) { return false; }

        CTFEntity ctfEntity = ctfEntityOptional.get();

        return ctfEntity.getFlags().stream().map(Flag::getFlag)
                .anyMatch(flag -> flag.equals(flagAnswer.getFlag()));
    }

    public Optional<AnsweredCTFEntity> addAnsweredCTFEntity(String username, FlagAnswer flagAnswer, boolean correct) {
        User user = this.userService.findUserByUsername(username);
        Optional<CTFEntity> ctfEntityOptional =  this.ctfEntityService.findCTFEntityById(flagAnswer.getQuestionId());
        if (ctfEntityOptional.isEmpty()) { return Optional.empty(); }


        Optional<AnsweredCTFEntity> answeredCTFEntityOptional = this.
                answeredCTFEntityService.
                findByUserIdAndCtfEntityId(user.getId(), ctfEntityOptional.get().getId());

        AnsweredCTFEntity answeredCTFEntity = answeredCTFEntityOptional.orElseGet(
                () -> AnsweredCTFEntity
                        .builder()
                        .user(user)
                        .ctfEntity(ctfEntityOptional.get())
                        .correct(correct)
                        .answers(new ArrayList<>())
                        .times(new ArrayList<>())
                        .build());

        answeredCTFEntity.getAnswers().add(flagAnswer.getFlag());
        answeredCTFEntity.getTimes().add(LocalDateTime.now());
        answeredCTFEntity.setAttempts(answeredCTFEntity.getAttempts() + 1);
        answeredCTFEntity.setCorrect(correct);

        return this.answeredCTFEntityService.saveAnsweredCTFEntity(answeredCTFEntity);
    }
}
