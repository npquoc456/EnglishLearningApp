package com.example.demo.repository;

import com.example.demo.entity.studySessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface StudySessionRepository extends JpaRepository<studySessions, Long> {

    // Lấy lịch sử các phiên học của user, mới nhất xếp trước
    List<studySessions> findByUserIdOrderByStudiedAtDesc(Long userId);

    // Lấy lịch sử học của user trên một bộ thẻ cụ thể
    List<studySessions> findByUserIdAndDeckIdOrderByStudiedAtDesc(Long userId, Long deckId);
    boolean existsByIdAndUserId(Long id, Long userId);
}
