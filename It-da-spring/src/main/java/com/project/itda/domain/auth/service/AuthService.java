package com.project.itda.domain.auth.service;

import com.project.itda.domain.auth.dto.request.LoginRequest;
import com.project.itda.domain.auth.dto.request.SignupRequest;
import com.project.itda.domain.auth.dto.response.LoginResponse;
import com.project.itda.domain.auth.dto.response.SignupResponse;
import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.enums.UserStatus;
import com.project.itda.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    /**
     * 회원가입
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        // 2. 전화번호 중복 체크
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다");
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. User 생성
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .username(request.getUsername())
                .phone(request.getPhone())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        log.info("회원가입 성공: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        return SignupResponse.from(savedUser);
    }

    /**
     * 일반 로그인 (이메일 + 비밀번호)
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpSession session) {
        log.info("로그인 시도: email={}", request.getEmail());

        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("이메일 또는 비밀번호가 일치하지 않습니다"));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("비밀번호 불일치: email={}", request.getEmail());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // 3. 계정 상태 확인
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("비활성 계정 로그인 시도: userId={}, status={}", user.getUserId(), user.getStatus());
            throw new IllegalStateException("비활성화된 계정입니다. 고객센터에 문의해주세요.");
        }

        // 4. 마지막 로그인 시간 업데이트
        user.updateLastLogin();

        // 5. 세션에 사용자 정보 저장
        sessionService.createUserSession(session, user);

        log.info("로그인 성공: userId={}, email={}", user.getUserId(), user.getEmail());

        // 6. 응답 반환
        return LoginResponse.from(user, session.getId());
    }

    /**
     * 로그아웃
     */
    public void logout(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        log.info("로그아웃: userId={}", userId);

        session.invalidate();
    }

    /**
     * 현재 로그인한 사용자 조회
     */
    public User getCurrentUser(HttpSession session) {
        Long userId = sessionService.getUserIdFromSession(session);

        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));
    }

    /**
     * 현재 로그인한 사용자 ID 조회
     */
    public Long getCurrentUserId(HttpSession session) {
        return sessionService.getUserIdFromSession(session);
    }
}