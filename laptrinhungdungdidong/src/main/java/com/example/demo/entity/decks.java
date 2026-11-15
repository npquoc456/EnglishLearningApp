package com.example.demo.entity;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.demo.constant.DeckVisibility;

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
@Table (name = "decks")
public class decks {
    
    @Id 
    @GeneratedValue (strategy =  GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne(fetch = FetchType.LAZY) //Khai báo rằng nhiều bộ thẻ (Deck) có thể thuộc về một người dùng (User).
    @JoinColumn(name = "user_id", nullable = false)
    private users user; 
    
    @Column(name = "title", length = 200, nullable = false )
    private String title; 

    @Column (name = "category_tage", length = 50)
    private String categoryTag; 

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private DeckVisibility visibility; 

    @Column(name = "total_cards")
    private Integer totalCards; 

    @Column(name = "last_studied_At")
    private LocalDateTime lastStudiedAt; 


    @CreationTimestamp 
    @Column (name = "created_at", updatable = false)
    private LocalDateTime createAt; 

    @UpdateTimestamp 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; 

}
