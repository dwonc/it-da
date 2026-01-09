package com.project.itda.domain.auth.dto.response;

import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LoginResponse {
    private Long userId;
    private String email;
    private String username;
    private UserStatus status;
    private String profileImageUrl;
    private String provider;  // 소셜 로그인 제공자
    private LocalDateTime lastLoginAt;
    private String sessionId;
    private String message;

    public static LoginResponse from(User user, String sessionId) {
        return LoginResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .status(user.getStatus())
                .profileImageUrl(user.getProfileImageUrl())
                .provider(user.getProvider())
                .lastLoginAt(user.getLastLoginAt())
                .sessionId(sessionId)
                .message("로그인 성공")
                .build();
    }
}