// src/hooks/badge/useBadges.ts
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getUserBadges, updateAllBadges, updateBadgeProgress } from "@/api/badge.api";
import type { UserBadgeDto } from "@/types/badge";

export function useBadges() {
    return useQuery<UserBadgeDto[]>({
        queryKey: ["badges"],
        queryFn: async () => {
            // 먼저 update-all 호출해서 user_badges 테이블에 데이터 생성
            try {
                await updateAllBadges();
                console.log("✅ 배지 진행도 업데이트 완료");
            } catch (err) {
                console.warn("⚠️ 배지 업데이트 실패 (무시하고 진행):", err);
            }

            // 배지 데이터 조회
            const raw = await getUserBadges();
            console.log("📦 조회된 배지:", raw?.length ?? 0, "개");
            return Array.isArray(raw) ? raw : [];
        },
        staleTime: 1000 * 60 * 5, // 5분 캐시
    });
}

export function useUpdateAllBadges() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: updateAllBadges,
        onSuccess: async () => {
            await qc.invalidateQueries({ queryKey: ["badges"] });
        },
    });
}

export function useUpdateOneBadge() {
    const qc = useQueryClient();
    return useMutation({
        mutationFn: (badgeCode: string) => updateBadgeProgress(badgeCode),
        onSuccess: async () => {
            await qc.invalidateQueries({ queryKey: ["badges"] });
        },
    });
}