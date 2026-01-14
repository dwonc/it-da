"""
데이터베이스 초기화 스크립트
Notification 테이블을 MySQL에 자동 생성
"""

import sys
import os

# 프로젝트 루트를 Python 경로에 추가
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.core.database import engine, Base, test_connection
from app.models.notification_model import Notification  # ✨ 모델 import 필수!


def init_database():
    """
    데이터베이스 초기화
    1. 연결 테스트
    2. 테이블 생성
    """

    print("=" * 60)
    print("🚀 ITDA 데이터베이스 초기화 시작")
    print("=" * 60)

    # 1단계: 연결 테스트
    print("\n[1단계] 데이터베이스 연결 테스트...")
    if not test_connection():
        print("❌ 데이터베이스 연결 실패!")
        print("💡 확인 사항:")
        print("   1. MySQL 서버가 실행 중인가요?")
        print("   2. .env 파일의 DB 정보가 맞나요?")
        print("   3. itda 데이터베이스가 생성되어 있나요?")
        return False

    # 2단계: 테이블 생성
    print("\n[2단계] 테이블 생성 중...")
    try:
        # 모든 테이블 생성
        Base.metadata.create_all(bind=engine)

        print("\n✅ 테이블 생성 완료!")
        print("\n생성된 테이블:")
        print("  📋 notifications - 알림 테이블")

        # 테이블 구조 확인
        print("\n테이블 구조:")
        print("  - notification_id (INT, PRIMARY KEY, AUTO_INCREMENT)")
        print("  - user_id (INT, INDEX)")
        print("  - notification_type (ENUM)")
        print("  - title (VARCHAR(200))")
        print("  - content (TEXT)")
        print("  - link_url (VARCHAR(500))")
        print("  - related_id (INT)")
        print("  - is_read (BOOLEAN, INDEX)")
        print("  - sent_at (DATETIME)")
        print("  - read_at (DATETIME)")

        print("\n인덱스:")
        print("  - idx_user_unread (user_id, is_read)")
        print("  - idx_user_sent (user_id, sent_at)")
        print("  - idx_type_sent (notification_type, sent_at)")

        return True

    except Exception as e:
        print(f"\n❌ 테이블 생성 실패: {e}")
        return False


def drop_tables():
    """
    모든 테이블 삭제 (주의!)
    """
    print("=" * 60)
    print("⚠️  경고: 모든 테이블을 삭제합니다!")
    print("=" * 60)

    confirm = input("\n정말로 삭제하시겠습니까? (yes/no): ")

    if confirm.lower() == "yes":
        try:
            Base.metadata.drop_all(bind=engine)
            print("\n✅ 모든 테이블 삭제 완료")
            return True
        except Exception as e:
            print(f"\n❌ 테이블 삭제 실패: {e}")
            return False
    else:
        print("\n취소되었습니다.")
        return False


def show_menu():
    """메뉴 표시"""
    print("\n" + "=" * 60)
    print("ITDA 데이터베이스 관리")
    print("=" * 60)
    print("1. 테이블 생성 (초기화)")
    print("2. 테이블 삭제 (주의!)")
    print("3. 연결 테스트")
    print("0. 종료")
    print("=" * 60)


if __name__ == "__main__":
    """
    실행 방법:
    
    터미널에서:
    cd it-da-ai-server
    python scripts/init_database.py
    """

    while True:
        show_menu()
        choice = input("\n선택 (0-3): ").strip()

        if choice == "1":
            # 테이블 생성
            init_database()

        elif choice == "2":
            # 테이블 삭제
            drop_tables()

        elif choice == "3":
            # 연결 테스트
            print("\n연결 테스트 중...")
            test_connection()

        elif choice == "0":
            # 종료
            print("\n👋 프로그램을 종료합니다.")
            break

        else:
            print("\n❌ 잘못된 입력입니다. 0-3 중에서 선택하세요.")


# ========================================
# 실생활 비유로 이해하기 🏠
# ========================================

"""
🏗️ 건물 짓기 (테이블 생성)

1. init_database()
   - 땅에 건물 기초 공사하기
   - notifications 테이블 = 우편함 건물

2. Base.metadata.create_all()
   - 설계도(Model)대로 실제 건물 짓기
   - Column들 = 건물 구조물

3. drop_tables()
   - 건물 철거하기 (주의!)
   - 데이터 전부 사라짐!

4. test_connection()
   - 건설 현장에 갈 수 있는지 확인
   - MySQL 서버 = 건설 현장
"""


# ========================================
# 문제 해결 가이드
# ========================================

"""
문제 1: "데이터베이스 연결 실패"
해결:
  1. MySQL 서버 실행 확인
     - Windows: 작업 관리자 > 서비스 > MySQL
     - Mac: brew services list
  
  2. .env 파일 확인
     - DB_USER=root
     - DB_PASSWORD=1234
     - DB_HOST=localhost
     - DB_PORT=3306
     - DB_NAME=itda
  
  3. itda 데이터베이스 생성 확인
     mysql -u root -p
     CREATE DATABASE IF NOT EXISTS itda;


문제 2: "테이블이 이미 존재합니다"
해결:
  - 정상입니다! 이미 테이블이 있다는 뜻
  - 다시 만들고 싶으면 옵션 2로 삭제 후 재생성


문제 3: "모듈을 찾을 수 없습니다"
해결:
  - 프로젝트 루트에서 실행하세요
  cd it-da-ai-server
  python scripts/init_database.py


문제 4: "pymysql을 찾을 수 없습니다"
해결:
  pip install pymysql sqlalchemy python-dotenv
"""