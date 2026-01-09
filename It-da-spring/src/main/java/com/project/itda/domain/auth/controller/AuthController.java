package com.project.itda.domain.auth.controller;

import com.project.itda.domain.auth.dto.request.LoginRequest;
import com.project.itda.domain.auth.dto.request.SignupRequest;
import com.project.itda.domain.auth.dto.request.SocialLoginRequest;
import com.project.itda.domain.auth.dto.response.LoginResponse;
import com.project.itda.domain.auth.dto.response.SessionInfoResponse;
import com.project.itda.domain.auth.dto.response.SignupResponse;
import com.project.itda.domain.auth.service.AuthService;
import com.project.itda.domain.auth.service.OAuth2Service;
import com.project.itda.domain.auth.service.SessionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oauth2Service;
    private final SessionService sessionService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request) {

        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 일반 로그인 (이메일 + 비밀번호)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {

        LoginResponse response = authService.login(request, session);
        return ResponseEntity.ok(response);
    }

    /**
     * 소셜 로그인 (카카오, 네이버, 구글)
     */
    @PostMapping("/social-login")
    public ResponseEntity<LoginResponse> socialLogin(
            @Valid @RequestBody SocialLoginRequest request,
            HttpSession session) {

        LoginResponse response = oauth2Service.socialLogin(request, session);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        authService.logout(session);

        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃 성공");

        return ResponseEntity.ok(response);
    }

    /**
     * 세션 정보 조회
     */
    @GetMapping("/session")
    public ResponseEntity<SessionInfoResponse> getSessionInfo(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String username = sessionService.getUsernameFromSession(session);
        String userStatus = sessionService.getUserStatusFromSession(session);

        SessionInfoResponse response = SessionInfoResponse.builder()
                .sessionId(session.getId())
                .userId(userId)
                .username(username)
                .userStatus(userStatus)
                .creationTime(new Date(session.getCreationTime()))
                .lastAccessedTime(new Date(session.getLastAccessedTime()))
                .maxInactiveInterval(session.getMaxInactiveInterval())
                .isLoggedIn(userId != null)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 상태 확인
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkLogin(HttpSession session) {
        boolean isLoggedIn = sessionService.isLoggedIn(session);
        Long userId = isLoggedIn ? (Long) session.getAttribute("userId") : null;

        Map<String, Object> response = new HashMap<>();
        response.put("isLoggedIn", isLoggedIn);
        response.put("userId", userId);

        return ResponseEntity.ok(response);
    }
}