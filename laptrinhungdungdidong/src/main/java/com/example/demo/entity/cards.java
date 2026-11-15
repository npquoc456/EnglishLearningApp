package com.example.demo.entity;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table (name = "cards")
public class cards {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "deck_id", nullable = false)
    private decks deck; 

    @Column(name = "term", length = 255, nullable = false)
    private String term; 

    @Column (name = "definition", nullable = false)
    private String definition; 

    @Column (name = "word_type", length = 50)
    private String wordType; 

    @Column (name = "ipa_pronunciation", length = 255)
    private String ipaPronunciation; 

    @Column (name = "audio_url", length = 500)
    private String audioUrl; 

    @Column (name = "order_index")
    private Integer orderIndex; 

    @CreationTimestamp 
    @Column (name = "created_at")
    private LocalDateTime createdAt; 
}
