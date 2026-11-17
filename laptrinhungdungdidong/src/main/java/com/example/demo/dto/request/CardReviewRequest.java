package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;

public record CardReviewRequest(@NotNull Long userId, @NotNull Long cardId,
                                @NotNull Rating rating) {
    public enum Rating { AGAIN, HARD, GOOD, EASY }
}
