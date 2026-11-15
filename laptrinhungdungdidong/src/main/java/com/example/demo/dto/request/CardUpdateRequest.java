package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class CardUpdateRequest {
    
    @Size(max = 255, message = "Từ vựng tối đa 255 ký tự")
    private String term;

    private String definition;

    @Size(max = 50, message = "Loại từ tối đa 50 ký tự")
    private String wordType;

    @Size(max = 255, message = "Phiên âm IPA tối đa 255 ký tự")
    private String ipaPronunciation;

    @Size(max = 500, message = "Link audio tối đa 500 ký tự")
    private String audioUrl;

    private Integer orderIndex;
}
