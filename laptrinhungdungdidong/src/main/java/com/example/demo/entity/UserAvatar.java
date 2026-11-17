package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_avatars")
public class UserAvatar {
    @Id
    private Long userId;

    @Lob
    @Column(name = "image_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Column(name = "content_type", nullable = false, length = 40)
    private String contentType;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}
