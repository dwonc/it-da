import { useCallback, useEffect, useState } from "react";
import { getUnlockedBadges, type Badge } from "@/api/badge.api";

// ✅ 임시: 로그인 붙이기 전까지 userId 하드코딩
const TEMP_USER_ID = 1;

type UseMyBadgesResult = {
    data: Badge[];
    isLoading: boolean;
    isError: boolean;
    refetch: () => Promise<void>;
};

export function useMyBadges(): UseMyBadgesResult {
    const [data, setData] = useState<Badge[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [isError, setIsError] = useState<boolean>(false);

    const load = useCallback(async (): Promise<void> => {
        try {
            setIsLoading(true);
            setIsError(false);
            const badges = await getUnlockedBadges(TEMP_USER_ID);
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
