// src/main/java/com/project/itda/domain/badge/controller/BadgeController.java
package com.project.itda.domain.badge.controller;

import com.project.itda.domain.badge.dto.response.BadgeProgressResponse;
import com.project.itda.domain.badge.dto.response.UserBadgeResponse;
import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 인증 방식이 아직 확정되지 않았으므로,
 * 임시로 X-User-Id 헤더에서 userId를 받도록 구현했습니다.
 *
 * 나중에 Security 확정되면 @AuthenticationPrincipal 또는 SecurityUtil로 교체하시면 됩니다.
 */
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ResponseEntity<List<UserBadgeResponse>> getUserBadges(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(badgeService.getUserBadges(userId));
    }

    @GetMapping("/unlocked")
    public ResponseEntity<List<UserBadgeResponse>> getUnlockedBadges(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(badgeService.getUnlockedBadges(userId));
    }

    @PostMapping("/{badgeCode}/update")
    public ResponseEntity<BadgeProgressResponse> updateBadgeProgress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String badgeCode
    ) {
        return ResponseEntity.ok(badgeService.updateBadgeProgress(userId, badgeCode));
    }

    @PostMapping("/update-all")
    public ResponseEntity<List<Badge>> updateAll(
            @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(badgeService.updateAllBadgeProgress(userId));
    }
}
