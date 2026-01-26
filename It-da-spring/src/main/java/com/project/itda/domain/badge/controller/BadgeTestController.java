// src/main/java/com/project/itda/domain/badge/controller/BadgeTestController.java
package com.project.itda.domain.badge.controller;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.entity.UserBadge;
import com.project.itda.domain.badge.notification.BadgeNotificationPort;
import com.project.itda.domain.badge.repository.BadgeRepository;
import com.project.itda.domain.badge.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ⚠️ 개발/테스트 환경에서만 사용!
 * 배지 획득 테스트용 컨트롤러
 */
@RestController
@RequestMapping("/api/test/badges")
@RequiredArgsConstructor
@Slf4j
public class BadgeTestController {

    private final BadgeNotificationPort badgeNotificationPort;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    /**
     * 🧪 배지 획득 테스트 (실제 DB 저장 + WebSocket 알림)
     *
     * POST http://localhost:8080/api/test/badges/unlock
     * Body: { "userId": 49, "badgeCode": "participate_1" }
     */
    @PostMapping("/unlock")
    public ResponseEntity<Map<String, Object>> testBadgeUnlock(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String badgeCode = request.get("badgeCode").toString();

        log.info("🧪 [TEST] 배지 획득 테스트. userId={}, badgeCode={}", userId, badgeCode);

        // 1. 배지 조회
        Badge badge = badgeRepository.findByBadgeCode(badgeCode)
                .orElseThrow(() -> new IllegalArgumentException("배지를 찾을 수 없습니다: " + badgeCode));

        // 2. UserBadge 조회
        Optional<UserBadge> existingOpt = userBadgeRepository.findByUserIdAndBadge_BadgeId(userId, badge.getBadgeId());

        if (existingOpt.isPresent()) {
            UserBadge userBadge = existingOpt.get();

            // 이미 획득한 경우
            if (Boolean.TRUE.equals(userBadge.getUnlocked())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "이미 획득한 배지입니다: " + badge.getBadgeName());
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // unlocked=false면 → unlock!
            userBadge.unlock();
            userBadgeRepository.save(userBadge);
            log.info("✅ 기존 배지 unlock 완료. userId={}, badge={}", userId, badge.getBadgeName());

        } else {
            // 레코드가 없으면 새로 생성
            UserBadge userBadge = UserBadge.builder()
                    .userId(userId)
                    .badge(badge)
                    .unlocked(true)
                    .progress(badge.getTargetValue())
                    .unlockedAt(LocalDateTime.now())
                    .build();
            userBadgeRepository.save(userBadge);
            log.info("✅ 새 배지 생성 및 unlock 완료. userId={}, badge={}", userId, badge.getBadgeName());
        }

        // 3. WebSocket 알림 전송!
        badgeNotificationPort.sendBadgeUnlocked(userId, badge);

        // 4. 성공 응답
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", "배지 획득 완료!");
        successResponse.put("userId", userId);
        successResponse.put("badgeName", badge.getBadgeName());
        successResponse.put("badgeIcon", badge.getIcon());

        return ResponseEntity.ok(successResponse);
    }

    /**
     * 🧪 사용 가능한 배지 코드 목록 조회
     */
    @GetMapping("/codes")
    public ResponseEntity<?> getBadgeCodes() {
        var badges = badgeRepository.findAll().stream()
                .map(b -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", b.getBadgeCode());
                    map.put("name", b.getBadgeName());
                    map.put("icon", b.getIcon() != null ? b.getIcon() : "🏅");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(badges);
    }
}