package com.example.demo.service;

import com.example.demo.dto.request.CardCreateRequest;
import com.example.demo.dto.request.CardUpdateRequest;
import com.example.demo.dto.response.CardResponse;
import com.example.demo.entity.cards;
import com.example.demo.entity.decks;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    @Transactional
    public CardResponse createCard(CardCreateRequest request) {
        decks deck = deckRepository.findById(request.getDeckId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ thẻ với ID: " + request.getDeckId()));

        cards card = new cards();
        card.setDeck(deck);
        card.setTerm(request.getTerm());
        card.setDefinition(request.getDefinition());
        card.setWordType(request.getWordType());
        card.setIpaPronunciation(request.getIpaPronunciation());
        card.setAudioUrl(request.getAudioUrl());
        card.setOrderIndex(request.getOrderIndex());

        cards savedCard = cardRepository.save(card);

        // Cập nhật lại tổng số thẻ trong deck
        long count = cardRepository.countByDeckId(deck.getId());
        deck.setTotalCards((int) count);
        deckRepository.save(deck);

        return mapToResponse(savedCard);
    }
    

    @Transactional(readOnly = true)
    public List<CardResponse> getAllCards() {
        return cardRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getCardsByDeckId(Long deckId) {
        return cardRepository.findByDeckIdOrderByOrderIndexAsc(deckId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id) {
        cards card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với ID: " + id));
        return mapToResponse(card);
    }

    @Transactional
    public CardResponse updateCard(Long id, CardUpdateRequest request) {
        cards card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với ID: " + id));

        if (request.getTerm() != null) card.setTerm(request.getTerm());
        if (request.getDefinition() != null) card.setDefinition(request.getDefinition());
        if (request.getWordType() != null) card.setWordType(request.getWordType());
        if (request.getIpaPronunciation() != null) card.setIpaPronunciation(request.getIpaPronunciation());
        if (request.getAudioUrl() != null) card.setAudioUrl(request.getAudioUrl());
        if (request.getOrderIndex() != null) card.setOrderIndex(request.getOrderIndex());

        cards updatedCard = cardRepository.save(card);
        return mapToResponse(updatedCard);
    }

    @Transactional
    public void deleteCard(Long id) {
        cards card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với ID: " + id));

        decks deck = card.getDeck();
        cardRepository.delete(card);

        // Cập nhật lại tổng số thẻ trong deck sau khi xóa
        long count = cardRepository.countByDeckId(deck.getId());
        deck.setTotalCards((int) count);
        deckRepository.save(deck);
    }

    private CardResponse mapToResponse(cards card) {
        return CardResponse.builder()
                .id(card.getId())
                .deckId(card.getDeck().getId())
                .term(card.getTerm())
                .definition(card.getDefinition())
                .wordType(card.getWordType())
                .ipaPronunciation(card.getIpaPronunciation())
                .audioUrl(card.getAudioUrl())
                .orderIndex(card.getOrderIndex())
                .createdAt(card.getCreatedAt())
                .build();
    }
}