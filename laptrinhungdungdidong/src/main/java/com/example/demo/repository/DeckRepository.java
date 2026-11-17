package com.example.demo.repository;

import com.example.demo.constant.DeckVisibility;
import com.example.demo.entity.decks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository 
public interface DeckRepository extends JpaRepository<decks, Long> {
    // Lấy tất cả bộ thẻ của một user
    List<decks> findByUserId(Long userId);

    // Lấy danh sách bộ thẻ công khai (Public)
    List<decks> findByVisibility(DeckVisibility visibility);

    // Tìm kiếm bộ thẻ của user theo tiêu đề
    List<decks> findByUserIdAndTitleContainingIgnoreCase(Long userId, String keyword);

    // Tìm kiếm bộ thẻ công khai theo từ khóa
    List<decks> findByVisibilityAndTitleContainingIgnoreCase(DeckVisibility visibility, String keyword);
    boolean existsByIdAndUserId(Long id, Long userId);
}
