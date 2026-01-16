// components/ai/AiRecommendSection.tsx
import { useCallback, useEffect, useMemo, useState } from "react";
import axios from "axios";
import AIRecommendCard from "@/components/ai/AiRecommendCard";
import { AiMeeting, PersonalizedMeetingResponse } from "@/types/ai.types";
import { mapPersonalizedToAiMeeting } from "@/utils/meetingMapper";
import { useMatchScores } from "@/hooks/ai/useMatchScore";

interface Props {
  userId?: number;
}

export default function AiRecommendSection({ userId }: Props) {
  const [meeting, setMeeting] = useState<AiMeeting | null>(null);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchRecommendation = useCallback(async () => {
    if (!userId) return;

    setIsRefreshing(true);
    setError(null);

    try {
      const res = await axios.get<PersonalizedMeetingResponse>(
        `http://localhost:8080/api/ai/recommendations/personalized/${userId}`,
        { withCredentials: true }
      );

      if (!res.data.success) {
        setMeeting(null);
        setError("추천 가능한 모임이 없습니다.");
        return;
      }

      setMeeting(mapPersonalizedToAiMeeting(res.data));
    } catch (e: any) {
      console.error("❌ 개인화 추천 실패:", e);
      setMeeting(null);
      setError("추천을 불러오지 못했습니다.");
    } finally {
      setIsRefreshing(false);
    }
  }, [userId]);

  useEffect(() => {
    fetchRecommendation();
  }, [fetchRecommendation]);

  const meetingId = meeting?.meetingId;
  const { matchMap, loading } = useMatchScores(
    userId,
    meetingId ? [meetingId] : []
  );
  const matchPercentage = meetingId ? matchMap[meetingId]?.matchPercentage : 0;

  return (
    <section>
      {error && <div style={{ color: "crimson" }}>⚠️ {error}</div>}

      {meeting && (
        <AIRecommendCard
          key={meeting.meetingId}
          meeting={meeting}
          matchPercentage={matchPercentage}
          loading={loading}
          onRefresh={fetchRecommendation}
          isRefreshing={isRefreshing}
        />
      )}
    </section>
  );
}
