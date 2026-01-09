package com.project.itda.domain.admin.controller;

import com.project.itda.domain.admin.entity.AdminUser;
import com.project.itda.domain.admin.repository.AdminUserRepository;
import com.project.itda.domain.auth.service.SessionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminUserRepository adminUserRepository;
    private final SessionService sessionService;

    /**
     * 관리자 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> adminLogin(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        String email = request.get("email");
        String password = request.get("password");

        // 관리자 조회
        AdminUser admin = adminUserRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("이메일 또는 비밀번호가 일치하지 않습니다"));

        // 임시: 평문 비밀번호 비교 (실제로는 암호화 필요)
        if (!password.equals(admin.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // 관리자 세션 생성
        sessionService.createAdminSession(session, admin.getAdminId(), admin.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "관리자 로그인 성공");
        response.put("adminId", admin.getAdminId());
        response.put("name", admin.getName());
        response.put("email", admin.getEmail());
        response.put("role", admin.getRole());
        response.put("sessionId", session.getId());

        return ResponseEntity.ok(response);
    }
}