// src/hooks/badge/useMyBadges.ts
import { useCallback, useEffect, useState } from "react";
import { getUnlockedBadges } from "@/api/badge.api";
import type { UserBadgeDto } from "@/types/badge";

type UseMyBadgesResult = {
    data: UserBadgeDto[];
    isLoading: boolean;
    isError: boolean;
    refetch: () => Promise<void>;
};

export function useMyBadges(): UseMyBadgesResult {
    const [data, setData] = useState<UserBadgeDto[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [isError, setIsError] = useState<boolean>(false);

    const load = useCallback(async (): Promise<void> => {
        try {
            setIsLoading(true);
            setIsError(false);
            const badges = await getUnlockedBadges();
            setData(Array.isArray(badges) ? badges : []);
        } catch {
            setIsError(true);
            setData([]);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        void load();
    }, [load]);

    return { data, isLoading, isError, refetch: load };
}