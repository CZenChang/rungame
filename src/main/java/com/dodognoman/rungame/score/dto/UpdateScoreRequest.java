package com.dodognoman.rungame.score.dto;

import jakarta.validation.constraints.Min;

public record UpdateScoreRequest(
        @Min(value = 0, message = "score 不可為負數")
        int score
) {}
