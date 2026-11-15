package com.example.demo.dto.request;

import java.time.LocalTime;

import com.example.demo.constant.studyDirectionConstant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class UserUpdateRequest {
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @Size(max = 500, message = "Link ảnh tối đa 500 ký tự")
    private String avatarUrl;

    private studyDirectionConstant defaultStudyDirection;

    @Min(value = 1, message = "Số thẻ mới mỗi ngày phải lớn hơn 0")
    private Integer dailyNewCardsLimit;

    private Boolean autoPlayAudio;

    private LocalTime reminderTime;
}
