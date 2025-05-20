package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.models.dto.FlagAnswer;
import com.infernokun.infernoctf.models.entities.AnsweredCTFEntity;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.services.AnsweredCTFEntityService;
import com.infernokun.infernoctf.services.FlagService;
import com.infernokun.infernoctf.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/answer")
@CrossOrigin
public class FlagAnswerController {
    private final FlagService flagService;
    private final AnsweredCTFEntityService answeredCTFEntityService;
    private final UserService userService;

    private final Logger LOGGER = LoggerFactory.getLogger(FlagAnswerController.class);
    public FlagAnswerController(FlagService flagService, AnsweredCTFEntityService answeredCTFEntityService, UserService userService) {
        this.flagService = flagService;
        this.answeredCTFEntityService = answeredCTFEntityService;
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<AnsweredCTFEntity> answerQuestion(@RequestBody FlagAnswer flagAnswer) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (flagAnswer == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!flagAnswer.getUsername().equals(authentication.getName())) {
            // If usernames don't match, return unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAnswerCorrect = this.flagService.validateFlag(flagAnswer);

        Optional<AnsweredCTFEntity> answeredCTFEntityOptional = this.flagService
                .addAnsweredCTFEntity(authentication.getName(), flagAnswer, isAnswerCorrect);

        return answeredCTFEntityOptional.map(answeredCTFEntity ->
                        ResponseEntity.status(HttpStatus.OK).body(answeredCTFEntity))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/check")
    public ResponseEntity<AnsweredCTFEntity> checkChallengeStatus(@RequestParam String ctfEntityId) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = this.userService.findUserByUsername(authentication.getName());

        Optional<AnsweredCTFEntity> answeredCTFEntityOptional = this.answeredCTFEntityService
                .findByUserIdAndCtfEntityId(user.getId(), ctfEntityId);

        return answeredCTFEntityOptional.map(answeredCTFEntity ->
                        ResponseEntity.status(HttpStatus.OK).body(answeredCTFEntity))
                .orElse(ResponseEntity.ok().build());
    }
}
