// src/types/notification.types.ts

/** 백엔드 알림 타입 (DB ENUM) */
export type NotificationTypeBackend = 'MEETING' | 'CHAT' | 'REVIEW' | 'BADGE' | 'SYSTEM' | 'FOLLOW' | 'FOLLOW_REQUEST' | 'FOLLOW_ACCEPT' | 'MESSAGE';

/** 프론트엔드 알림 타입 */
export type NotificationTypeFrontend = 'follow' | 'follow_request' | 'follow_accept' | 'message' | 'meeting' | 'review' | 'badge' | 'system';

/** 백엔드에서 받아오는 알림 응답 DTO */
export interface NotificationResponseDTO {
    notificationId: number;
    userId: number;
    notificationType: NotificationTypeBackend;
    title: string;
    content: string;
    linkUrl?: string;
    relatedId?: number;
    isRead: boolean;
    sentAt: string;
    readAt?: string;
    // 추가 필드 (팔로우/메시지 관련)
    fromUserId?: number;
    fromUsername?: string;
    fromProfileImage?: string;
    roomId?: number;
    senderId?: number;
    senderName?: string;
    senderProfileImage?: string;
}

/** 알림 목록 응답 */
export interface NotificationListResponse {
    notifications: NotificationResponseDTO[];
    unreadCount: number;
    totalCount: number;
    hasMore: boolean;
}

/** 읽지 않은 알림 개수 응답 */
export interface UnreadCountResponse {
    unreadCount: number;
}

/** 알림 읽음 처리 요청 */
export interface MarkAsReadRequest {
    notificationIds: number[];
}

/** 백엔드 타입 → 프론트엔드 타입 변환 */
export const convertNotificationType = (backendType: NotificationTypeBackend): NotificationTypeFrontend => {
    const typeMap: Record<NotificationTypeBackend, NotificationTypeFrontend> = {
        'FOLLOW': 'follow',
        'FOLLOW_REQUEST': 'follow_request',
        'FOLLOW_ACCEPT': 'follow_accept',
        'MESSAGE': 'message',
        'CHAT': 'message',
        'MEETING': 'meeting',
        'REVIEW': 'review',
        'BADGE': 'badge',
        'SYSTEM': 'system',
    };
    return typeMap[backendType] || 'system';
};