package com.example.demo.repository;

import com.example.demo.entity.userCardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
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
}