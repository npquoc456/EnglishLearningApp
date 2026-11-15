package com.example.demo.dto.response;


import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class CardResponse {
    private Long id;
    private Long deckId;
    private String term;
    private String definition;
    private String wordType;
    private String ipaPronunciation;
    private String audioUrl;
    private Integer orderIndex;
    private LocalDateTime createdAt;
}
