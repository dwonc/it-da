// src/components/badge/badge.utils.ts
import type { BadgeCategory, BadgeGrade, UserBadgeDto } from "@/api/badge.api";

export const categoryLabel: Record<BadgeCategory, string> = {
    PARTICIPATION: "참여",
    AI: "AI 추천",
    DISTANCE: "거리",
    TIME: "시간대",
    PERSONALITY: "성향",
    CATEGORY: "카테고리",
    REVIEW: "리뷰/평점",
    SOCIAL: "소셜",
    HOST: "주최",
    SPECIAL: "특별",
};

export const gradeLabel: Record<BadgeGrade, string> = {
    COMMON: "일반",
    RARE: "레어",
    EPIC: "에픽",
    LEGENDARY: "전설",
};

export function safeIcon(icon?: string | null): string {
    return icon && icon.trim().length > 0 ? icon : "🏷️";
}

export type SortKey = "EARNED_FIRST" | "PROGRESS_DESC" | "GRADE_DESC" | "NAME_ASC";

const gradeOrder: Record<BadgeGrade, number> = {
    COMMON: 0,
    RARE: 1,
    EPIC: 2,
    LEGENDARY: 3,
};

export function sortBadges(list: UserBadgeDto[], sortKey: SortKey): UserBadgeDto[] {
    const copied = [...list];
    switch (sortKey) {
        case "EARNED_FIRST":
            return copied.sort((a, b) => Number(b.unlocked) - Number(a.unlocked));
        case "PROGRESS_DESC":
            return copied.sort((a, b) => (b.progressPercentage ?? 0) - (a.progressPercentage ?? 0));
        case "GRADE_DESC":
            return copied.sort((a, b) => gradeOrder[b.grade] - gradeOrder[a.grade]);
        case "NAME_ASC":
            return copied.sort((a, b) => a.badgeName.localeCompare(b.badgeName));
        default:
            return copied;
    }
}
