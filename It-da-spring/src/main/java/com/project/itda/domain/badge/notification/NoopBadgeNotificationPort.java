// src/main/java/com/project/itda/domain/badge/notification/NoopBadgeNotificationPort.java
package com.project.itda.domain.badge.notification;

import com.project.itda.domain.badge.entity.Badge;
import org.springframework.stereotype.Component;

/**
 * 지금은 알림 모듈이 없으므로 아무것도 하지 않는 구현체
 * ⚠️ @Primary 제거됨 - BadgeWebSocketNotificationAdapter가 우선 사용됨
 */
@Component
public class NoopBadgeNotificationPort implements BadgeNotificationPort {

    @Override
    public void sendBadgeUnlocked(Long userId, Badge badge) {
        // intentionally empty
    }
}