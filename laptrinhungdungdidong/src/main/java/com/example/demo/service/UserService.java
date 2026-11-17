package com.example.demo.service;

import com.example.demo.dto.request.UserLoginRequest;
import com.example.demo.dto.request.UserRegisterRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.users;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại trong hệ thống");
        }

        users user = new users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));

        // Default values for brand new user (starts completely fresh with 0 streak)
        user.setCurrentStreak(0);
        user.setLastStudyDate(null);
        user.setDailyNewCardsLimit(20);
        user.setAutoPlayAudio(true);

        users savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse login(UserLoginRequest request) {
        users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản hoặc mật khẩu không chính xác"));

        String stored = user.getPassword();
        boolean verified = stored != null && PASSWORD_ENCODER.matches(request.getPassword(), stored);
        // Compatibility for users from the old demo database: upgrade a
        // legacy plaintext password only after a successful login.
        boolean legacyVerified = stored != null
                && !stored.startsWith("$2")
                && stored.equals(request.getPassword());
        if (!verified && !legacyVerified) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Tài khoản hoặc mật khẩu không chính xác");
        }
        if (legacyVerified) {
            user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));
            userRepository.save(user);
        }

        return mapToResponse(user);
    }

    @Transactional
    public UserResponse createUser(UserLoginRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại trong hệ thống");
        }

        users user = new users();
        user.setFullName(request.getEmail().split("@")[0]);
        user.setEmail(request.getEmail());
        user.setPassword(PASSWORD_ENCODER.encode(request.getPassword()));

        user.setCurrentStreak(0);
        user.setDailyNewCardsLimit(20);
        user.setAutoPlayAudio(true);

        users savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getDefaultStudyDirection() != null) user.setDefaultStudyDirection(request.getDefaultStudyDirection());
        if (request.getDailyNewCardsLimit() != null) user.setDailyNewCardsLimit(request.getDailyNewCardsLimit());
        if (request.getAutoPlayAudio() != null) user.setAutoPlayAudio(request.getAutoPlayAudio());
        if (request.getReminderTime() != null) user.setReminderTime(request.getReminderTime());

        users updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy người dùng với ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public UserResponse mapToResponse(users user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .currentStreak(user.getCurrentStreak())
                .lastStudyDate(user.getLastStudyDate())
                .defaultStudyDirection(user.getDefaultStudyDirection())
                .dailyNewCardsLimit(user.getDailyNewCardsLimit())
                .autoPlayAudio(user.getAutoPlayAudio())
                .reminderTime(user.getReminderTime())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
