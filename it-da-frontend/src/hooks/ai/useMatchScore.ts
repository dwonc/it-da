// hooks/ai/useMatchScore.ts
import { useEffect, useRef, useState } from "react";
import axios from "axios";

type MatchItem = {
  meetingId: number;
  matchPercentage: number;
  matchLevel?: string;
  predictedRating?: number;
  percentile?: number;
};

export function useMatchScores(userId?: number, meetingIds: number[] = []) {
  const [matchMap, setMatchMap] = useState<Record<number, MatchItem>>({});
  const [loading, setLoading] = useState(false);

  const inFlightRef = useRef(false);
  const abortRef = useRef<AbortController | null>(null);
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    if (!userId || meetingIds.length === 0) {
      setMatchMap({});
      return;
    }

    // ✅ 1) debounce: 250ms 동안 변화 없을 때만 호출
    if (timerRef.current) window.clearTimeout(timerRef.current);

    timerRef.current = window.setTimeout(async () => {
      // ✅ 2) 이미 요청 중이면 이전 요청 취소하고 새로
      abortRef.current?.abort();
      abortRef.current = new AbortController();

      // ✅ 3) 동시에 2개 이상 못 나가게
      if (inFlightRef.current) return;
      inFlightRef.current = true;

      setLoading(true);
      try {
        const res = await axios.post(
          "http://localhost:8080/api/ai/recommendations/match-scores",
          { userId, meetingIds },
          {
            withCredentials: true,
            timeout: 30000, // ✅ 일단 30초로(원인 잡히면 줄여)
            signal: abortRef.current.signal,
          }
        );

        const items: MatchItem[] = res.data?.items ?? [];
        const map: Record<number, MatchItem> = {};
        for (const it of items) map[it.meetingId] = it;

        setMatchMap(map);
      } catch (e: any) {
        if (e?.name !== "CanceledError" && e?.code !== "ERR_CANCELED") {
          console.error("❌ match-scores 호출 실패:", e);
        }
      } finally {
        inFlightRef.current = false;
        setLoading(false);
      }
    }, 250);

    return () => {
      if (timerRef.current) window.clearTimeout(timerRef.current);
      abortRef.current?.abort();
    };
  }, [userId, meetingIds.join(",")]); // ✅ JSON.stringify 말고 join

  return { matchMap, loading };
}
