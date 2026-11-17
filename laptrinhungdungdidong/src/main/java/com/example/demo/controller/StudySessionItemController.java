package com.example.demo.controller;

import com.example.demo.dto.request.StudySessionItemCreateRequest;
import com.example.demo.dto.response.StudySessionItemResponse;
import com.example.demo.service.StudySessionItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

@RestController
@RequestMapping("/api/study-session-items")
@RequiredArgsConstructor
public class StudySessionItemController {

    private final StudySessionItemService itemService;

    @PreAuthorize("@owner.sessionAndCard(#request.sessionId, #request.cardId)")
    @PostMapping
    public ResponseEntity<StudySessionItemResponse> createSessionItem(
            @Valid @RequestBody StudySessionItemCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.createSessionItem(request));
    }

    @PreAuthorize("@owner.ownsSession(#sessionId)")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<StudySessionItemResponse>> getItemsBySessionId(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(itemService.getItemsBySessionId(sessionId));
    }

    @PreAuthorize("@owner.ownsSession(#sessionId)")
    @GetMapping("/session/{sessionId}/incorrect")
    public ResponseEntity<List<StudySessionItemResponse>> getIncorrectItemsBySessionId(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(itemService.getIncorrectItemsBySessionId(sessionId));
    }
}