import pickle
import numpy as np
from pathlib import Path
from typing import Optional, Any, List, Dict


class LightGBMRegressorModel:
    """LightGBM Regressor 모델 (payload_dict도 대응 가능)"""

    def __init__(self, model_path: str = "models/lightgbm_regressor.pkl"):
        self.model_path = Path(model_path)
        self.model: Optional[Any] = None
        self.scaler = None
        self.feature_names: List[str] = []
        self.model_type: str = "unknown"

    def load(self):
        if not self.model_path.exists():
            raise FileNotFoundError(f"Model not found: {self.model_path}")

        with open(self.model_path, "rb") as f:
            obj = pickle.load(f)

        if isinstance(obj, dict) and ("model" in obj or "regressor" in obj):
            self.model = obj.get("regressor") or obj.get("model")
            self.scaler = obj.get("scaler")
            self.feature_names = obj.get("feature_names", [])
            self.model_type = "payload_dict"
        elif hasattr(obj, "predict"):
            self.model = obj
            self.scaler = None
            self.feature_names = []
            self.model_type = "direct_model"
        else:
            raise ValueError(f"지원하지 않는 모델 포맷: {type(obj)}")

        print(f"✅ LightGBM Regressor 로드 완료: {self.model_path} (타입: {self.model_type})")

        # ✅ 여기서 booster 찍기 (정답)
        try:
            booster = getattr(self.model, "booster_", None)
            if booster:
                print(
                    "[DBG] regressor booster params:",
                    "max_depth =", booster.params.get("max_depth"),
                    "num_leaves =", booster.params.get("num_leaves"),
                    "learning_rate =", booster.params.get("learning_rate"),
                )
            else:
                print("[DBG] no booster_ found")
        except Exception as e:
            print("[DBG] booster inspect failed:", e)

    def is_loaded(self) -> bool:
        return self.model is not None

    def predict(self, X: np.ndarray) -> np.ndarray:
        if self.model is None:
            raise ValueError("Model not loaded. Call load() first.")

        if X.ndim == 1:
            X = X.reshape(1, -1)

        if self.scaler is not None:
            X = self.scaler.transform(X)

        return self.model.predict(X)
