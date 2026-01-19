// src/components/notification/NotificationDropdown.tsx

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useNotificationStore, Notification } from '@/stores/useNotificationStore';
import { useAuthStore } from '@/stores/useAuthStore';
import apiClient from '@/api/client';
import './NotificationDropdown.css';

interface NotificationDropdownProps {
    isOpen?: boolean;
    onClose?: () => void;
}

const NotificationDropdown: React.FC<NotificationDropdownProps> = ({ isOpen: propIsOpen, onClose: propOnClose }) => {
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuthStore();
    const {
        notifications,
        isOpen: storeIsOpen,
        isLoading,           // ✅ 로딩 상태 추가
        hasMore,             // ✅ 더 불러올 알림 있는지
        closeDropdown: storeCloseDropdown,
        fetchNotifications,  // ✅ 알림 목록 조회
        fetchMoreNotifications, // ✅ 추가 로드
        markAsRead,
        markAllAsRead,
        removeNotification
    } = useNotificationStore();

    const [loadingId, setLoadingId] = useState<string | null>(null);
    const listRef = useRef<HTMLDivElement>(null); // ✅ 무한 스크롤용 ref

    const isOpen = propIsOpen !== undefined ? propIsOpen : storeIsOpen;
    const onClose = propOnClose || storeCloseDropdown;

    // ✅ 드롭다운 열릴 때 알림 목록 조회
    useEffect(() => {
        if (isOpen) {
            fetchNotifications();
        }
    }, [isOpen, fetchNotifications]);

    // ✅ 무한 스크롤 핸들러
    const handleScroll = useCallback(() => {
        if (!listRef.current || isLoading || !hasMore) return;

        const { scrollTop, scrollHeight, clientHeight } = listRef.current;
        // 스크롤이 하단 100px 이내에 도달하면 추가 로드
        if (scrollHeight - scrollTop - clientHeight < 100) {
            fetchMoreNotifications();
        }
    }, [isLoading, hasMore, fetchMoreNotifications]);

    // ✅ 스크롤 이벤트 리스너 등록
    useEffect(() => {
        const listElement = listRef.current;
        if (listElement) {
            listElement.addEventListener('scroll', handleScroll);
            return () => listElement.removeEventListener('scroll', handleScroll);
        }
    }, [handleScroll]);

    if (!isOpen) return null;

    const getProfileImageUrl = (url?: string) => {
        if (!url) return null;
        if (url.startsWith('http')) return url;
        return `http://localhost:8080${url}`;
    };

    // ✅ 팔로우 요청 수락
    const handleAcceptFollow = async (e: React.MouseEvent, notification: Notification) => {
        e.stopPropagation();
        if (!user?.userId || !notification.fromUserId) return;

        setLoadingId(notification.id);
        try {
            await apiClient.post(`/api/users/${user.userId}/follow-request/${notification.fromUserId}/accept`);
            removeNotification(notification.id);
            alert(`${notification.fromUsername}님의 팔로우 요청을 수락했습니다!`);
        } catch (error) {
            console.error('팔로우 요청 수락 실패:', error);
            alert('팔로우 요청 수락에 실패했습니다.');
        } finally {
            setLoadingId(null);
        }
    };

    // ✅ 팔로우 요청 거절
    const handleRejectFollow = async (e: React.MouseEvent, notification: Notification) => {
        e.stopPropagation();
        if (!user?.userId || !notification.fromUserId) return;

        setLoadingId(notification.id);
        try {
            await apiClient.post(`/api/users/${user.userId}/follow-request/${notification.fromUserId}/reject`);
            removeNotification(notification.id);
            alert(`${notification.fromUsername}님의 팔로우 요청을 거절했습니다.`);
        } catch (error) {
            console.error('팔로우 요청 거절 실패:', error);
            alert('팔로우 요청 거절에 실패했습니다.');
        } finally {
            setLoadingId(null);
        }
    };

    const handleNotificationClick = (notification: Notification) => {
        // 팔로우 요청은 클릭해도 이동 안 함 (버튼으로 처리)
        if (notification.type === 'follow_request') return;

        markAsRead(notification.id);
        onClose();

        // ✅ 메시지 알림 클릭 시 채팅방으로 이동
        if (notification.type === 'message' && notification.roomId) {
            const targetPath = `/user-chat/${notification.roomId}`;
            if (location.pathname === targetPath) {
                window.location.reload();
            } else {
                navigate(targetPath);
            }
        } else if (notification.fromUserId) {
            const targetPath = `/profile/id/${notification.fromUserId}`;
            if (location.pathname === targetPath) {
                window.location.reload();
            } else {
                navigate(targetPath);
            }
        }
    };

    const getProfileInfo = (notification: Notification) => {
        if (notification.type === 'message') {
            return {
                image: notification.senderProfileImage,
                name: notification.senderName || '알 수 없음'
            };
        }
        return {
            image: notification.fromProfileImage,
            name: notification.fromUsername || '알 수 없음'
        };
    };

    const getNotificationIcon = (notification: Notification) => {
        switch (notification.type) {
            case 'message': return '💬';
            case 'follow': return '👤';
            case 'follow_request': return '🔔';
            case 'follow_accept': return '✅';
            default: return '🔔';
        }
    };

    return (
        <>
            <div className="notification-overlay" onClick={onClose} />
            <div className="notification-dropdown">
                <div className="notification-header">
                    <h3>알림</h3>
                    {notifications.filter(n => n.isUnread).length > 0 && (
                        <button className="mark-all-read-btn" onClick={() => markAllAsRead()}>모두 읽음</button>
                    )}
                </div>

                <div className="notification-list" ref={listRef}>
                    {/* ✅ 초기 로딩 상태 */}
                    {isLoading && notifications.length === 0 ? (
                        <div className="notification-loading">
                            <span className="loading-spinner">⏳</span>
                            <p>알림을 불러오는 중...</p>
                        </div>
                    ) : notifications.length === 0 ? (
                        <div className="notification-empty">
                            <span className="empty-icon">🔔</span>
                            <p>알림이 없습니다</p>
                        </div>
                    ) : (
                        <>
                            {notifications.map((notification) => {
                                const profile = getProfileInfo(notification);
                                return (
                                    <div
                                        key={notification.id}
                                        className={`notification-item ${notification.type} ${notification.isUnread ? 'unread' : ''}`}
                                        onClick={() => handleNotificationClick(notification)}
                                    >
                                        <div className="notification-avatar">
                                            {getProfileImageUrl(profile.image) ? (
                                                <img src={getProfileImageUrl(profile.image)!} alt={profile.name} />
                                            ) : (
                                                <div className="avatar-placeholder">{profile.name.charAt(0).toUpperCase()}</div>
                                            )}
                                            <span className="notification-type-icon">{getNotificationIcon(notification)}</span>
                                        </div>

                                        <div className="notification-content">
                                            <div className="notification-title">{notification.title}</div>
                                            <div className="notification-text">{notification.text}</div>

                                            {/* ✅ 팔로우 요청일 때만 수락/거절 버튼 표시 */}
                                            {notification.type === 'follow_request' && (
                                                <div className="notif-actions">
                                                    <button
                                                        className="notif-accept-btn"
                                                        onClick={(e) => handleAcceptFollow(e, notification)}
                                                        disabled={loadingId === notification.id}
                                                    >
                                                        {loadingId === notification.id ? '...' : '수락'}
                                                    </button>
                                                    <button
                                                        className="notif-reject-btn"
                                                        onClick={(e) => handleRejectFollow(e, notification)}
                                                        disabled={loadingId === notification.id}
                                                    >
                                                        {loadingId === notification.id ? '...' : '거절'}
                                                    </button>
                                                </div>
                                            )}

                                            <div className="notification-time">{notification.time}</div>
                                        </div>

                                        <button
                                            className="notification-delete-btn"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                removeNotification(notification.id);
                                            }}
                                        >✕</button>
                                    </div>
                                );
                            })}

                            {/* ✅ 추가 로딩 인디케이터 */}
                            {isLoading && notifications.length > 0 && (
                                <div className="notification-loading-more">
                                    <span>더 불러오는 중...</span>
                                </div>
                            )}

                            {/* ✅ 더 이상 알림 없음 표시 */}
                            {!hasMore && notifications.length > 0 && (
                                <div className="notification-end">
                                    <span>모든 알림을 확인했습니다</span>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>
        </>
    );
};

export default NotificationDropdown;
