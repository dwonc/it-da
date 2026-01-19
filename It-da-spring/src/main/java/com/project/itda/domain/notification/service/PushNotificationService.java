package com.project.itda.domain.notification.service;

import com.project.itda.domain.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 푸시 알림 서비스 (Stub)
 * - 현재는 로그만 출력
 * - 추후 FCM 연동 시 실제 구현으로 교체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    /**
     * 단일 사용자에게 푸시 알림 전송
     */
    @Async
    public void sendPushNotification(Long userId, String title, String body, NotificationType type, String linkUrl) {
        // TODO: FCM 연동 후 실제 푸시 전송 구현
        log.info("[PUSH] userId: {}, title: {}, type: {}", userId, title, type);
    }

    /**
     * 여러 사용자에게 푸시 알림 전송
     */
    @Async
    public void sendPushNotificationToUsers(List<Long> userIds, String title, String body, NotificationType type, String linkUrl) {
        for (Long userId : userIds) {
            sendPushNotification(userId, title, body, type, linkUrl);
        }
    }

    // ==================== 알림 유형별 푸시 메서드 ====================

    /**
     * 모임 참가 푸시 알림
     */
    @Async
    public void pushMeetingJoin(Long hostUserId, String participantName, Long meetingId, String meetingTitle) {
        log.info("[PUSH-MEETING] 모임 참가 알림 - hostUserId: {}, participant: {}, meetingId: {}",
                hostUserId, participantName, meetingId);
    }

    /**
     * 모임 D-1 리마인더 푸시 알림
     */
    @Async
    public void pushMeetingReminder(Long userId, Long meetingId, String meetingTitle, String meetingTime, String location) {
        log.info("[PUSH-MEETING] 리마인더 알림 - userId: {}, meetingId: {}, time: {}",
                userId, meetingId, meetingTime);
    }

    /**
     * 후기 작성 푸시 알림
     */
    @Async
    public void pushReview(Long hostUserId, String reviewerName, Long meetingId, String meetingTitle, int rating) {
        log.info("[PUSH-REVIEW] 후기 알림 - hostUserId: {}, reviewer: {}, rating: {}",
                hostUserId, reviewerName, rating);
    }

    /**
     * 팔로우 푸시 알림
     */
    @Async
    public void pushFollow(Long targetUserId, String followerName, Long followerId) {
        log.info("[PUSH-FOLLOW] 팔로우 알림 - targetUserId: {}, follower: {}",
                targetUserId, followerName);
    }

    /**
     * 배지 획득 푸시 알림
     */
    @Async
    public void pushBadge(Long userId, Long badgeId, String badgeName, String badgeDescription) {
        log.info("[PUSH-BADGE] 배지 알림 - userId: {}, badge: {}",
                userId, badgeName);
    }

    /**
     * 채팅 푸시 알림
     */
    @Async
    public void pushChat(Long userId, Long chatroomId, String senderName, String message) {
        log.info("[PUSH-CHAT] 채팅 알림 - userId: {}, sender: {}, chatroomId: {}",
                userId, senderName, chatroomId);
    }
}