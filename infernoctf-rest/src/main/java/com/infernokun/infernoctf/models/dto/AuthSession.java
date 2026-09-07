package com.infernokun.infernoctf.models.dto;

import com.infernokun.infernoctf.models.entities.User;

/**
 * The refresh token stays out of {@link LoginResponseDTO} so there is no serialisable field
 * that could put it in a JSON body; the controller sets it as an httpOnly cookie instead.
 */
public record AuthSession(String accessToken, String refreshToken, User user) {
}
