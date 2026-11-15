package com.example.demo.dto.request;


import java.time.LocalDateTime;

import com.example.demo.constant.StudyModeType;

import jakarta.validation.constraints.Min;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data 
public class StudySessionCreateRequest {
    @NotNull(message = "userId không được để trống")
    private Long userId;

    // Không đặt @NotNull vì deck_id cho phép null (học tổng hợp nhiều bộ thẻ)
    private Long deckId;

    @NotNull(message = "Chế độ học (studyMode) không được để trống")
    private StudyModeType studyMode;

    @NotNull(message = "Tổng số thẻ (totalCards) không được để trống")
    @Min(value = 1, message = "Tổng số thẻ phải lớn hơn hoặc bằng 1")
    private Integer totalCards;

    @NotNull(message = "Số thẻ đúng (correctCards) không được để trống")
    @Min(value = 0, message = "Số thẻ đúng không được âm")
    private Integer correctCards;

    @NotNull(message = "Thời gian học (durationSeconds) không được để trống")
    @Min(value = 0, message = "Thời gian học không được âm")
    private Integer durationSeconds;

    private LocalDateTime studiedAt;
}
