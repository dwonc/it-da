"""
LightGBM Ranker 학습 (Synthetic)
- group: user_id
- label: user별 상대선호(relevance)로 변환 (편향 감소)
- 누수 방지: meeting_avg_rating / count는 leave-one-out 형태로 구성(가능한 범위)
- negative sampling: 유저별로 일부만 샘플링하여 인기편향 완화
"""

import os
import math
import pickle
import numpy as np
import pandas as pd
import lightgbm as lgb
from sklearn.model_selection import GroupShuffleSplit
from sklearn.preprocessing import RobustScaler

# =========================
# Paths
# =========================
# 로컬 프로젝트 기준
INTERACTIONS_PATH = os.getenv("INTERACTIONS_PATH", "data/synthetic_interactions_nationwide.csv")
USERS_PATH        = os.getenv("USERS_PATH", "data/synthetic_users_nationwide.csv")
MEETINGS_PATH     = os.getenv("MEETINGS_PATH", "data/synthetic_meetings_nationwide.csv")

# 업로드 환경(예: 이 대화의 /mnt/data)에서 바로 돌릴 때는 아래로 바꿔도 됨:
# INTERACTIONS_PATH = "/mnt/data/synthetic_interactions_nationwide.csv"
# USERS_PATH        = "/mnt/data/synthetic_users_nationwide.csv"
# MEETINGS_PATH     = "/mnt/data/synthetic_meetings_nationwide.csv"

OUT_PATH = os.getenv("OUT_PATH", "models/lightgbm_ranker.pkl")


# =========================
# Utils
# =========================
def haversine_distance(lat1, lng1, lat2, lng2):
    R = 6371
    dlat = math.radians(lat2 - lat1)
    dlng = math.radians(lng2 - lng1)
    a = (math.sin(dlat / 2) ** 2 +
         math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) *
         math.sin(dlng / 2) ** 2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c

def calculate_interest_match(user_interests, meeting_category):
    if pd.isna(user_interests) or pd.isna(meeting_category):
        return 0.0
    user_keywords = set(str(user_interests).lower().split(", "))
    meeting_keywords = set(str(meeting_category).lower().split())
    if len(user_keywords) == 0:
        return 0.0
    return len(user_keywords & meeting_keywords) / len(user_keywords)

def calculate_cost_match(user_budget_type, meeting_cost):
    cost_ranges = {
        "low": (0, 10000),
        "value": (10000, 30000),
        "medium": (30000, 50000),
        "high": (50000, 100000),
        "premium": (100000, float('inf')),
        # 네 서비스 normalize에 맞춰 quality도 흡수
        "quality": (30000, 80000),
    }
    if user_budget_type not in cost_ranges:
        return 0.5
    mn, mx = cost_ranges[user_budget_type]
    if mn <= meeting_cost <= mx:
        return 1.0
    if meeting_cost < mn:
        return max(0.0, 1.0 - (mn - meeting_cost) / max(mn, 1))
    return max(0.0, 1.0 - (meeting_cost - mx) / max(mx, 1))

def make_relevance_per_user(ratings: pd.Series) -> pd.Series:
    """
    유저별 상대선호(relevance) 생성.
    - 평점이 절대적으로 높아도 유저가 후하게 주는 타입이면 과대평가될 수 있음 → 유저 내부 분위수/표준화가 안전.
    - 여기서는 분위수 기반 0~4 등급(relevance) 사용 (LambdaRank에 안정적)
    """
    # 값이 너무 적은 유저는 전부 1로 두어 과적합 줄임
    if len(ratings) < 5:
        return pd.Series(np.ones(len(ratings), dtype=int), index=ratings.index)

    # 분위수로 5등급(0~4)
    q = ratings.rank(method="average", pct=True)
    rel = pd.cut(q, bins=[0, .2, .4, .6, .8, 1.0], labels=[0,1,2,3,4]).astype(int)
    return rel

def leave_one_out_mean(df, key_col, target_col):
    """
    leave-one-out mean:
    group mean에서 자기 자신을 뺀 평균을 사용해서 누수/인기편향 완화
    """
    grp_sum = df.groupby(key_col)[target_col].transform("sum")
    grp_cnt = df.groupby(key_col)[target_col].transform("count")
    loo = (grp_sum - df[target_col]) / (grp_cnt - 1)
    # cnt==1이면 NaN → 글로벌 평균으로 대체
    return loo.fillna(df[target_col].mean())


# =========================
# Load
# =========================
print("데이터 로딩...")
interactions = pd.read_csv(INTERACTIONS_PATH)
users = pd.read_csv(USERS_PATH)
meetings = pd.read_csv(MEETINGS_PATH)

# rating NaN 제거
interactions = interactions.dropna(subset=["rating"]).copy()
interactions["rating"] = interactions["rating"].astype(float)

print(f"interactions={len(interactions):,} users={len(users):,} meetings={len(meetings):,}")


# =========================
# Feature engineering (편향 최소화 버전)
# =========================
print("유저/모임 통계 생성(누수 완화)...")

# 유저 통계 (여긴 상관 적음)
user_stats = interactions.groupby("user_id")["rating"].agg(["mean","std","count"]).reset_index()
user_stats.columns = ["user_id", "user_avg_rating", "user_rating_std", "user_meeting_count"]
user_stats["user_rating_std"] = user_stats["user_rating_std"].fillna(0.3)

# 모임 통계는 "leave-one-out mean"을 interactions row 단위에서 만들기 위해 merge 후 생성
base = interactions.merge(users, on="user_id", how="left").merge(meetings, on="meeting_id", how="left")
base = base.merge(user_stats, on="user_id", how="left")

# leave-one-out 기반 meeting 평균평점(누수/인기편향 감소)
base["meeting_avg_rating_loo"] = leave_one_out_mean(base, "meeting_id", "rating")

# meeting count / participant count도 너무 강하면 인기편향이 심해짐 → log1p + clip
meet_cnt = base.groupby("meeting_id")["rating"].transform("count")
base["meeting_rating_count"] = np.log1p(meet_cnt).clip(0, 5)  # 상한 클립

# participant_count가 있으면 사용(없으면 meeting_rating_count로 대체)
if "participant_count" in base.columns:
    base["meeting_participant_count"] = np.log1p(base["participant_count"].fillna(0)).clip(0, 6)
else:
    base["meeting_participant_count"] = base["meeting_rating_count"]

# =========================
# 라벨: 유저별 상대선호(relevance) 생성
# =========================
print("라벨(relevance) 생성...")
base["relevance"] = base.groupby("user_id")["rating"].transform(make_relevance_per_user).astype(int)

# =========================
# Negative sampling (선택/추천)
# =========================
# 합성데이터가 user-meeting이 너무 밀집되어 있으면 인기편향이 더 커짐.
# 유저별로 interactions를 (긍정/부정 비율 유지하며) 일부만 뽑아서 학습 안정화.
print("샘플링(편향 완화)...")
rng = np.random.default_rng(42)

def sample_per_user(df_u: pd.DataFrame, max_rows=200):
    if len(df_u) <= max_rows:
        return df_u
    # relevance 낮은 쪽(부정/중립)도 남기되, 높은 쪽만 과하게 남지 않도록 stratified 느낌
    # 각 relevance별로 비슷한 비율로 뽑음
    out = []
    per_bucket = max_rows // 5
    for rel in range(5):
        bucket = df_u[df_u["relevance"] == rel]
        if len(bucket) == 0:
            continue
        take = min(len(bucket), per_bucket)
        idx = rng.choice(bucket.index.to_numpy(), size=take, replace=False)
        out.append(df_u.loc[idx])
    sampled = pd.concat(out) if out else df_u.sample(n=max_rows, random_state=42)
    return sampled

base = base.groupby("user_id", group_keys=False).apply(sample_per_user, max_rows=250).reset_index(drop=True)
print(f"샘플링 후 rows={len(base):,}")

# =========================
# One-hot: category/vibe (너 코드 유지)
# =========================
CATEGORIES = ["스포츠", "맛집", "카페", "문화예술", "스터디", "취미활동", "소셜"]
VIBES = ["활기찬", "여유로운", "진지한", "즐거운", "감성적인", "에너지 넘치는", "힐링", "창의적인"]

def safe_get(row, key, default=None):
    v = row.get(key)
    return default if pd.isna(v) else v

print("특징 행렬 생성...")

feature_names = [
    "distance_km",
    "time_match",
    "location_type_match",
    "interest_match_score",
    "cost_match_score",
    "user_avg_rating",
    "user_meeting_count",
    "user_rating_std",
    "meeting_avg_rating_loo",
    "meeting_rating_count_log",
    "meeting_participant_count_log",
    "max_participants",
] + [f"cat_{c}" for c in CATEGORIES] + [f"vibe_{v}" for v in VIBES]

rows = []
groups = []
labels = []

# group sizes 만들기 위해 user별 순회
for user_id, df_u in base.groupby("user_id"):
    # 유저당 최소 5개는 있어야 랭킹 의미가 있음
    if len(df_u) < 5:
        continue

    for _, row in df_u.iterrows():
        dist = haversine_distance(
            safe_get(row, "user_lat", 37.5), safe_get(row, "user_lng", 127.0),
            safe_get(row, "meeting_lat", 37.5), safe_get(row, "meeting_lng", 127.0),
        )

        time_match = 1.0 if safe_get(row, "time_preference", "") == safe_get(row, "time_slot", "") else 0.0
        loc_match = 1.0 if safe_get(row, "location_preference", "") == safe_get(row, "location_type", "") else 0.0

        interest = calculate_interest_match(safe_get(row, "interests", ""), safe_get(row, "category", ""))
        cost = calculate_cost_match(str(safe_get(row, "budget_type", "value")).lower(), int(safe_get(row, "expected_cost", 20000)))

        feat = [
            float(dist),
            float(time_match),
            float(loc_match),
            float(interest),
            float(cost),
            float(safe_get(row, "user_avg_rating", 3.0)),
            float(safe_get(row, "user_meeting_count", 0)),
            float(safe_get(row, "user_rating_std", 0.3)),
            float(safe_get(row, "meeting_avg_rating_loo", 3.0)),
            float(safe_get(row, "meeting_rating_count", 0.0)),       # 이미 log+clip
            float(safe_get(row, "meeting_participant_count", 0.0)),  # 이미 log+clip
            float(safe_get(row, "max_participants", 10)),
        ]

        cat = str(safe_get(row, "category", ""))
        feat.extend([1.0 if cat == c else 0.0 for c in CATEGORIES])

        vibe = str(safe_get(row, "vibe", ""))
        feat.extend([1.0 if vibe == v else 0.0 for v in VIBES])

        rows.append(feat)
        labels.append(int(safe_get(row, "relevance", 1)))

    groups.append(len(df_u))

X = np.array(rows, dtype=float)
y = np.array(labels, dtype=int)
group = np.array(groups, dtype=int)

print(f"X={X.shape} y={y.shape} groups={len(group)} (sum={group.sum()})")
assert group.sum() == len(y)

# =========================
# Scaling (편향 완화 + 안정화)
# - 거리/카운트류 스케일이 너무 크면 그쪽으로만 학습됨 → RobustScaler 추천
# =========================
scaler = RobustScaler()
X_scaled = scaler.fit_transform(X)

# =========================
# Train/Valid split (group 단위로!)
# =========================
gss = GroupShuffleSplit(n_splits=1, test_size=0.2, random_state=42)

# group 단위를 인덱스로 만들기 위해, 각 row가 어떤 group(유저)에 속하는지 row_group_id를 생성
row_group_ids = np.concatenate([np.full(sz, i) for i, sz in enumerate(group)])

train_idx, valid_idx = next(gss.split(X_scaled, y, groups=row_group_ids))

X_train, y_train = X_scaled[train_idx], y[train_idx]
X_valid, y_valid = X_scaled[valid_idx], y[valid_idx]

# train/valid group도 재구성
def build_group_from_indices(indices):
    # indices를 row_group_ids로 다시 묶어서 group size 리스트로 만들기
    df = pd.DataFrame({"gid": row_group_ids[indices]})
    return df.groupby("gid").size().to_numpy()

group_train = build_group_from_indices(train_idx)
group_valid = build_group_from_indices(valid_idx)

print(f"train rows={len(train_idx):,} groups={len(group_train)} / valid rows={len(valid_idx):,} groups={len(group_valid)}")


# =========================
# Train Ranker (LambdaRank)
# =========================
ranker = lgb.LGBMRanker(
    objective="lambdarank",
    metric="ndcg",
    eval_at=[5, 10, 20],
    n_estimators=3000,
    learning_rate=0.03,

    # 과적합/편향 완화 세팅
    max_depth=6,
    num_leaves=63,              # depth6이면 64 근처, 너무 크지 않게
    min_child_samples=80,       # 중요: 작게 하면 과적합 심해짐
    min_child_weight=1e-3,

    subsample=0.8,
    subsample_freq=1,
    colsample_bytree=0.8,

    reg_alpha=0.3,
    reg_lambda=3.0,

    # “한쪽 feature로만 몰림” 방지에 도움 되는 옵션들
    # (데이터에 따라 효과 다름)
    max_bin=255,
    random_state=42,
)

ranker.fit(
    X_train, y_train,
    group=group_train,
    eval_set=[(X_valid, y_valid)],
    eval_group=[group_valid],
    eval_at=[5, 10, 20],
    callbacks=[lgb.early_stopping(stopping_rounds=80, verbose=True)]
)

print("best_iteration:", ranker.best_iteration_)

# =========================
# Save (모델 + 스케일러 + feature_names)
# =========================
os.makedirs(os.path.dirname(OUT_PATH), exist_ok=True)

payload = {
    "ranker": ranker,
    "scaler": scaler,
    "feature_names": feature_names,
    "meta": {
        "objective": "lambdarank",
        "label": "user_relative_relevance_0to4",
        "note": "robust_scaler + loo_meeting_stats + per_user_quantile_labels + per_user_sampling"
    }
}

with open(OUT_PATH, "wb") as f:
    pickle.dump(payload, f)

print(f"✅ Ranker 저장 완료: {OUT_PATH}")
