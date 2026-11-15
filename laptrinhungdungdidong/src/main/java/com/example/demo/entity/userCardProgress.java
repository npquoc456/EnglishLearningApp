package com.example.demo.entity;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import com.example.demo.constant.CardStatus;

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
@Table (name = "user_card_progress")
public class userCardProgress {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; 
    
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "user_id", nullable = false)
    private users user; 

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "card_id", nullable = false)
    private cards card; 

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "deck_id", nullable = false)
    private decks deck; 

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CardStatus status; 

    @Column (name = "is_starred")
    private Boolean isStarred; 

    @Column (name = "interal_days")
    private Integer intervalDays; 

    @Column(name = "ease_factor", precision = 4, scale = 2)
    private BigDecimal easeFactor;

    @Column (name = "repetition_count")
    private Integer repetitionCount; 

    @Column (name = "lapse_count")
    private Integer lapseCount; 

    @Column (name = "next_review_at")
    private LocalDateTime nextReviewAt; 

    @Column (name = "last_review_at")
    private LocalDateTime lastReviewAt; 

}
