package com.example.demo.dto.response;


import java.time.LocalDateTime;

import com.example.demo.constant.StudyModeType;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class StudySessionResponse {
    private Long id;
    private Long userId;
    private Long deckId;
    private StudyModeType studyMode;
    private Integer totalCards;
    private Integer correctCards;
    private Integer durationSeconds;
    private LocalDateTime studiedAt;
}
