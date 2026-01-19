// src/stores/useNotificationStore.ts

import { create } from 'zustand';
import { notificationApi } from '@/api/notification.api';
import type { NotificationResponseDTO } from '@/types/notification.types';
import { convertNotificationType } from '@/types/notification.types';

export interface Notification {
    id: string;
    backendId?: number;
    type: 'follow' | 'follow_request' | 'follow_accept' | 'message';
    title: string;
    text: string;
    time: string;
    isUnread: boolean;
    message: string;
    isRead: boolean;
    createdAt: string;
    fromUserId?: number;
    fromUsername?: string;
    fromProfileImage?: string;
    roomId?: number;
    senderId?: number;
    senderName?: string;
    senderProfileImage?: string;
    content?: string;
}

// ✅ 사용하지 않는 타입 별칭 제거됨

interface NotificationState {
    notifications: Notification[];
    unreadCount: number;
    isOpen: boolean;
    isLoading: boolean;
    hasMore: boolean;
    page: number;
    fetchNotifications: () => Promise<void>;
    fetchMoreNotifications: () => Promise<void>;
    refreshUnreadCount: () => Promise<void>;
    addFollowNotification: (data: {
        fromUserId: number;
        fromUsername: string;
        fromProfileImage?: string;
        toUserId?: number;
        type?: 'follow' | 'follow_request' | 'follow_accept';
        message?: string;
        newFollowerCount?: number;
    }) => void;
    addFollowRequestNotification: (data: {
        fromUserId: number;
        fromUsername: string;
        fromProfileImage?: string;
        toUserId?: number;
    }) => void;
    updateUserProfile: (userId: number, data: {
        username?: string;
        profileImage?: string;
    }) => void;
    addMessageNotification: (data: {
        roomId: number;
        senderId: number;
        senderName: string;
        senderProfileImage?: string;
        content: string;
    }) => void;
    markAsRead: (id: string) => void;
    markAllAsRead: () => void;
    removeNotification: (id: string) => void;
    clearAll: () => void;
    toggleDropdown: () => void;
    closeDropdown: () => void;
}

const formatTimeAgo = (date: Date): string => {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return '방금 전';
    if (diffMins < 60) return `${diffMins}분 전`;
    if (diffHours < 24) return `${diffHours}시간 전`;
    if (diffDays < 7) return `${diffDays}일 전`;
    return date.toLocaleDateString();
};

// ✅ 백엔드 응답 → 프론트엔드 Notification 변환 함수
const convertToNotification = (dto: NotificationResponseDTO): Notification => {
    const createdAt = new Date(dto.sentAt);
    const frontendType = convertNotificationType(dto.notificationType);

    return {
        id: `notif-${dto.notificationId}`,
        backendId: dto.notificationId,
        type: frontendType as Notification['type'],
        title: dto.title,
        text: dto.content,
        message: dto.content,
        time: formatTimeAgo(createdAt),
        isUnread: !dto.isRead,
        isRead: dto.isRead,
        createdAt: dto.sentAt,
        fromUserId: dto.fromUserId,
        fromUsername: dto.fromUsername,
        fromProfileImage: dto.fromProfileImage,
        roomId: dto.roomId,
        senderId: dto.senderId,
        senderName: dto.senderName,
        senderProfileImage: dto.senderProfileImage,
        content: dto.content,
    };
};

// ✅ 고유 ID 생성 헬퍼 함수 (중복 코드 제거)
const generateUniqueId = (prefix: string): string => {
    return `${prefix}-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
};

export const useNotificationStore = create<NotificationState>((set, get) => ({
    notifications: [],
    unreadCount: 0,
    isOpen: false,
    isLoading: false,
    hasMore: true,
    page: 0,

    // ✅ 알림 목록 조회 (백엔드 연동)
    fetchNotifications: async () => {
        const { isLoading } = get();
        if (isLoading) return;

        set({ isLoading: true });
        try {
            const response = await notificationApi.getNotifications(0, 20);
            const notifications = response.notifications.map(convertToNotification);

            set({
                notifications,
                unreadCount: response.unreadCount,
                hasMore: response.hasMore,
                page: 0,
                isLoading: false,
            });
            console.log('📬 알림 목록 조회 완료:', notifications.length);
        } catch (error) {
            console.error('❌ 알림 목록 조회 실패:', error);
            set({ isLoading: false });
        }
    },

    // ✅ 추가 알림 로드 (무한 스크롤용)
    fetchMoreNotifications: async () => {
        const { isLoading, hasMore, page } = get();
        if (isLoading || !hasMore) return;

        set({ isLoading: true });
        try {
            const nextPage = page + 1;
            const response = await notificationApi.getNotifications(nextPage, 20);
            const newNotifications = response.notifications.map(convertToNotification);

            set((state) => ({
                notifications: [...state.notifications, ...newNotifications],
                hasMore: response.hasMore,
                page: nextPage,
                isLoading: false,
            }));
        } catch (error) {
            console.error('❌ 추가 알림 로드 실패:', error);
            set({ isLoading: false });
        }
    },

    // ✅ 읽지 않은 알림 개수 갱신
    refreshUnreadCount: async () => {
        try {
            const unreadCount = await notificationApi.getUnreadCount();
            set({ unreadCount });
        } catch (error) {
            console.error('❌ 읽지 않은 알림 개수 조회 실패:', error);
        }
    },

    addFollowNotification: (data) => {
        const typeMap: { [key: string]: string } = {
            'follow': '님이 회원님을 팔로우했습니다.',
            'follow_request': '님이 팔로우를 요청했습니다.',
            'follow_accept': '님이 팔로우 요청을 수락했습니다.',
        };

        const notificationType = data.type || 'follow';
        const messageText = data.message || `${data.fromUsername}${typeMap[notificationType] || '님이 회원님을 팔로우했습니다.'}`;
        const now = new Date();

        const newNotification: Notification = {
            id: generateUniqueId('follow'),
            type: notificationType,
            title: `${data.fromUsername}님`,
            text: messageText,
            message: messageText,
            time: formatTimeAgo(now),
            isUnread: true,
            isRead: false,
            createdAt: now.toISOString(),
            fromUserId: data.fromUserId,
            fromUsername: data.fromUsername,
            fromProfileImage: data.fromProfileImage,
        };

        set((state) => ({
            notifications: [newNotification, ...state.notifications],
            unreadCount: state.unreadCount + 1,
        }));
    },

    addFollowRequestNotification: (data) => {
        get().addFollowNotification({
            ...data,
            type: 'follow_request',
        });
    },

    updateUserProfile: (userId, data) => {
        set((state) => ({
            notifications: state.notifications.map((n) => {
                if (n.fromUserId === userId) {
                    return {
                        ...n,
                        fromUsername: data.username ?? n.fromUsername,
                        fromProfileImage: data.profileImage ?? n.fromProfileImage,
                    };
                }
                if (n.senderId === userId) {
                    return {
                        ...n,
                        senderName: data.username ?? n.senderName,
                        senderProfileImage: data.profileImage ?? n.senderProfileImage,
                    };
                }
                return n;
            }),
        }));
    },

    addMessageNotification: (data) => {
        const now = new Date();
        const truncatedContent = data.content.length > 30
            ? data.content.substring(0, 30) + '...'
            : data.content;

        const messageText = `💬 ${truncatedContent}`;

        const newNotification: Notification = {
            id: generateUniqueId('message'),
            type: 'message',
            title: `${data.senderName}님의 새 메시지`,
            text: messageText,
            message: messageText,
            time: formatTimeAgo(now),
            isUnread: true,
            isRead: false,
            createdAt: now.toISOString(),
            roomId: data.roomId,
            senderId: data.senderId,
            senderName: data.senderName,
            senderProfileImage: data.senderProfileImage,
            content: data.content,
        };

        console.log('📬 메시지 알림 추가:', newNotification);

        set((state) => ({
            notifications: [newNotification, ...state.notifications],
            unreadCount: state.unreadCount + 1,
        }));
    },

    // ✅ 읽음 처리 (백엔드 연동)
    markAsRead: (id) => {
        const notification = get().notifications.find(n => n.id === id);

        set((state) => ({
            notifications: state.notifications.map((n) =>
                n.id === id ? { ...n, isRead: true, isUnread: false } : n
            ),
            unreadCount: state.notifications.find(n => n.id === id && !n.isRead)
                ? Math.max(0, state.unreadCount - 1)
                : state.unreadCount,
        }));

        if (notification?.backendId) {
            notificationApi.markAsRead(notification.backendId).catch((error) => {
                console.error('❌ 알림 읽음 처리 실패:', error);
            });
        }
    },

    // ✅ 모두 읽음 처리 (백엔드 연동)
    markAllAsRead: () => {
        set((state) => ({
            notifications: state.notifications.map((n) => ({
                ...n,
                isRead: true,
                isUnread: false
            })),
            unreadCount: 0,
        }));

        notificationApi.markAllAsRead().catch((error) => {
            console.error('❌ 모든 알림 읽음 처리 실패:', error);
        });
    },

    // ✅ 알림 삭제 (백엔드 연동)
    removeNotification: (id) => {
        const notification = get().notifications.find(n => n.id === id);

        set((state) => {
            const targetNotification = state.notifications.find(n => n.id === id);
            return {
                notifications: state.notifications.filter((n) => n.id !== id),
                unreadCount: targetNotification && !targetNotification.isRead
                    ? Math.max(0, state.unreadCount - 1)
                    : state.unreadCount,
            };
        });

        if (notification?.backendId) {
            notificationApi.deleteNotification(notification.backendId).catch((error) => {
                console.error('❌ 알림 삭제 실패:', error);
            });
        }
    },

    clearAll: () => {
        set({ notifications: [], unreadCount: 0 });
    },

    toggleDropdown: () => {
        const { isOpen, fetchNotifications } = get();
        if (!isOpen) {
            void fetchNotifications();
        }
        set((state) => ({ isOpen: !state.isOpen }));
    },

    closeDropdown: () => {
        set({ isOpen: false });
    },
}));