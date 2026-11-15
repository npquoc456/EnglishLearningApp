package com.example.demo.dto.request;



import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class StudySessionItemCreateRequest {
    @NotNull(message = "sessionId không được để trống")
    private Long sessionId;

    @NotNull(message = "cardId không được để trống")
    private Long cardId;

    @NotNull(message = "Trạng thái đúng/sai (isCorrect) không được để trống")
    private Boolean isCorrect;

    @Size(max = 255, message = "Câu trả lời nhập vào tối đa 255 ký tự")
    private String userTypedAnswer;
}
