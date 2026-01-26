// src/utils/badge/mapServerBadge.ts
import type { UserBadgeDto, BadgeGrade, BadgeCategory, ServerUserBadgeResponse } from "@/types/badge";

/**
 * 서버 응답을 프론트엔드 타입으로 변환
 */
export function mapServerBadge(server: ServerUserBadgeResponse): UserBadgeDto {
    return {
        badgeId: server.badgeId,
        badgeCode: server.badgeCode,
        badgeName: server.badgeName,
        description: server.description,
        grade: server.grade as BadgeGrade,           // ✅ 타입 캐스팅
        category: server.category as BadgeCategory,  // ✅ 타입 캐스팅
        icon: server.icon,
        unlocked: server.unlocked,
        progress: server.progress,
        targetValue: server.targetValue,
        progressPercentage: server.progressPercentage,
        unlockedAt: server.unlockedAt,
    };
}

/**
 * 서버 응답 배열을 프론트엔드 타입 배열로 변환
 */
export function mapServerBadges(servers: ServerUserBadgeResponse[]): UserBadgeDto[] {
    return servers.map(mapServerBadge);
}