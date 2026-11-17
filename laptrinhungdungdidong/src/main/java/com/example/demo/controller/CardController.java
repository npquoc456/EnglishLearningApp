package com.example.demo.controller;

import com.example.demo.dto.request.CardCreateRequest;
import com.example.demo.dto.request.CardUpdateRequest;
import com.example.demo.dto.response.CardResponse;
import com.example.demo.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PreAuthorize("@owner.ownsDeck(#request.deckId)")
    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
    }

    @PreAuthorize("denyAll()")
    @GetMapping
    public ResponseEntity<List<CardResponse>> getAllCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @PreAuthorize("@owner.ownsCard(#id)")
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @PreAuthorize("@owner.ownsDeck(#deckId)")
    @GetMapping("/deck/{deckId}")
    public ResponseEntity<List<CardResponse>> getCardsByDeckId(@PathVariable Long deckId) {
        return ResponseEntity.ok(cardService.getCardsByDeckId(deckId));
    }

    @PreAuthorize("@owner.ownsCard(#id)")
    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody CardUpdateRequest request) {
        return ResponseEntity.ok(cardService.updateCard(id, request));
    }

    @PreAuthorize("@owner.ownsCard(#id)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}