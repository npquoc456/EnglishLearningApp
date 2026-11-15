package com.example.demo.dto.response;



import lombok.Builder;
import lombok.Data;

@Builder 
@Data 
public class StudySessionItemResponse {
    private Long id;
    private Long sessionId;
    private Long cardId;
    private Boolean isCorrect;
    private String userTypedAnswer;
}
