package com.example.demo.dto.response;


import java.time.LocalDateTime;

import com.example.demo.constant.DeckVisibility;

import lombok.Builder;
import lombok.Data;

@Builder 
@Data 
public class DeckResponse {
    private Long id;
    private Long userId;
    private String title;
    private String categoryTag;
    private DeckVisibility visibility;
    private Integer totalCards;
    private LocalDateTime lastStudiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
