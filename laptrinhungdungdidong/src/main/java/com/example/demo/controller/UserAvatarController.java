package com.example.demo.controller;

import com.example.demo.entity.UserAvatar;
import com.example.demo.repository.UserAvatarRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/users/{userId}/avatar")
public class UserAvatarController {
    private final UserAvatarRepository avatarRepository;
    private final UserRepository userRepository;

    public UserAvatarController(UserAvatarRepository avatarRepository, UserRepository userRepository) {
        this.avatarRepository = avatarRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("@owner.isSelf(#userId)")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Void> upload(@PathVariable Long userId,
            @RequestPart("image") MultipartFile image) throws IOException {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (image.isEmpty() || image.getSize() > 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image must be between 1B and 1MB");
        }
        String type = image.getContentType();
        if (type == null || !Set.of("image/jpeg", "image/png", "image/webp").contains(type)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only JPEG, PNG, WEBP");
        }
        byte[] bytes = image.getBytes();
        // Verify content is a supported image, not merely client-provided MIME metadata.
        boolean jpeg = bytes.length > 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8;
        boolean png = bytes.length > 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N';
        boolean webp = bytes.length > 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
        if (!(jpeg && type.equals("image/jpeg") || png && type.equals("image/png") || webp && type.equals("image/webp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image data");
        }
        UserAvatar avatar = avatarRepository.findById(userId).orElseGet(UserAvatar::new);
        avatar.setUserId(userId);
        avatar.setImageData(bytes);
        avatar.setContentType(type);
        avatarRepository.save(avatar);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@owner.isSelf(#userId)")
    @GetMapping
    @Transactional
    public ResponseEntity<byte[]> download(@PathVariable Long userId) {
        UserAvatar avatar = avatarRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No avatar"));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(avatar.getContentType()))
                .cacheControl(CacheControl.noCache())
                .body(avatar.getImageData());
    }
}
