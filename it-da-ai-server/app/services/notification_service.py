"""
알림 서비스 (Business Logic)
알림 생성, 조회, 읽음 처리, 삭제 등 실제 작업 수행
"""

from typing import List, Optional
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from sqlalchemy import and_, desc

# 스키마 import (schemas/notification.py에서)
from app.schemas.notification import (
    NotificationCreate,
    NotificationResponse,
    NotificationListResponse,
    UnreadCountResponse
)

# 모델 import (models/notification_model.py에서)
from app.models.notification_model import Notification, NotificationTypeEnum


# ========================================
# NotificationService 클래스
# ========================================

class NotificationService:
    """
    알림 서비스

    모든 알림 관련 비즈니스 로직 처리:
    - 알림 생성
    - 알림 목록 조회
    - 읽음 처리
    - 삭제
    - 통계
    """

    def __init__(self, db: Session):
        """
        초기화

        Args:
            db: 데이터베이스 세션
        """
        self.db = db


    # ========================================
    # 1. 알림 생성
    # ========================================

    def create_notification(
            self,
            notification_data: NotificationCreate
    ) -> NotificationResponse:
        """
        새 알림 생성

        Args:
            notification_data: 알림 생성 요청 데이터

        Returns:
            생성된 알림 정보

        Example:
            >>> service = NotificationService(db)
            >>> data = NotificationCreate(
            ...     user_id=1,
            ...     notification_type="BADGE",
            ...     title="🏆 배지 획득!",
            ...     content="열정러 배지를 획득했어요!"
            ... )
            >>> result = service.create_notification(data)
        """

        # 1. Notification 객체 생성
        notification = Notification(
            user_id=notification_data.user_id,
            notification_type=NotificationTypeEnum(notification_data.notification_type),
            title=notification_data.title,
            content=notification_data.content,
            link_url=notification_data.link_url,
            related_id=notification_data.related_id,
            is_read=False,
            sent_at=datetime.utcnow()
        )

        # 2. 데이터베이스에 저장
        self.db.add(notification)
        self.db.commit()
        self.db.refresh(notification)

        # 3. Response 객체로 변환해서 반환
        return NotificationResponse(
            notification_id=notification.notification_id,
            user_id=notification.user_id,
            notification_type=notification.notification_type.value,
            title=notification.title,
            content=notification.content,
            link_url=notification.link_url,
            related_id=notification.related_id,
            is_read=notification.is_read,
            sent_at=notification.sent_at,
            read_at=notification.read_at
        )


    # ========================================
    # 2. 알림 목록 조회
    # ========================================

    def get_notifications(
            self,
            user_id: int,
            unread_only: bool = False,
            limit: int = 20,
            offset: int = 0
    ) -> NotificationListResponse:
        """
        사용자의 알림 목록 조회

        Args:
            user_id: 사용자 ID
            unread_only: True면 읽지 않은 알림만, False면 전체
            limit: 한 번에 가져올 개수 (기본 20개)
            offset: 건너뛸 개수 (페이지네이션용)

        Returns:
            알림 목록 + 통계

        Example:
            >>> # 전체 알림 조회
            >>> result = service.get_notifications(user_id=1)
            >>>
            >>> # 읽지 않은 알림만 조회
            >>> result = service.get_notifications(user_id=1, unread_only=True)
        """

        # 1. 기본 쿼리 (user_id로 필터링)
        query = self.db.query(Notification).filter(
            Notification.user_id == user_id
        )

        # 2. 읽지 않은 것만? (옵션)
        if unread_only:
            query = query.filter(Notification.is_read == False)

        # 3. 최신순 정렬
        query = query.order_by(desc(Notification.sent_at))

        # 4. 전체 개수 세기 (페이지네이션용)
        total_count = query.count()

        # 5. 읽지 않은 알림 개수 세기
        unread_count = self.db.query(Notification).filter(
            and_(
                Notification.user_id == user_id,
                Notification.is_read == False
            )
        ).count()

        # 6. 페이지네이션 적용
        notifications = query.offset(offset).limit(limit).all()

        # 7. Response 객체로 변환
        notification_responses = [
            NotificationResponse(
                notification_id=n.notification_id,
                user_id=n.user_id,
                notification_type=n.notification_type.value,
                title=n.title,
                content=n.content,
                link_url=n.link_url,
                related_id=n.related_id,
                is_read=n.is_read,
                sent_at=n.sent_at,
                read_at=n.read_at
            )
            for n in notifications
        ]

        # 8. 최종 결과 반환
        return NotificationListResponse(
            total_count=total_count,
            unread_count=unread_count,
            notifications=notification_responses
        )


    # ========================================
    # 3. 알림 1개 조회
    # ========================================

    def get_notification_by_id(
            self,
            notification_id: int,
            user_id: int
    ) -> Optional[NotificationResponse]:
        """
        특정 알림 1개 조회

        Args:
            notification_id: 알림 ID
            user_id: 사용자 ID (본인 알림만 조회 가능)

        Returns:
            알림 정보 (없으면 None)
        """

        notification = self.db.query(Notification).filter(
            and_(
                Notification.notification_id == notification_id,
                Notification.user_id == user_id
            )
        ).first()

        if not notification:
            return None

        return NotificationResponse(
            notification_id=notification.notification_id,
            user_id=notification.user_id,
            notification_type=notification.notification_type.value,
            title=notification.title,
            content=notification.content,
            link_url=notification.link_url,
            related_id=notification.related_id,
            is_read=notification.is_read,
            sent_at=notification.sent_at,
            read_at=notification.read_at
        )


    # ========================================
    # 4. 읽음 처리
    # ========================================

    def mark_as_read(
            self,
            notification_ids: List[int],
            user_id: int
    ) -> int:
        """
        알림 읽음 처리

        Args:
            notification_ids: 읽음 처리할 알림 ID 목록
            user_id: 사용자 ID (본인 알림만 처리 가능)

        Returns:
            실제로 읽음 처리된 개수

        Example:
            >>> # 알림 3개 읽음 처리
            >>> count = service.mark_as_read([1, 2, 3], user_id=1)
            >>> print(f"{count}개 읽음 처리 완료")
        """

        # 1. 해당 알림들 조회
        notifications = self.db.query(Notification).filter(
            and_(
                Notification.notification_id.in_(notification_ids),
                Notification.user_id == user_id,
                Notification.is_read == False  # 아직 안 읽은 것만
            )
        ).all()

        # 2. 읽음 처리
        affected_count = 0
        current_time = datetime.utcnow()

        for notification in notifications:
            notification.is_read = True
            notification.read_at = current_time
            affected_count += 1

        # 3. 저장
        self.db.commit()

        return affected_count


    # ========================================
    # 5. 전체 읽음 처리
    # ========================================

    def mark_all_as_read(self, user_id: int) -> int:
        """
        사용자의 모든 알림 읽음 처리

        Args:
            user_id: 사용자 ID

        Returns:
            읽음 처리된 개수
        """

        # 읽지 않은 알림 모두 조회
        notifications = self.db.query(Notification).filter(
            and_(
                Notification.user_id == user_id,
                Notification.is_read == False
            )
        ).all()

        # 읽음 처리
        current_time = datetime.utcnow()
        for notification in notifications:
            notification.is_read = True
            notification.read_at = current_time

        self.db.commit()

        return len(notifications)


    # ========================================
    # 6. 알림 삭제
    # ========================================

    def delete_notification(
            self,
            notification_id: int,
            user_id: int
    ) -> bool:
        """
        알림 삭제

        Args:
            notification_id: 삭제할 알림 ID
            user_id: 사용자 ID (본인 알림만 삭제 가능)

        Returns:
            성공 여부
        """

        notification = self.db.query(Notification).filter(
            and_(
                Notification.notification_id == notification_id,
                Notification.user_id == user_id
            )
        ).first()

        if not notification:
            return False

        self.db.delete(notification)
        self.db.commit()

        return True


    # ========================================
    # 7. 여러 알림 삭제
    # ========================================

    def delete_notifications(
            self,
            notification_ids: List[int],
            user_id: int
    ) -> int:
        """
        여러 알림 한 번에 삭제

        Args:
            notification_ids: 삭제할 알림 ID 목록
            user_id: 사용자 ID

        Returns:
            삭제된 개수
        """

        notifications = self.db.query(Notification).filter(
            and_(
                Notification.notification_id.in_(notification_ids),
                Notification.user_id == user_id
            )
        ).all()

        for notification in notifications:
            self.db.delete(notification)

        self.db.commit()

        return len(notifications)


    # ========================================
    # 8. 읽지 않은 알림 개수 조회
    # ========================================

    def get_unread_count(self, user_id: int) -> UnreadCountResponse:
        """
        읽지 않은 알림 개수 조회

        Args:
            user_id: 사용자 ID

        Returns:
            읽지 않은 알림 개수

        Example:
            >>> result = service.get_unread_count(user_id=1)
            >>> print(f"읽지 않은 알림: {result.unread_count}개")
        """

        count = self.db.query(Notification).filter(
            and_(
                Notification.user_id == user_id,
                Notification.is_read == False
            )
        ).count()

        return UnreadCountResponse(unread_count=count)


    # ========================================
    # 9. 오래된 알림 자동 삭제 (선택 사항)
    # ========================================

    def delete_old_notifications(self, days: int = 30) -> int:
        """
        오래된 알림 자동 삭제
        (30일 지난 읽은 알림 삭제)

        Args:
            days: 며칠 이전 알림 삭제할지 (기본 30일)

        Returns:
            삭제된 개수
        """

        cutoff_date = datetime.utcnow() - timedelta(days=days)

        old_notifications = self.db.query(Notification).filter(
            and_(
                Notification.is_read == True,
                Notification.sent_at < cutoff_date
            )
        ).all()

        for notification in old_notifications:
            self.db.delete(notification)

        self.db.commit()

        return len(old_notifications)


# ========================================
# 실생활 비유로 이해하기 🏠
# ========================================

"""
📬 우편함 관리하는 사람 (NotificationService)

1. create_notification (편지 만들어서 넣기)
   - 새 편지를 작성해서 우편함에 넣는다
   - 예: "배지 획득 축하" 편지 작성 → 우편함에 넣기

2. get_notifications (우편함 확인하기)
   - 우편함 열어서 편지들 확인
   - 옵션: 안 읽은 편지만 / 전체 편지
   - 최신 편지가 위에 오게 정렬

3. mark_as_read (편지 읽음 표시하기)
   - 편지 읽었으니 "읽음" 스티커 붙이기
   - 읽은 시각도 기록

4. delete_notification (편지 버리기)
   - 필요 없는 편지 쓰레기통에 버리기

5. get_unread_count (안 읽은 편지 개수 세기)
   - "아직 안 읽은 편지 5개 있어요!"

6. delete_old_notifications (오래된 편지 청소)
   - 30일 지난 읽은 편지는 자동으로 버리기
   - 우편함이 너무 가득 차지 않게!
"""


# ========================================
# 사용 예시
# ========================================

"""
# FastAPI 라우터에서 사용하는 방법

@router.post("/notifications")
def create_notification(
    data: NotificationCreate,
    db: Session = Depends(get_db)
):
    service = NotificationService(db)
    result = service.create_notification(data)
    return result

@router.get("/notifications")
def get_notifications(
    user_id: int,
    unread_only: bool = False,
    db: Session = Depends(get_db)
):
    service = NotificationService(db)
    result = service.get_notifications(user_id, unread_only)
    return result

@router.post("/notifications/mark-read")
def mark_as_read(
    data: NotificationMarkRead,
    user_id: int,
    db: Session = Depends(get_db)
):
    service = NotificationService(db)
    count = service.mark_as_read(data.notification_ids, user_id)
    return {"affected_count": count}
"""