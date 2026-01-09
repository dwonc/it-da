package com.project.itda.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {

    @NotBlank(message = "Provider는 필수입니다")
    private String provider;  // kakao, naver, google

    @NotBlank(message = "Access Token은 필수입니다")
    private String accessToken;

    // 회원가입 시 추가 정보 (선택사항)
    private String username;
    private String phone;
    private String address;
    private Double latitude;
    private Double longitude;
}