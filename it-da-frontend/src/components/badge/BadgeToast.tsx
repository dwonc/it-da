// src/components/badge/BadgeToast.tsx
import React from "react";
import "./BadgeToast.css";

interface BadgeToastProps {
    visible: boolean;
    badgeName: string;
    badgeIcon: string;
    badgeGrade: string;
    badgeDescription: string;
    onClose: () => void;
}

const GRADE_COLORS: Record<string, { bg: string; border: string; glow: string }> = {
    COMMON: { bg: "#f3f4f6", border: "#9ca3af", glow: "rgba(156, 163, 175, 0.5)" },
    RARE: { bg: "#dbeafe", border: "#3b82f6", glow: "rgba(59, 130, 246, 0.5)" },
    EPIC: { bg: "#ede9fe", border: "#8b5cf6", glow: "rgba(139, 92, 246, 0.5)" },
    LEGENDARY: { bg: "#fef3c7", border: "#f59e0b", glow: "rgba(245, 158, 11, 0.6)" },
};

const GRADE_LABELS: Record<string, string> = {
    COMMON: "일반",
    RARE: "레어",
    EPIC: "에픽",
    LEGENDARY: "전설",
};

/**
 * 배지 획득 토스트 알림 컴포넌트
 */
export default function BadgeToast({
                                       visible,
                                       badgeName,
                                       badgeIcon,
                                       badgeGrade,
                                       badgeDescription,
                                       onClose,
                                   }: BadgeToastProps): React.ReactElement | null {
    if (!visible) return null;

    const gradeColor = GRADE_COLORS[badgeGrade] || GRADE_COLORS.COMMON;

    return (
        <div className="badge-toast-overlay">
            <div
                className="badge-toast"
                style={{
                    borderColor: gradeColor.border,
                    boxShadow: `0 0 30px ${gradeColor.glow}, 0 20px 60px rgba(0, 0, 0, 0.3)`,
                }}
            >
                {/* 배경 파티클 효과 */}
                <div className="badge-toast-particles">
                    <span>✨</span>
                    <span>⭐</span>
                    <span>🌟</span>
                    <span>✨</span>
                    <span>⭐</span>
                </div>

                {/* 아이콘 */}
                <div
                    className="badge-toast-icon"
                    style={{
                        background: gradeColor.bg,
                        borderColor: gradeColor.border,
                    }}
                >
                    {badgeIcon || "🏅"}
                </div>

                {/* 내용 */}
                <div className="badge-toast-content">
                    <div className="badge-toast-header">
                        🎉 배지 획득!
                    </div>
                    <div className="badge-toast-name">
                        {badgeName}
                    </div>
                    <div
                        className="badge-toast-grade"
                        style={{
                            background: gradeColor.bg,
                            color: gradeColor.border,
                            borderColor: gradeColor.border,
                        }}
                    >
                        {GRADE_LABELS[badgeGrade] || badgeGrade}
                    </div>
                    <div className="badge-toast-desc">
                        {badgeDescription}
                    </div>
                </div>

                {/* 닫기 버튼 */}
                <button className="badge-toast-close" onClick={onClose}>
                    ✕
                </button>
            </div>
        </div>
    );
}
