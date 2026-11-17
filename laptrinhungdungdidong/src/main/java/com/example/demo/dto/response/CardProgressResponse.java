package com.example.demo.dto.response;

import java.time.LocalDateTime;

public record CardProgressResponse(Long cardId, String status,
                                   int repetitionCount, int intervalDays,
                                   LocalDateTime nextReviewAt) {}
