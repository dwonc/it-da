package com.project.itda.domain.auth.dto.response;

import com.project.itda.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SignupResponse {
    private Long userId;
    private String email;
    private String username;
    private LocalDateTime createdAt;
    private String message;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .createdAt(user.getCreatedAt())
                .message("회원가입 성공")
                .build();
    }
}