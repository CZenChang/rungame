package com.dodognoman.rungame.user.dto;

import java.time.OffsetDateTime;

public record AuthResponse(
        Long userId,
        String username,
        String role,
        String accessToken,
        String refreshToken,
        OffsetDateTime tokenExpiresAt
) {}
