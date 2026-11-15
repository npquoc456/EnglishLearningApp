package com.example.demo.dto.request;



import com.example.demo.constant.DeckVisibility;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class DeckCreateRequest {
    @NotNull(message = "userId không được để trống")
    private Long userId;

    @NotBlank(message = "Tiêu đề bộ thẻ không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;

    @Size(max = 50, message = "Thẻ phân loại (categoryTag) tối đa 50 ký tự")
    private String categoryTag;

    private DeckVisibility visibility;
}
