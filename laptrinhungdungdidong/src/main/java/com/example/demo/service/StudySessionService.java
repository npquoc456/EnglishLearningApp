package com.example.demo.service;

import com.example.demo.dto.request.StudySessionCreateRequest;
import com.example.demo.dto.response.StudySessionResponse;
import com.example.demo.entity.decks;
import com.example.demo.entity.studySessions;
import com.example.demo.entity.users;
import com.example.demo.repository.DeckRepository;
import com.example.demo.repository.StudySessionRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    @Transactional
    public StudySessionResponse createStudySession(StudySessionCreateRequest request) {
        if (request.getCorrectCards() > request.getTotalCards()) {
            throw new RuntimeException("Số thẻ đúng không được vượt quá tổng số thẻ");
        }

        users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + request.getUserId()));

        LocalDateTime studiedAt = request.getStudiedAt() != null ? request.getStudiedAt() : LocalDateTime.now();

        decks deck = null;
        if (request.getDeckId() != null) {
            deck = deckRepository.findById(request.getDeckId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ thẻ với ID: " + request.getDeckId()));
            deck.setLastStudiedAt(studiedAt);
            deckRepository.save(deck);
        }

        // Cập nhật streak và ngày học gần nhất cho User
        updateUserStreak(user, studiedAt.toLocalDate());

        studySessions session = new studySessions();
        session.setUser(user);
        session.setDeck(deck);
        session.setStudyMode(request.getStudyMode());
        session.setTotalCards(request.getTotalCards());
        session.setCorrectCards(request.getCorrectCards());
        session.setDurationSeconds(request.getDurationSeconds());
        session.setStudiedAt(studiedAt);

        studySessions savedSession = studySessionRepository.save(session);
        return mapToResponse(savedSession);
    }

    
    @Transactional(readOnly = true)
    public List<StudySessionResponse> getAllSessions() {
        return studySessionRepository.findAll().stream()
                .sorted((a, b) -> b.getStudiedAt().compareTo(a.getStudiedAt()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudySessionResponse> getSessionsByUserId(Long userId) {
        return studySessionRepository.findByUserIdOrderByStudiedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudySessionResponse> getSessionsByUserAndDeck(Long userId, Long deckId) {
        return studySessionRepository.findByUserIdAndDeckIdOrderByStudiedAtDesc(userId, deckId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void updateUserStreak(users user, LocalDate studyDate) {
        LocalDate lastDate = user.getLastStudyDate();
        int currentStreak = user.getCurrentStreak() != null ? user.getCurrentStreak() : 0;

        if (lastDate == null) {
            user.setCurrentStreak(1);
            user.setLastStudyDate(studyDate);
        } else if (studyDate.isEqual(lastDate.plusDays(1))) {
            user.setCurrentStreak(currentStreak + 1);
            user.setLastStudyDate(studyDate);
        } else if (studyDate.isAfter(lastDate.plusDays(1))) {
            user.setCurrentStreak(1);
            user.setLastStudyDate(studyDate);
        }
        userRepository.save(user);
    }

    private StudySessionResponse mapToResponse(studySessions session) {
        return StudySessionResponse.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .deckId(session.getDeck() != null ? session.getDeck().getId() : null)
                .deckTitle(session.getDeck() != null ? session.getDeck().getTitle() : "Ôn tập tổng hợp")
                .studyMode(session.getStudyMode())
                .totalCards(session.getTotalCards())
                .correctCards(session.getCorrectCards())
                .durationSeconds(session.getDurationSeconds())
                .studiedAt(session.getStudiedAt())
                .build();
    }
}