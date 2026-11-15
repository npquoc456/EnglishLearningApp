package com.example.demo.service;

import com.example.demo.dto.request.StudySessionItemCreateRequest;
import com.example.demo.dto.response.StudySessionItemResponse;
import com.example.demo.entity.cards;
import com.example.demo.entity.studySessionItems;
import com.example.demo.entity.studySessions;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.StudySessionItemRepository;
import com.example.demo.repository.StudySessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudySessionItemService {

    private final StudySessionItemRepository itemRepository;
    private final StudySessionRepository sessionRepository;
    private final CardRepository cardRepository;

    @Transactional
    public StudySessionItemResponse createSessionItem(StudySessionItemCreateRequest request) {
        studySessions session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên học với ID: " + request.getSessionId()));

        cards card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ với ID: " + request.getCardId()));

        studySessionItems item = new studySessionItems();
        item.setSessions(session); // Khớp với tên biến sessions trong entity studySessionItems của bạn
        item.setCard(card);
        item.setIsCorrect(request.getIsCorrect());
        item.setUserTypedAnswer(request.getUserTypedAnswer());

        studySessionItems savedItem = itemRepository.save(item);
        return mapToResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public List<StudySessionItemResponse> getItemsBySessionId(Long sessionId) {
        return itemRepository.findBySessionsId(sessionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudySessionItemResponse> getIncorrectItemsBySessionId(Long sessionId) {
        return itemRepository.findBySessionsIdAndIsCorrectFalse(sessionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private StudySessionItemResponse mapToResponse(studySessionItems item) {
        return StudySessionItemResponse.builder()
                .id(item.getId())
                .sessionId(item.getSessions().getId())
                .cardId(item.getCard().getId())
                .isCorrect(item.getIsCorrect())
                .userTypedAnswer(item.getUserTypedAnswer())
                .build();
    }
}