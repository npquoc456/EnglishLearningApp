package com.example.demo.repository;

import com.example.demo.entity.studySessionItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface StudySessionItemRepository extends JpaRepository<studySessionItems, Long> {

    // Lấy chi tiết tất cả câu trả lời trong một phiên học
    List<studySessionItems> findBySessionsId(Long sessionId);

    // Lấy riêng các thẻ user trả lời sai trong phiên học đó để ôn lại
    List<studySessionItems> findBySessionsIdAndIsCorrectFalse(Long sessionId);
}