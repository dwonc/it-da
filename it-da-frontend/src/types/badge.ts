// src/types/badge.ts

export type BadgeGrade = "COMMON" | "RARE" | "EPIC" | "LEGENDARY";

export type BadgeCategory =
    | "PARTICIPATION"
    | "AI"
    | "DISTANCE"
    | "TIME"
    | "PERSONALITY"
    | "CATEGORY"
    | "REVIEW"
    | "SOCIAL"
    | "HOST"
    | "SPECIAL";

export interface UserBadgeDto {
    badgeId: number;
    badgeCode: string;
    badgeName: string;
    description: string | null;
    grade: BadgeGrade;
    category: BadgeCategory;
    icon: string | null;

    unlocked: boolean;

    // 서버에서 null이 올 수도 있으니 여기서 널 허용 (방어는 normalizeBadge가 함)
    progress: number | null;
    targetValue: number | null;
    progressPercentage: number | null;

    unlockedAt: string | null;
}
