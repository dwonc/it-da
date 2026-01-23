// src/main/java/com/project/itda/domain/badge/notification/BadgeNotificationPort.java
package com.project.itda.domain.badge.notification;

import com.project.itda.domain.badge.entity.Badge;

/**
 * 배지 획득 알림 발송 포트
 * 실제 Notification 모듈이 준비되면 구현체를 교체하면 됩니다.
 */
public interface BadgeNotificationPort {
    void sendBadgeUnlocked(Long userId, Badge badge);
}
