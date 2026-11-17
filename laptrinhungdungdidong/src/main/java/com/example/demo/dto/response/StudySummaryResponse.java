package com.example.demo.dto.response;

public record StudySummaryResponse(long total, long mastered,
                                   long learning, long newCards, long due) {}
