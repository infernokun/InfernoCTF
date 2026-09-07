package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.models.ApiResponse;
import com.infernokun.infernoctf.models.dto.FlagAnswer;
import com.infernokun.infernoctf.models.entities.AnsweredCTFEntity;
import com.infernokun.infernoctf.models.entities.User;
import com.infernokun.infernoctf.services.AnsweredCTFEntityService;
import com.infernokun.infernoctf.services.FlagService;
import com.infernokun.infernoctf.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.infernokun.infernoctf.utils.ConstFunctions.buildSuccessResponse;

@Slf4j
@RestController
@RequestMapping("/api/answer")
public class FlagAnswerController {
    private final FlagService flagService;
    private final AnsweredCTFEntityService answeredCTFEntityService;
    private final UserService userService;

    public FlagAnswerController(FlagService flagService, AnsweredCTFEntityService answeredCTFEntityService,
                                UserService userService) {
        this.flagService = flagService;
        this.answeredCTFEntityService = answeredCTFEntityService;
        this.userService = userService;
    }

    /**
     * The submitter comes from the token, never the request body. Note the JWT subject is the
     * user ID, not the username, so {@code flagAnswer.getUsername()} is ignored.
     */
    @PostMapping()
    public ResponseEntity<ApiResponse<AnsweredCTFEntity>> answerQuestion(@RequestBody FlagAnswer flagAnswer,
                                                                        Authentication authentication) {
        if (flagAnswer == null || flagAnswer.getQuestionId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user = this.userService.findUserById(authentication.getName());
        boolean isAnswerCorrect = this.flagService.validateFlag(flagAnswer);

        return buildSuccessResponse("Got some answer",
                flagService.addAnsweredCTFEntity(user, flagAnswer, isAnswerCorrect), HttpStatus.OK);
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<AnsweredCTFEntity>> checkChallengeStatus(@RequestParam String ctfEntityId,
                                                                              Authentication authentication) {
        User user = this.userService.findUserById(authentication.getName());

        return buildSuccessResponse("Got some answer", answeredCTFEntityService
                .findByUserIdAndCtfEntityId(user.getId(), ctfEntityId), HttpStatus.OK);
    }
}
