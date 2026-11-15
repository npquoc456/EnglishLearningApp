package com.example.demo.entity;



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
@Table (name = "study_session_items")
public class studySessionItems {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "session_id", nullable = false)
    private studySessions sessions; 

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "card_id", nullable = false)
    private cards card; 

    @Column (name = "is_correct", nullable = false)
    private Boolean isCorrect; 

    @Column (name = "user_typed_answer", length = 255)
    private String userTypedAnswer; 
}
