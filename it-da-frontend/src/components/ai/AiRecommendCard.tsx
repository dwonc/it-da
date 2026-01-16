// components/ai/AiRecommendCard.tsx
import { useNavigate } from "react-router-dom";
import type { AiMeeting } from "@/types/ai.types";
import "./AiRecommendCard.css";

type AIRecommendCardProps = {
  meeting: AiMeeting; // 너 프로젝트 타입(AiMeeting/Meeting) 있으면 그걸로 교체
  onRefresh?: () => Promise<void> | void;
  isRefreshing?: boolean;
  matchPercentage?: number;
  loading?: boolean;
};

const AIRecommendCard = ({
  meeting,
  matchPercentage = 0,
  loading = false,
  onRefresh,
  isRefreshing = false,
}: AIRecommendCardProps) => {
  const navigate = useNavigate();
  const meetingId = meeting?.meetingId;

  const handleCardClick = () => {
    if (!meetingId) return;
    navigate(`/meetings/${meetingId}`);
  };

  const handleRefreshClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    onRefresh?.();
  };

  if (!meeting) return null;

  return (
    <div className="ai-recommend-section">
      <div className="ai-header">
        <div className="ai-badge">
          🤖 AI 매칭률 {loading ? "계산중..." : `${matchPercentage}%`}
        </div>

        {onRefresh && (
          <button
            className="refresh-btn"
            onClick={handleRefreshClick}
            disabled={Boolean(isRefreshing) || Boolean(loading)}
          >
            {isRefreshing || loading ? "🔄" : "↻"} 다시 추천받기
          </button>
        )}
      </div>

      <div className="recommend-card" onClick={handleCardClick}>
        <div className="card-image">
          {meeting.imageUrl ? (
            <img src={meeting.imageUrl} alt={meeting.title} />
          ) : (
            <div className="image-placeholder">🎯</div>
          )}
        </div>

        <div className="card-info">
          <h3 className="card-title">{meeting.title}</h3>
          <p className="card-desc">{meeting.description}</p>

          <div className="card-tags">
            {meeting.category && <span>#{meeting.category}</span>}
            {meeting.subcategory && <span>#{meeting.subcategory}</span>}
            {meeting.vibe && <span>#{meeting.vibe}</span>}
          </div>

          <div className="card-meta">
            <span>📍 {meeting.locationName}</span>
            <span>
              👥 {meeting.currentParticipants}/{meeting.maxParticipants}명
            </span>
          </div>

          <div className="card-actions">
            <button className="btn-primary">🌙 톡방 입장하기</button>
            <button className="btn-secondary">상세보기</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AIRecommendCard;
