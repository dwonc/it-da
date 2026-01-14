"""
데이터베이스 연결 설정 (SQLAlchemy)
MySQL 데이터베이스와 연결하고 세션 관리
"""

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from typing import Generator
import os
from dotenv import load_dotenv

# 환경 변수 로드
load_dotenv()


# ========================================
# 데이터베이스 연결 정보
# ========================================

# 환경 변수에서 가져오기 (보안을 위해)
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "1234")
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "3306")
DB_NAME = os.getenv("DB_NAME", "itda")

# MySQL 연결 URL 생성
# 형식: mysql+pymysql://사용자:비밀번호@호스트:포트/데이터베이스
DATABASE_URL = f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}?charset=utf8mb4"


# ========================================
# SQLAlchemy 엔진 생성
# ========================================

engine = create_engine(
    DATABASE_URL,
    # 연결 풀 설정 (성능 최적화)
    pool_size=10,              # 기본 연결 수
    max_overflow=20,           # 추가 가능한 최대 연결 수
    pool_timeout=30,           # 연결 대기 시간 (초)
    pool_recycle=3600,         # 연결 재사용 시간 (1시간)
    pool_pre_ping=True,        # 연결 상태 미리 확인
    echo=False,                # SQL 로그 출력 (개발 시 True로 변경)
)


# ========================================
# 세션 팩토리 생성
# ========================================

SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine
)


# ========================================
# Base 클래스 생성 (모든 모델의 부모)
# ========================================

Base = declarative_base()


# ========================================
# 데이터베이스 세션 의존성 (FastAPI용)
# ========================================

def get_db() -> Generator[Session, None, None]:
    """
    데이터베이스 세션 생성 및 관리

    FastAPI의 Depends에서 사용
    자동으로 연결 열고 닫기

    Yields:
        Session: 데이터베이스 세션

    Example:
        @router.get("/notifications")
        def get_notifications(db: Session = Depends(get_db)):
            # db 사용
            ...
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# ========================================
# 데이터베이스 초기화
# ========================================

def init_db():
    """
    데이터베이스 초기화
    - 모든 테이블 생성
    - 개발 시 사용 (프로덕션에서는 Alembic 사용 권장)
    """
    # 모든 모델 import 필요
    from app.models.notification_model import Notification

    # 테이블 생성
    Base.metadata.create_all(bind=engine)
    print("✅ 데이터베이스 테이블 생성 완료")


def drop_db():
    """
    모든 테이블 삭제 (주의!)
    개발/테스트용
    """
    Base.metadata.drop_all(bind=engine)
    print("⚠️  모든 테이블 삭제 완료")


# ========================================
# 연결 테스트
# ========================================

def test_connection():
    """
    데이터베이스 연결 테스트

    Returns:
        bool: 연결 성공 여부
    """
    try:
        db = SessionLocal()
        # 간단한 쿼리 실행
        db.execute("SELECT 1")
        db.close()
        print("✅ 데이터베이스 연결 성공")
        return True
    except Exception as e:
        print(f"❌ 데이터베이스 연결 실패: {e}")
        return False


# ========================================
# 실생활 비유로 이해하기 🏠
# ========================================

"""
🏦 은행 (Database)

1. DATABASE_URL (은행 주소)
   - "여기가 우리 은행입니다!"
   - mysql://localhost:3306/itda

2. engine (은행 건물)
   - 실제 데이터베이스 서버와 연결
   - 여러 창구(연결) 관리

3. SessionLocal (창구)
   - 고객이 업무 보는 곳
   - 한 명씩 차례로 사용

4. get_db() (창구 이용하기)
   - 창구 열기 → 업무 보기 → 창구 닫기
   - 자동으로 관리됨!

5. Base (설계도)
   - 모든 테이블(금고)의 기본 설계도
   - Notification 테이블도 이걸 상속받음
"""


# ========================================
# 환경 변수 설정 (.env 파일)
# ========================================

"""
프로젝트 루트에 .env 파일 생성:

# MySQL 설정
DB_USER=root
DB_PASSWORD=1234
DB_HOST=localhost
DB_PORT=3306
DB_NAME=itda

# 또는 Spring Boot application.properties와 동일하게!
spring.datasource.url=jdbc:mysql://localhost:3306/itda
spring.datasource.username=root
spring.datasource.password=1234
"""


# ========================================
# 사용 예시
# ========================================

"""
# 1. FastAPI 라우터에서 사용
from app.core.database import get_db

@router.get("/notifications")
def get_notifications(db: Session = Depends(get_db)):
    # db 자동으로 연결됨!
    notifications = db.query(Notification).all()
    return notifications


# 2. 직접 사용 (테스트용)
from app.core.database import SessionLocal

db = SessionLocal()
try:
    notifications = db.query(Notification).all()
    print(notifications)
finally:
    db.close()


# 3. 테이블 생성 (최초 1회)
from app.core.database import init_db

init_db()  # 모든 테이블 생성


# 4. 연결 테스트
from app.core.database import test_connection

test_connection()  # ✅ 연결 성공 / ❌ 연결 실패
"""


# ========================================
# 주의사항
# ========================================

"""
⚠️ 중요!

1. .env 파일은 .gitignore에 추가하세요!
   - 비밀번호가 GitHub에 올라가면 안 됩니다!

2. pymysql 설치 필요:
   pip install pymysql

3. python-dotenv 설치 필요:
   pip install python-dotenv

4. SQLAlchemy 설치 필요:
   pip install sqlalchemy

5. Spring Boot와 같은 DB 사용:
   - DB_NAME=itda (Spring Boot와 동일)
   - 같은 테이블 공유 가능!
"""