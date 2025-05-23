package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.models.ApiResponse;
import com.infernokun.infernoctf.models.dto.FlagAnswer;
import com.infernokun.infernoctf.models.entities.AnsweredCTFEntity;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.services.AnsweredCTFEntityService;
import com.infernokun.infernoctf.services.FlagService;
import com.infernokun.infernoctf.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.infernokun.infernoctf.utils.ConstFunctions.buildSuccessResponse;

@Slf4j
@RestController
@RequestMapping("/api/answer")
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
    public ResponseEntity<ApiResponse<AnsweredCTFEntity>> answerQuestion(@RequestBody FlagAnswer flagAnswer) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (flagAnswer == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!flagAnswer.getUsername().equals(authentication.getName())) {
            // If usernames don't match, return unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAnswerCorrect = this.flagService.validateFlag(flagAnswer);

        return buildSuccessResponse("Got some answer", flagService
                .addAnsweredCTFEntity(authentication.getName(), flagAnswer, isAnswerCorrect), HttpStatus.OK);
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<AnsweredCTFEntity>> checkChallengeStatus(@RequestParam String ctfEntityId) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = this.userService.findUserById(authentication.getName());

        return buildSuccessResponse("Got some answer", answeredCTFEntityService
                .findByUserIdAndCtfEntityId(user.getId(), ctfEntityId), HttpStatus.OK);
    }
}
