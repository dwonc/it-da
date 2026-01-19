package com.project.itda.domain.notification.dto.response;

import com.project.itda.domain.notification.entity.Notification;
import com.project.itda.domain.notification.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 알림 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long notificationId;
    private NotificationType notificationType;
    private String title;
    private String content;
    private String linkUrl;
    private Long relatedId;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String timeAgo;

    /**
     * Entity → DTO 변환
     */
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .linkUrl(notification.getLinkUrl())
                .relatedId(notification.getRelatedId())
                .isRead(notification.getIsRead())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .timeAgo(calculateTimeAgo(notification.getSentAt()))
                .build();
    }

    /**
     * 시간 차이 계산
     */
    private static String calculateTimeAgo(LocalDateTime sentAt) {
        if (sentAt == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(sentAt, now);
        long hours = ChronoUnit.HOURS.between(sentAt, now);
        long days = ChronoUnit.DAYS.between(sentAt, now);

        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        if (hours < 24) return hours + "시간 전";
        if (days == 1) return "어제";
        if (days < 7) return days + "일 전";
        if (days < 30) return (days / 7) + "주 전";
        return (days / 30) + "개월 전";
    }
}