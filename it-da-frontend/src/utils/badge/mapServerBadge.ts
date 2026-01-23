// src/utils/badge/mapServerBadge.ts
import type { ServerUserBadgeResponse, UserBadgeDto } from "@/types/badge";

/** 서버(중첩) -> UI(평탄화) */
export function mapServerBadgeToDto(item: ServerUserBadgeResponse): UserBadgeDto {
    const b = item.badge;

    return {
        badgeId: b.badgeId,
        badgeCode: b.badgeCode,
        badgeName: b.badgeName,
        description: b.description ?? null,
        grade: String(b.grade ?? "UNKNOWN"),
        category: String(b.category ?? "UNKNOWN"),
        icon: b.icon ?? null,

        unlocked: Boolean(item.unlocked),
        progress: typeof item.progress === "number" ? item.progress : 0,
        targetValue: typeof item.targetValue === "number" ? item.targetValue : 0,
        progressPercentage:
            typeof item.progressPercentage === "number" ? item.progressPercentage : null,
        unlockedAt: item.unlockedAt ?? null,
    };
}

export function mapServerBadgesToDtos(data: unknown): UserBadgeDto[] {
    if (!Array.isArray(data)) return [];
    return data.map((x) => mapServerBadgeToDto(x as ServerUserBadgeResponse));
}
