package com.example.demo.dto.request;



import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class CardCreateRequest {
    @NotNull(message = "deckId không được để trống")
    private Long deckId;

    @NotBlank(message = "Từ vựng (term) không được để trống")
    @Size(max = 255, message = "Từ vựng tối đa 255 ký tự")
    private String term;

    @NotBlank(message = "Định nghĩa (definition) không được để trống")
    private String definition;

    @Size(max = 50, message = "Loại từ tối đa 50 ký tự")
    private String wordType;

    @Size(max = 255, message = "Phiên âm IPA tối đa 255 ký tự")
    private String ipaPronunciation;

    @Size(max = 500, message = "Link audio tối đa 500 ký tự")
    private String audioUrl;

    private Integer orderIndex;
}
