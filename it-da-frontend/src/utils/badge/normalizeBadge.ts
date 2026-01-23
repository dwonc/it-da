// src/utils/badge/normalizeBadge.ts
import type { UserBadgeDto } from "@/types/badge";

/**
 * 서버 응답(UserBadgeDto)을 UI에서 안전하게 쓰도록 보정합니다.
 * - progress/targetValue/progressPercentage null 방어
 * - progressPercentage 서버 미제공(null) 시 계산
 */
export function normalizeBadge(badge: UserBadgeDto): UserBadgeDto {
    const safeProgress =
        typeof badge.progress === "number" && Number.isFinite(badge.progress)
            ? badge.progress
            : 0;

    const safeTarget =
        typeof badge.targetValue === "number" && Number.isFinite(badge.targetValue) && badge.targetValue > 0
            ? badge.targetValue
            : 0;

    const calculatedPercentage =
        safeTarget === 0 ? 0 : Math.min(100, Math.max(0, Math.round((safeProgress / safeTarget) * 100)));

    const safePercentage =
        typeof badge.progressPercentage === "number" && Number.isFinite(badge.progressPercentage)
            ? badge.progressPercentage
            : calculatedPercentage;

    return {
        ...badge,
        description: badge.description ?? null,
        icon: badge.icon ?? null,
        unlockedAt: badge.unlockedAt ?? null,
        progress: safeProgress,
        targetValue: safeTarget,          // UI에서는 0으로 통일해서 안전하게 사용
        progressPercentage: safePercentage,
    };
}
