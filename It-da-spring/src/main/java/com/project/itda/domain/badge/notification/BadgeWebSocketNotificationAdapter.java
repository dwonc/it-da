// src/main/java/com/project/itda/domain/badge/notification/BadgeWebSocketNotificationAdapter.java
package com.project.itda.domain.badge.notification;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.notification.entity.Notification;
import com.project.itda.domain.notification.enums.NotificationType;
import com.project.itda.domain.notification.repository.NotificationRepository;
import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 배지 획득 시 WebSocket 알림 발송 + DB 저장
 * NoopBadgeNotificationPort 대신 이 구현체를 사용합니다.
 */
@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class BadgeWebSocketNotificationAdapter implements BadgeNotificationPort {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void sendBadgeUnlocked(Long userId, Badge badge) {
        try {
            // 1. User 엔티티 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + userId));

            // 2. DB에 알림 저장
            Notification notification = Notification.builder()
                    .user(user)
                    .notificationType(NotificationType.BADGE)
                    .title("🏆 새 배지 획득!")
                    .content(badge.getIcon() + " " + badge.getBadgeName() + " 배지를 획득했습니다!")
                    .relatedId(badge.getBadgeId())
                    .linkUrl("/mypage")
                    .isRead(false)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("✅ 배지 알림 DB 저장 완료. userId={}, badgeCode={}", userId, badge.getBadgeCode());

            // 3. WebSocket으로 실시간 알림 전송 (배지 전용 채널)
            Map<String, Object> badgePayload = new HashMap<>();
            badgePayload.put("type", "BADGE_UNLOCKED");
            badgePayload.put("notificationId", saved.getNotificationId());
            badgePayload.put("notificationType", "BADGE");
            badgePayload.put("title", "🏆 새 배지 획득!");
            badgePayload.put("content", badge.getIcon() + " " + badge.getBadgeName() + " 배지를 획득했습니다!");
            badgePayload.put("badgeId", badge.getBadgeId());
            badgePayload.put("badgeCode", badge.getBadgeCode());
            badgePayload.put("badgeName", badge.getBadgeName());
            badgePayload.put("badgeIcon", badge.getIcon());
            badgePayload.put("badgeGrade", badge.getGrade().name());
            badgePayload.put("badgeCategory", badge.getCategory().name());
            badgePayload.put("badgeDescription", badge.getDescription());
            badgePayload.put("linkUrl", "/mypage");
            badgePayload.put("sentAt", LocalDateTime.now().toString());
            badgePayload.put("isRead", false);

            messagingTemplate.convertAndSend("/topic/badge/" + userId, badgePayload);
            log.info("📡 배지 WebSocket 알림 전송 완료. userId={}, badge={}", userId, badge.getBadgeName());

            // 4. 알림벨용 WebSocket 전송 (/topic/notification/{userId})
            Map<String, Object> notifPayload = new HashMap<>();
            notifPayload.put("notificationId", saved.getNotificationId());
            notifPayload.put("notificationType", "BADGE");
            notifPayload.put("title", "🏆 새 배지 획득!");
            notifPayload.put("content", badge.getIcon() + " " + badge.getBadgeName() + " 배지를 획득했습니다!");
            notifPayload.put("senderId", null);
            notifPayload.put("senderName", "시스템");
            notifPayload.put("senderProfileImage", null);
            notifPayload.put("relatedId", badge.getBadgeId());
            notifPayload.put("linkUrl", "/mypage");
            notifPayload.put("sentAt", LocalDateTime.now().toString());
            notifPayload.put("isRead", false);
            notifPayload.put("timeAgo", "방금 전");

            messagingTemplate.convertAndSend("/topic/notification/" + userId, notifPayload);
            log.info("🔔 알림벨 WebSocket 전송 완료. userId={}", userId);

        } catch (Exception e) {
            log.error("❌ 배지 알림 전송 실패. userId={}, badge={}", userId, badge.getBadgeCode(), e);
        }
    }
}