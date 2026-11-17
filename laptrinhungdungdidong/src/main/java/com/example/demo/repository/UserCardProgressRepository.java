package com.example.demo.repository;

import com.example.demo.entity.userCardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCardProgressRepository extends JpaRepository<userCardProgress, Long> {

    // Tìm tiến độ học của 1 user trên 1 thẻ cụ thể
    Optional<userCardProgress> findByUserIdAndCardId(Long userId, Long cardId);

    // Lấy tiến độ toàn bộ thẻ trong 1 deck của user
    List<userCardProgress> findByUserIdAndDeckId(Long userId, Long deckId);

    // Lấy danh sách thẻ đã đến hạn ôn tập (Spaced Repetition)
    List<userCardProgress> findByUserIdAndDeckIdAndNextReviewAtLessThanEqual(
            Long userId,
            Long deckId,
            LocalDateTime now
    );

    // Lấy danh sách thẻ được đánh dấu sao (is_starred = true)
    List<userCardProgress> findByUserIdAndDeckIdAndIsStarredTrue(Long userId, Long deckId);
    @Query(value = "SELECT COUNT(*) FROM cards c " +
            "JOIN decks d ON c.deck_id=d.id " +
            "LEFT JOIN user_card_progress p ON p.card_id=c.id AND p.user_id=:userId " +
            "WHERE d.user_id=:userId AND (p.id IS NULL OR p.next_review_at IS NULL OR p.next_review_at<=CURRENT_TIMESTAMP)",
            nativeQuery = true)
    long countDueCards(@Param("userId") Long userId);
    @Query(value = "SELECT COUNT(c.id), " +
            "COALESCE(SUM(CASE WHEN p.status='MASTERED' THEN 1 ELSE 0 END),0), " +
            "COALESCE(SUM(CASE WHEN p.status='LEARNING' THEN 1 ELSE 0 END),0), " +
            "COALESCE(SUM(CASE WHEN p.id IS NULL THEN 1 ELSE 0 END),0), " +
            "COALESCE(SUM(CASE WHEN p.id IS NULL OR p.next_review_at IS NULL OR p.next_review_at<=CURRENT_TIMESTAMP THEN 1 ELSE 0 END),0) " +
            "FROM cards c JOIN decks d ON c.deck_id=d.id " +
            "LEFT JOIN user_card_progress p ON p.card_id=c.id AND p.user_id=:userId " +
            "WHERE d.user_id=:userId", nativeQuery = true)
    List<Object[]> summarizeUser(@Param("userId") Long userId);

}
