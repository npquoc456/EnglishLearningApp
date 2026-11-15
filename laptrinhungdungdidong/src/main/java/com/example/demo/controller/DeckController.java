package com.example.demo.controller;

import com.example.demo.constant.DeckVisibility;
import com.example.demo.dto.request.DeckCreateRequest;
import com.example.demo.dto.request.DeckUpdateRequest;
import com.example.demo.dto.response.DeckResponse;
import com.example.demo.service.DeckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(@Valid @RequestBody DeckCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deckService.createDeck(request));
    }

    @GetMapping
    public ResponseEntity<List<DeckResponse>> getAllDecks() {
        return ResponseEntity.ok(deckService.getAllDecks());
    }

    @GetMapping("/search")
    public ResponseEntity<List<DeckResponse>> searchDecks(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(deckService.searchDecks(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> getDeckById(@PathVariable Long id) {
        return ResponseEntity.ok(deckService.getDeckById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeckResponse>> getDecksByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(deckService.getDecksByUserId(userId));
    }

    @GetMapping("/visibility")
    public ResponseEntity<List<DeckResponse>> getDecksByVisibility(@RequestParam DeckVisibility visibility) {
        return ResponseEntity.ok(deckService.getDecksByVisibility(visibility));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckResponse> updateDeck(
            @PathVariable Long id,
            @Valid @RequestBody DeckUpdateRequest request) {
        return ResponseEntity.ok(deckService.updateDeck(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }
}
