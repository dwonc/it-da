package com.project.itda.domain.auth.service;

import com.project.itda.domain.auth.dto.request.SocialLoginRequest;
import com.project.itda.domain.auth.dto.response.LoginResponse;
import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.enums.UserStatus;
import com.project.itda.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2Service {

    private final UserRepository userRepository;
    private final SessionService sessionService;

    /**
     * 소셜 로그인
     * TODO: 실제 OAuth2 인증 구현 필요
     */
    @Transactional
    public LoginResponse socialLogin(SocialLoginRequest request, HttpSession session) {
        log.info("소셜 로그인 시도: provider={}", request.getProvider());

        // TODO: Access Token으로 소셜 제공자(Kakao, Naver, Google)에서 사용자 정보 가져오기
        // 1. Access Token 검증
        // 2. 소셜 제공자 API 호출하여 사용자 정보 조회

        // 임시: providerId를 accessToken으로 사용 (실제로는 API 호출 결과 사용)
        String providerId = request.getAccessToken();

        // 3. 기존 사용자 조회 또는 신규 가입
        User user = userRepository.findByProviderAndProviderId(
                request.getProvider(),
                providerId
        ).orElseGet(() -> createSocialUser(request, providerId));

        // 4. 계정 상태 확인
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("비활성화된 계정입니다");
        }

        // 5. 마지막 로그인 시간 업데이트
        user.updateLastLogin();

        // 6. 세션 생성
        sessionService.createUserSession(session, user);

        log.info("소셜 로그인 성공: userId={}, provider={}", user.getUserId(), user.getProvider());

        return LoginResponse.from(user, session.getId());
    }

    /**
     * 소셜 로그인 신규 사용자 생성
     */
    @Transactional
    protected User createSocialUser(SocialLoginRequest request, String providerId) {
        log.info("소셜 로그인 신규 가입: provider={}", request.getProvider());

        User user = User.builder()
                .email(generateEmailFromProvider(request.getProvider(), providerId))
                .passwordHash("SOCIAL_LOGIN")  // 소셜 로그인은 비밀번호 불필요
                .username(request.getUsername() != null ? request.getUsername() : "사용자" + System.currentTimeMillis())
                .phone(request.getPhone())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .provider(request.getProvider())
                .providerId(providerId)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)  // 소셜 로그인은 이메일 인증 불필요
                .build();

        return userRepository.save(user);
    }

    /**
     * Provider와 ProviderId로 임시 이메일 생성
     */
    private String generateEmailFromProvider(String provider, String providerId) {
        return provider + "_" + providerId + "@social.itda.com";
    }
}