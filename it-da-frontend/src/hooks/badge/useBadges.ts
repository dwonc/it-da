// src/hooks/badge/useBadges.ts
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { getUserBadges, updateAllBadges, updateBadgeProgress } from "@/api/badge.api";
import type { UserBadgeDto } from "@/types/badge";
import { normalizeBadge } from "@/utils/badge/normalizeBadge";

export function useBadges() {
    return useQuery<UserBadgeDto[]>({
        queryKey: ["badges"],
        queryFn: async () => {
            const raw = await getUserBadges();
            return Array.isArray(raw) ? raw.map(normalizeBadge) : [];
        },
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
