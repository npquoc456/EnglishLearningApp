package com.example.demo.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.time.*;

import org.hibernate.annotations.CreationTimestamp;

import com.example.demo.constant.studyDirectionConstant;


@Data
@Entity
@Table (name = "users")
public class users {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName; 

    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email; 

    @Column(name = "password_hash", length = 255, nullable = false)
    private String password; 

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl; 

    @Column (name = "current_streak")
    private Integer currentStreak; 

    @Column (name = "last_study_date")
    private LocalDate lastStudyDate; 

    @Enumerated(EnumType.STRING)
    @Column (name = "default_study_direction")
    private studyDirectionConstant defaultStudyDirection; 

    @Column (name = "daily_new_cards_limit")
    private Integer dailyNewCardsLimit; 

    @NotNull 
    @Column (name = "auto_play_audio")
    private Boolean autoPlayAudio; 

    @Column (name = "reminder_time")
    private LocalTime reminderTime; 

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
}
