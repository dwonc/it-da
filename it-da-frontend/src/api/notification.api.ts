// src/api/notification.api.ts

import apiClient from '@/api/client';
import type {
    NotificationResponseDTO,
    NotificationListResponse,
    UnreadCountResponse,
    MarkAsReadRequest,
} from '@/types/notification.types';

const BASE_URL = '/api/notifications';

/** 알림 API */
export const notificationApi = {
    /**
     * 알림 목록 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     */
    getNotifications: async (page: number = 0, size: number = 20): Promise<NotificationListResponse> => {
        const response = await apiClient.get<NotificationListResponse>(BASE_URL, {
            params: { page, size }
        });
        return response.data;
    },

    /**
     * 읽지 않은 알림 개수 조회
     */
    getUnreadCount: async (): Promise<number> => {
        const response = await apiClient.get<UnreadCountResponse>(`${BASE_URL}/unread-count`);
        return response.data.unreadCount;
    },

    /**
     * 단일 알림 읽음 처리
     * @param notificationId 알림 ID
     */
    markAsRead: async (notificationId: number): Promise<void> => {
        await apiClient.patch(`${BASE_URL}/${notificationId}/read`);
    },

    /**
     * 여러 알림 읽음 처리
     * @param notificationIds 알림 ID 배열
     */
    markMultipleAsRead: async (notificationIds: number[]): Promise<void> => {
        await apiClient.patch(`${BASE_URL}/read`, { notificationIds } as MarkAsReadRequest);
    },

    /**
     * 모든 알림 읽음 처리
     */
    markAllAsRead: async (): Promise<void> => {
        await apiClient.patch(`${BASE_URL}/read-all`);
    },

    /**
     * 알림 삭제
     * @param notificationId 알림 ID
     */
    deleteNotification: async (notificationId: number): Promise<void> => {
        await apiClient.delete(`${BASE_URL}/${notificationId}`);
    },

    /**
     * 모든 알림 삭제
     */
    deleteAllNotifications: async (): Promise<void> => {
        await apiClient.delete(BASE_URL);
    },
};

export default notificationApi;