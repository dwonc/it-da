from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    # =========================
    # Server
    # =========================
    HOST: str = "0.0.0.0"
    PORT: int = 8000

    # =========================
    # CORS
    # =========================
    ALLOWED_ORIGINS: str = "http://localhost:3000,http://localhost:8080"

    # =========================
    # Kakao API
    # =========================
    KAKAO_REST_API_KEY: str = "your_key_here"
    KAKAO_LOCAL_API_URL: str = "https://dapi.kakao.com/v2/local"

    # =========================
    # Model Paths (🔥 분리)
    # =========================
    SVD_MODEL_PATH: str = "./models/svd_model.pkl"

    LIGHTGBM_RANKER_PATH: str = "./models/lightgbm_ranker.pkl"
    LIGHTGBM_REGRESSOR_PATH: str = "./models/lightgbm_regressor.pkl"

    KCELECTRA_MODEL_NAME: str = "beomi/KcELECTRA-base"

    # =========================
    # Recommendation Policy
    # =========================
    DEFAULT_SEARCH_RADIUS: int = 3000
    MAX_SEARCH_RADIUS: int = 5000

    # satisfaction / match score 기준
    MIN_RECOMMENDED_RATING: float = 3.5   # ⭐ satisfaction 판단 기준
    MIN_MATCH_SCORE: int = 70              # ⭐ ranker 기준

    TOP_N_RECOMMENDATIONS: int = 3

    # =========================
    # Database (선택)
    # =========================
    DATABASE_URL: str = "postgresql://user:pass@localhost/db"

    class Config:
        env_file = ".env"
        case_sensitive = True

    @property
    def get_allowed_origins(self) -> List[str]:
        return [origin.strip() for origin in self.ALLOWED_ORIGINS.split(",")]


settings = Settings()
