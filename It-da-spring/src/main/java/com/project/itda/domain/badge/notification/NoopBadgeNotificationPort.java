// src/main/java/com/project/itda/domain/badge/notification/NoopBadgeNotificationPort.java
package com.project.itda.domain.badge.notification;

import com.project.itda.domain.badge.entity.Badge;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 지금은 알림 모듈이 없으므로 아무것도 하지 않는 구현체
 */
@Component
@Primary
public class NoopBadgeNotificationPort implements BadgeNotificationPort {

    @Override
    public void sendBadgeUnlocked(Long userId, Badge badge) {
        // intentionally empty
    }
}
