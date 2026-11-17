package com.example.demo.service;

import com.example.demo.dto.request.CardReviewRequest;
import com.example.demo.dto.response.CardProgressResponse;
import com.example.demo.dto.response.StudySummaryResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardProgressService {
    private final UserCardProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    private CardProgressResponse toResponse(userCardProgress p) {
        return new CardProgressResponse(p.getCard().getId(),
                p.getStatus() == null ? "NEW" : p.getStatus().name(),
                p.getRepetitionCount() == null ? 0 : p.getRepetitionCount(),
                p.getIntervalDays() == null ? 0 : p.getIntervalDays(), p.getNextReviewAt());
    }

    @Transactional(readOnly = true)
    public List<CardProgressResponse> byDeck(Long userId, Long deckId) {
        return progressRepository.findByUserIdAndDeckId(userId, deckId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public CardProgressResponse review(CardReviewRequest request) {
        users user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        cards card = cardRepository.findById(request.cardId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));
        if (!card.getDeck().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Card does not belong to user");
        }
        userCardProgress progress = progressRepository.findByUserIdAndCardId(user.getId(), card.getId())
                .orElseGet(userCardProgress::new);
        if (progress.getId() == null) {
            progress.setUser(user);
            progress.setCard(card);
            progress.setDeck(card.getDeck());
            progress.setIsStarred(false);
            progress.setRepetitionCount(0);
            progress.setLapseCount(0);
            progress.setIntervalDays(0);
            progress.setEaseFactor(new BigDecimal("2.50"));
        }
        int repetitions = progress.getRepetitionCount() == null ? 0 : progress.getRepetitionCount();
        int previousDays = progress.getIntervalDays() == null ? 0 : progress.getIntervalDays();
        double ease = progress.getEaseFactor() == null ? 2.5 : progress.getEaseFactor().doubleValue();
        LocalDateTime now = LocalDateTime.now();
        int days;
        switch (request.rating()) {
            case AGAIN -> {
                days = 0;
                repetitions = 0;
                ease = Math.max(1.3, ease - 0.2);
                progress.setLapseCount((progress.getLapseCount() == null ? 0 : progress.getLapseCount()) + 1);
                progress.setStatus(com.example.demo.constant.CardStatus.LEARNING);
                progress.setNextReviewAt(now.plusMinutes(10));
            }
            case HARD -> {
                days = Math.max(1, (int)Math.ceil(previousDays * 1.2));
                repetitions++;
                ease = Math.max(1.3, ease - 0.15);
                progress.setStatus(com.example.demo.constant.CardStatus.LEARNING);
                progress.setNextReviewAt(now.plusDays(days));
            }
            case GOOD -> {
                days = repetitions == 0 ? 1 : repetitions == 1 ? 3 :
                        Math.max(previousDays + 1, (int)Math.ceil(previousDays * ease));
                repetitions++;
                progress.setStatus(repetitions >= 3 ? com.example.demo.constant.CardStatus.MASTERED :
                        com.example.demo.constant.CardStatus.LEARNING);
                progress.setNextReviewAt(now.plusDays(days));
            }
            case EASY -> {
                days = previousDays == 0 ? 4 :
                        Math.max(4, (int)Math.ceil(previousDays * (ease + 0.3)));
                repetitions++;
                ease = Math.min(3.0, ease + 0.15);
                progress.setStatus(com.example.demo.constant.CardStatus.MASTERED);
                progress.setNextReviewAt(now.plusDays(days));
            }
            default -> throw new IllegalStateException("Unknown rating");
        }
        progress.setRepetitionCount(repetitions);
        progress.setIntervalDays(days);
        progress.setEaseFactor(BigDecimal.valueOf(ease).setScale(2, java.math.RoundingMode.HALF_UP));
        progress.setLastReviewAt(now);
        return toResponse(progressRepository.save(progress));
    }

    @Transactional(readOnly = true)
    public StudySummaryResponse summary(Long userId) {
        Object[] row = progressRepository.summarizeUser(userId).get(0);
        return new StudySummaryResponse(((Number) row[0]).longValue(), ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(), ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue());
    }

    @Transactional(readOnly = true)
    public long countDue(Long userId) {
        return progressRepository.countDueCards(userId);
    }
}
