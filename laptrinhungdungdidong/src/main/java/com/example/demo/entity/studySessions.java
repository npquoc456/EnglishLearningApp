package com.example.demo.entity;


import java.time.LocalDateTime;

import com.example.demo.constant.StudyModeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data 
@Entity 
@Table (name = "study_sessions")
public class studySessions {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "user_id", nullable = false)
    private users user; 

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "deck_id")
    private decks deck; 

    @Enumerated (EnumType.STRING)
    @Column(name = "study_mode", nullable = false)
    private StudyModeType studyMode; 

    @Column(name = "total_cards", nullable = false)
    private Integer totalCards; 

    @Column (name = "correct_cards", nullable = false)
    private Integer correctCards; 

    @Column (name = "duration_seconds", nullable = false)
    private Integer durationSeconds; 

    @Column (name = "studied_at")
    private LocalDateTime studiedAt; 
}
