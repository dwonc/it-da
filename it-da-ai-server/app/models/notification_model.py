"""
알림 모델 (SQLAlchemy ORM)
MySQL 데이터베이스 테이블 정의
"""

from sqlalchemy import Column, Integer, String, Text, Boolean, DateTime, Enum as SQLEnum, Index
from sqlalchemy.sql import func
from datetime import datetime
import enum

# ✨ Base import 추가!
from app.core.database import Base


# ========================================
# Enum: 알림 타입 (DB용)
# ========================================

class NotificationTypeEnum(str, enum.Enum):
    """알림 타입 (데이터베이스용)"""
    MEETING = "MEETING"      # 모임 관련
    CHAT = "CHAT"            # 채팅 메시지
    REVIEW = "REVIEW"        # 후기 관련
    BADGE = "BADGE"          # 배지 획득
    SYSTEM = "SYSTEM"        # 시스템 공지
    FOLLOW = "FOLLOW"        # 팔로우


# ========================================
# 모델: Notification 테이블
# ========================================

class Notification(Base):  # ✨ Base 상속!
    """
    알림 테이블

    사용자에게 발송되는 모든 알림을 저장
    - 배지 획득 알림
    - 모임 관련 알림
    - 채팅 메시지 알림
    - 시스템 공지 등
    """

    # ========== 기본 설정 ==========
    __tablename__ = "notifications"


    # ========== 컬럼 정의 ==========

    # 기본키
    notification_id = Column(
        Integer,
        primary_key=True,
        autoincrement=True,
        comment="알림 ID (기본키)"
    )

    # 사용자 정보
    user_id = Column(
        Integer,
        nullable=False,
        index=True,
        comment="사용자 ID (외래키 - users 테이블)"
    )

    # 알림 타입
    notification_type = Column(
        SQLEnum(NotificationTypeEnum),
        nullable=False,
        index=True,
        comment="알림 타입 (MEETING/CHAT/REVIEW/BADGE/SYSTEM/FOLLOW)"
    )

    # 알림 내용
    title = Column(
        String(200),
        nullable=False,
        comment="알림 제목"
    )

    content = Column(
        Text,
        nullable=False,
        comment="알림 내용"
    )

    # 연결 정보
    link_url = Column(
        String(500),
        nullable=True,
        comment="클릭 시 이동할 URL"
    )

    related_id = Column(
        Integer,
        nullable=True,
        comment="관련 ID (모임ID, 배지ID 등)"
    )

    # 읽음 상태
    is_read = Column(
        Boolean,
        nullable=False,
        default=False,
        index=True,
        comment="읽음 여부 (False=읽지않음, True=읽음)"
    )

    # 시간 정보
    sent_at = Column(
        DateTime,
        nullable=False,
        default=datetime.utcnow,
        server_default=func.now(),
        comment="발송 시각"
    )

    read_at = Column(
        DateTime,
        nullable=True,
        comment="읽은 시각"
    )


    # ========== 인덱스 설정 (성능 최적화) ==========
    __table_args__ = (
        # 복합 인덱스: 사용자별 읽지 않은 알림 빠르게 조회
        Index('idx_user_unread', 'user_id', 'is_read'),

        # 복합 인덱스: 사용자별 최신 알림 조회
        Index('idx_user_sent', 'user_id', 'sent_at'),

        # 복합 인덱스: 알림 타입별 조회
        Index('idx_type_sent', 'notification_type', 'sent_at'),

        {'comment': '알림 테이블 - 사용자에게 발송되는 모든 알림 저장'}
    )


    # ========== 메서드 ==========

    def __repr__(self):
        """객체 출력용"""
        return (
            f"<Notification("
            f"id={self.notification_id}, "
            f"user_id={self.user_id}, "
            f"type={self.notification_type}, "
            f"is_read={self.is_read}"
            f")>"
        )

    def to_dict(self):
        """딕셔너리로 변환 (API 응답용)"""
        return {
            "notification_id": self.notification_id,
            "user_id": self.user_id,
            "notification_type": self.notification_type.value if self.notification_type else None,
            "title": self.title,
            "content": self.content,
            "link_url": self.link_url,
            "related_id": self.related_id,
            "is_read": self.is_read,
            "sent_at": self.sent_at.isoformat() if self.sent_at else None,
            "read_at": self.read_at.isoformat() if self.read_at else None
        }

    def mark_as_read(self):
        """읽음 처리"""
        if not self.is_read:
            self.is_read = True
            self.read_at = datetime.utcnow()


# ========================================
# 실생활 비유로 이해하기 🏠
# ========================================

"""
📬 우편함(Notification 테이블)에 편지(알림) 저장

1. notification_id (편지 번호)
   - 각 편지마다 고유 번호
   - 예: 편지 #1, 편지 #2

2. user_id (받는 사람)
   - 이 편지를 받을 사람
   - 예: 훈님(user_id=1)

3. notification_type (편지 종류)
   - BADGE: "축하합니다! 배지 획득"
   - MEETING: "모임 시작 1시간 전입니다"
   - CHAT: "새 메시지가 도착했습니다"

4. title (편지 제목)
   - "🏆 열정러 배지 획득!"

5. content (편지 내용)
   - "10회 모임 참여를 달성했어요!"

6. link_url (관련 페이지)
   - "/badges/participate_10"
   - 클릭하면 해당 페이지로 이동

7. is_read (읽었는지 여부)
   - False: 아직 안 읽음 (봉투 안 뜯음)
   - True: 읽음 (봉투 뜯어봄)

8. sent_at (편지 도착 시간)
   - 2026-01-13 10:30:00

9. read_at (편지 읽은 시간)
   - 2026-01-13 11:00:00
   - 안 읽었으면 None
"""


# ========================================
# 실제 사용 예시
# ========================================

"""
# 1. 알림 생성
notification = Notification(
    user_id=1,
    notification_type=NotificationTypeEnum.BADGE,
    title="🏆 열정러 배지 획득!",
    content="10회 모임 참여를 달성했어요!",
    link_url="/badges/participate_10",
    related_id=10
)

# 2. 데이터베이스 저장
session.add(notification)
session.commit()

# 3. 읽음 처리
notification.mark_as_read()
session.commit()

# 4. 딕셔너리 변환 (API 응답용)
result = notification.to_dict()
"""