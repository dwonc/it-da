package com.project.itda.domain.auth.service;

import com.project.itda.domain.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SessionService {

    // 세션 키 상수
    private static final String SESSION_USER_ID = "userId";
    private static final String SESSION_USERNAME = "username";
    private static final String SESSION_USER_STATUS = "userStatus";
    private static final String SESSION_ADMIN_ID = "adminId";

    /**
     * 사용자 세션 생성
     */
    public void createUserSession(HttpSession session, User user) {
        session.setAttribute(SESSION_USER_ID, user.getUserId());
        session.setAttribute(SESSION_USERNAME, user.getUsername());
        session.setAttribute(SESSION_USER_STATUS, user.getStatus().name());

        log.debug("User session created: userId={}, username={}", user.getUserId(), user.getUsername());
    }

    /**
     * 관리자 세션 생성 (Admin 로그인용)
     */
    public void createAdminSession(HttpSession session, Long adminId, String adminName) {
        session.setAttribute(SESSION_ADMIN_ID, adminId);
        session.setAttribute(SESSION_USERNAME, adminName);

        log.debug("Admin session created: adminId={}, adminName={}", adminId, adminName);
    }

    /**
     * 세션에서 사용자 ID 조회
     */
    public Long getUserIdFromSession(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new IllegalStateException("로그인이 필요합니다");
        }
        return userId;
    }

    /**
     * 세션에서 사용자 이름 조회
     */
    public String getUsernameFromSession(HttpSession session) {
        return (String) session.getAttribute(SESSION_USERNAME);
    }

    /**
     * 세션에서 사용자 상태 조회
     */
    public String getUserStatusFromSession(HttpSession session) {
        return (String) session.getAttribute(SESSION_USER_STATUS);
    }

    /**
     * 세션에서 관리자 ID 조회
     */
    public Long getAdminIdFromSession(HttpSession session) {
        Long adminId = (Long) session.getAttribute(SESSION_ADMIN_ID);
        if (adminId == null) {
            throw new IllegalStateException("관리자 권한이 필요합니다");
        }
        return adminId;
    }

    /**
     * 로그인 상태 확인
     */
    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(SESSION_USER_ID) != null;
    }

    /**
     * 관리자 로그인 상태 확인
     */
    public boolean isAdminLoggedIn(HttpSession session) {
        return session.getAttribute(SESSION_ADMIN_ID) != null;
    }

    /**
     * 세션 무효화
     */
    public void invalidateSession(HttpSession session) {
        session.invalidate();
        log.debug("Session invalidated");
    }
}