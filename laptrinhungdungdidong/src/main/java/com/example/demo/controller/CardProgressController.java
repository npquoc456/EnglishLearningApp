package com.example.demo.controller;

import com.example.demo.dto.request.CardReviewRequest;
import com.example.demo.dto.response.CardProgressResponse;
import com.example.demo.dto.response.StudySummaryResponse;
import com.example.demo.service.CardProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class CardProgressController {
    private final CardProgressService service;

    @GetMapping("/user/{userId}/summary")
    public StudySummaryResponse summary(@PathVariable Long userId) {
        return service.summary(userId);
    }

    @PostMapping("/review")
    public CardProgressResponse review(@Valid @RequestBody CardReviewRequest request) {
        return service.review(request);
    }

    @GetMapping("/user/{userId}/deck/{deckId}")
    public List<CardProgressResponse> byDeck(@PathVariable Long userId, @PathVariable Long deckId) {
        return service.byDeck(userId, deckId);
    }

    @GetMapping("/user/{userId}/due-count")
    public Map<String, Long> dueCount(@PathVariable Long userId) {
        return Map.of("count", service.countDue(userId));
    }
}
