
package com.example.demo.dto.response;

import com.example.demo.constant.studyDirectionConstant;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String accessToken;
    private String fullName;
    private String email;
    private String avatarUrl;
    private Integer currentStreak;
    private LocalDate lastStudyDate;
    private studyDirectionConstant defaultStudyDirection;
    private Integer dailyNewCardsLimit;
    private Boolean autoPlayAudio;
    private LocalTime reminderTime;
    private LocalDateTime createdAt;
}