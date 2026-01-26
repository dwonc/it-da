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

    progress: number | null;
    targetValue: number | null;
    progressPercentage: number | null;

    unlockedAt: string | null;
}

// ✅ 서버 응답 타입 (mapServerBadge.ts 호환용)
export interface ServerUserBadgeResponse {
    badgeId: number;
    badgeCode: string;
    badgeName: string;
    description: string | null;
    grade: string;      // 서버에서는 string으로 옴
    category: string;   // 서버에서는 string으로 옴
    icon: string | null;
    unlocked: boolean;
    progress: number | null;
    targetValue: number | null;
    progressPercentage: number | null;
    unlockedAt: string | null;
}

// ✅ Badge 타입 alias
export type Badge = UserBadgeDto;