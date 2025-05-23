package com.infernokun.infernoctf.exceptions;

public class ChallengeNotAnsweredException extends RuntimeException {
    public ChallengeNotAnsweredException(String message) {
        super(message);
    }
}
