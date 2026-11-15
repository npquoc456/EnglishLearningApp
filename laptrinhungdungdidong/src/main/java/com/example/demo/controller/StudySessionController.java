package com.example.demo.controller;

import com.example.demo.dto.request.StudySessionCreateRequest;
import com.example.demo.dto.response.StudySessionResponse;
import com.example.demo.service.StudySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/study-sessions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping
    public ResponseEntity<StudySessionResponse> createStudySession(
            @Valid @RequestBody StudySessionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studySessionService.createStudySession(request));
    }

    @GetMapping
    public ResponseEntity<List<StudySessionResponse>> getAllSessions() {
        return ResponseEntity.ok(studySessionService.getAllSessions());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StudySessionResponse>> getSessionsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(studySessionService.getSessionsByUserId(userId));
    }

    @GetMapping("/user/{userId}/deck/{deckId}")
    public ResponseEntity<List<StudySessionResponse>> getSessionsByUserAndDeck(
            @PathVariable Long userId,
            @PathVariable Long deckId) {
        return ResponseEntity.ok(studySessionService.getSessionsByUserAndDeck(userId, deckId));
    }
}