package com.example.demo.service;

import com.example.demo.constant.DeckVisibility;
import com.example.demo.dto.request.DeckCreateRequest;
import com.example.demo.dto.request.DeckUpdateRequest;
import com.example.demo.dto.response.DeckResponse;
import com.example.demo.entity.decks;
import com.example.demo.entity.users;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.DeckRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    @Transactional
    public DeckResponse createDeck(DeckCreateRequest request) {
        users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + request.getUserId()));

        decks deck = new decks();
        deck.setUser(user);
        deck.setTitle(request.getTitle());
        deck.setCategoryTag(request.getCategoryTag());
        deck.setVisibility(request.getVisibility());
        deck.setTotalCards(0);

        decks savedDeck = deckRepository.save(deck);
        return mapToResponse(savedDeck);
    }

    @Transactional(readOnly = true)
    public DeckResponse getDeckById(Long id) {
        decks deck = deckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ thẻ với ID: " + id));
        return mapToResponse(deck);
    }

    @Transactional(readOnly = true)
    public List<DeckResponse> getDecksByUserId(Long userId) {
        return deckRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    
    @Transactional(readOnly = true)
    public List<DeckResponse> getAllDecks() {
        return deckRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeckResponse> searchDecks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDecks();
        }
        return deckRepository.findAll().stream()
                .filter(d -> d.getTitle() != null && d.getTitle().toLowerCase().contains(keyword.toLowerCase().trim()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DeckResponse> getDecksByVisibility(DeckVisibility visibility) {
        return deckRepository.findByVisibility(visibility).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeckResponse updateDeck(Long id, DeckUpdateRequest request) {
        decks deck = deckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ thẻ với ID: " + id));

        if (request.getTitle() != null) deck.setTitle(request.getTitle());
        if (request.getCategoryTag() != null) deck.setCategoryTag(request.getCategoryTag());
        if (request.getVisibility() != null) deck.setVisibility(request.getVisibility());

        decks updatedDeck = deckRepository.save(deck);
        return mapToResponse(updatedDeck);
    }

    @Transactional
    public void deleteDeck(Long id) {
        if (!deckRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bộ thẻ với ID: " + id);
        }
        // Xóa toàn bộ thẻ con trước khi xóa deck để tránh lỗi khóa ngoại
        cardRepository.deleteByDeckId(id);
        deckRepository.deleteById(id);
    }


    private DeckResponse mapToResponse(decks deck) {
        return DeckResponse.builder()
                .id(deck.getId())
                .userId(deck.getUser().getId())
                .title(deck.getTitle())
                .categoryTag(deck.getCategoryTag())
                .visibility(deck.getVisibility())
                .totalCards(deck.getTotalCards())
                .lastStudiedAt(deck.getLastStudiedAt())
                .createdAt(deck.getCreateAt()) // Khớp với tên biến createAt trong entity decks của bạn
                .updatedAt(deck.getUpdatedAt())
                .build();
    }
}