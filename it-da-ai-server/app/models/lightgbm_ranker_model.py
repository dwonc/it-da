import json
import os
import pickle
from pathlib import Path
from typing import Optional, Any

import numpy as np


class LightGBMRankerModel:
    def __init__(self, model_path: str = "models/lightgbm_ranker.pkl", calib_path: Optional[str] = None):
        self.model_path = Path(model_path)
        self.calib_path = Path(calib_path) if calib_path else None  # ✅ 이게 빠져있었음

        self.model: Optional[Any] = None
        self.calibration: Optional[dict] = None
        self.scaler = None
        self.feature_names = []
        self.model_type: Optional[str] = None

    def load(self):
        # 1) 모델 파일 존재 확인
        if not self.model_path.exists():
            raise FileNotFoundError(f"Model not found: {self.model_path}")

        # 2) 모델 로드 (너가 저장한 번들(dict) 기준)
        with open(self.model_path, "rb") as f:
            loaded = pickle.load(f)

        # ✅ 너가 저장한 형식: {"ranker": ..., "scaler": ..., "feature_names": ...}
        if isinstance(loaded, dict) and "ranker" in loaded:
            self.model = loaded["ranker"]
            self.scaler = loaded.get("scaler")
            self.feature_names = loaded.get("feature_names", [])
            self.model_type = "dict_ranker_bundle"
        else:
            # 혹시 모델만 저장했을 때
            self.model = loaded
            self.model_type = "direct_model"

        # 3) calibration 로드 (있으면)
        if self.calib_path and self.calib_path.exists():
            with open(self.calib_path, "r", encoding="utf-8") as f:
                self.calibration = json.load(f)

        print(
            f"✅ LightGBM Ranker 로드 완료: {self.model_path} "
            f"(type={self.model_type}, calib={'yes' if self.calibration else 'no'})"
        )

    def predict(self, X: np.ndarray) -> np.ndarray:
        if self.model is None:
            raise ValueError("Model not loaded. Call load() first.")
        if self.scaler is not None:
            X = self.scaler.transform(X)
        return self.model.predict(X)

    def predict_single(self, features: np.ndarray) -> float:
        if features.ndim == 1:
            features = features.reshape(1, -1)
        return float(self.predict(features)[0])

    def is_loaded(self) -> bool:
        return self.model is not None
