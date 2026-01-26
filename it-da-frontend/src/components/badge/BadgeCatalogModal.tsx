// src/components/badge/BadgeCatalogModal.tsx
import React, { useMemo, useState } from "react";
import type { UserBadgeDto, BadgeCategory } from "@/types/badge";
import { useBadges } from "@/hooks/badge/useBadges";
import "./BadgeCatalogModal.css";

interface Props {
    open: boolean;
    onClose: () => void;
}

// 카테고리 설정
const CATEGORIES: { key: BadgeCategory | "ALL"; label: string; icon: string }[] = [
    { key: "ALL", label: "전체", icon: "📖" },
    { key: "PARTICIPATION", label: "참여", icon: "🎯" },
    { key: "AI", label: "AI", icon: "🤖" },
    { key: "DISTANCE", label: "거리", icon: "📍" },
    { key: "TIME", label: "시간", icon: "⏰" },
    { key: "PERSONALITY", label: "성향", icon: "💫" },
    { key: "CATEGORY", label: "카테고리", icon: "📂" },
    { key: "REVIEW", label: "리뷰", icon: "⭐" },
    { key: "SOCIAL", label: "소셜", icon: "👥" },
    { key: "HOST", label: "주최", icon: "🎪" },
    { key: "SPECIAL", label: "특별", icon: "🌟" },
];

// 등급 색상
const GRADE_COLORS: Record<string, { bg: string; border: string; text: string; glow: string }> = {
    COMMON: { bg: "#f3f4f6", border: "#9ca3af", text: "#6b7280", glow: "rgba(156, 163, 175, 0.3)" },
    RARE: { bg: "#dbeafe", border: "#3b82f6", text: "#1d4ed8", glow: "rgba(59, 130, 246, 0.4)" },
    EPIC: { bg: "#ede9fe", border: "#8b5cf6", text: "#6d28d9", glow: "rgba(139, 92, 246, 0.4)" },
    LEGENDARY: { bg: "#fef3c7", border: "#f59e0b", text: "#b45309", glow: "rgba(245, 158, 11, 0.5)" },
};

const GRADE_LABELS: Record<string, string> = {
    COMMON: "일반",
    RARE: "레어",
    EPIC: "에픽",
    LEGENDARY: "전설",
};

export default function BadgeCatalogModal({ open, onClose }: Props): React.ReactElement | null {
    const { data, isLoading } = useBadges();
    const badges: UserBadgeDto[] = Array.isArray(data) ? data : [];

    const [activeCategory, setActiveCategory] = useState<BadgeCategory | "ALL">("ALL");
    const [selectedBadge, setSelectedBadge] = useState<UserBadgeDto | null>(null);

    // 통계
    const stats = useMemo(() => {
        const total = badges.length;
        const unlocked = badges.filter((b) => b.unlocked).length;
        return { total, unlocked };
    }, [badges]);

    // 카테고리별 필터링
    const filteredBadges = useMemo(() => {
        if (activeCategory === "ALL") return badges;
        return badges.filter((b) => b.category === activeCategory);
    }, [badges, activeCategory]);

    // 카테고리별 통계
    const categoryStats = useMemo(() => {
        const result: Record<string, { total: number; unlocked: number }> = {};
        badges.forEach((b) => {
            if (!result[b.category]) {
                result[b.category] = { total: 0, unlocked: 0 };
            }
            result[b.category].total++;
            if (b.unlocked) result[b.category].unlocked++;
        });
        return result;
    }, [badges]);

    if (!open) return null;

    return (
        <div className="badge-catalog-backdrop" onClick={onClose}>
            <div className="badge-catalog-modal" onClick={(e) => e.stopPropagation()}>
                {/* 헤더 */}
                <header className="badge-catalog-header">
                    <div className="badge-catalog-title-area">
                        <span className="badge-catalog-icon">📖</span>
                        <h2 className="badge-catalog-title">배지 도감</h2>
                    </div>
                    <div className="badge-catalog-stats">
                        <span className="badge-catalog-unlocked">{stats.unlocked}</span>
                        <span className="badge-catalog-divider">/</span>
                        <span className="badge-catalog-total">{stats.total}</span>
                    </div>
                    <button className="badge-catalog-close" onClick={onClose}>
                        ✕
                    </button>
                </header>

                {/* 카테고리 탭 */}
                <nav className="badge-catalog-tabs">
                    {CATEGORIES.map((cat) => {
                        const catStats = cat.key === "ALL"
                            ? stats
                            : categoryStats[cat.key] || { total: 0, unlocked: 0 };

                        return (
                            <button
                                key={cat.key}
                                className={`badge-catalog-tab ${activeCategory === cat.key ? "active" : ""}`}
                                onClick={() => setActiveCategory(cat.key)}
                            >
                                <span className="tab-icon">{cat.icon}</span>
                                <span className="tab-label">{cat.label}</span>
                                <span className="tab-count">
                                    {catStats.unlocked}/{catStats.total}
                                </span>
                            </button>
                        );
                    })}
                </nav>

                {/* 배지 그리드 */}
                <div className="badge-catalog-content">
                    {isLoading ? (
                        <div className="badge-catalog-loading">
                            <div className="loading-spinner"></div>
                            <span>불러오는 중...</span>
                        </div>
                    ) : filteredBadges.length === 0 ? (
                        <div className="badge-catalog-empty">
                            <span>이 카테고리에 배지가 없습니다.</span>
                        </div>
                    ) : (
                        <div className="badge-catalog-grid">
                            {filteredBadges.map((badge) => {
                                const gradeColor = GRADE_COLORS[badge.grade] || GRADE_COLORS.COMMON;
                                const isUnlocked = badge.unlocked;

                                return (
                                    <button
                                        key={badge.badgeId || badge.badgeCode}
                                        className={`badge-catalog-card ${isUnlocked ? "unlocked" : "locked"} grade-${badge.grade.toLowerCase()}`}
                                        onClick={() => setSelectedBadge(badge)}
                                        style={{
                                            "--grade-bg": gradeColor.bg,
                                            "--grade-border": gradeColor.border,
                                            "--grade-text": gradeColor.text,
                                            "--grade-glow": gradeColor.glow,
                                        } as React.CSSProperties}
                                    >
                                        {/* 배지 아이콘 */}
                                        <div className="badge-card-icon-wrapper">
                                            <div className="badge-card-icon">
                                                {badge.icon || "🏅"}
                                            </div>
                                            {!isUnlocked && (
                                                <div className="badge-card-lock">🔒</div>
                                            )}
                                        </div>

                                        {/* 배지 이름 */}
                                        <div className="badge-card-name">{badge.badgeName}</div>

                                        {/* 등급 */}
                                        <div className="badge-card-grade">
                                            {GRADE_LABELS[badge.grade] || badge.grade}
                                        </div>

                                        {/* 진행도 */}
                                        <div className="badge-card-progress">
                                            <div className="badge-card-progress-bar">
                                                <div
                                                    className="badge-card-progress-fill"
                                                    style={{
                                                        width: `${Math.min(100, badge.progressPercentage || 0)}%`,
                                                    }}
                                                />
                                            </div>
                                            <div className="badge-card-progress-text">
                                                {isUnlocked ? (
                                                    "✓ 획득"
                                                ) : (
                                                    `${badge.progress || 0}/${badge.targetValue || 0}`
                                                )}
                                            </div>
                                        </div>
                                    </button>
                                );
                            })}
                        </div>
                    )}
                </div>

                {/* 하단 안내 */}
                <footer className="badge-catalog-footer">
                    💡 배지를 클릭하면 상세 조건을 확인할 수 있어요!
                </footer>

                {/* 상세 모달 */}
                {selectedBadge && (
                    <div className="badge-detail-backdrop" onClick={() => setSelectedBadge(null)}>
                        <div className="badge-detail-modal" onClick={(e) => e.stopPropagation()}>
                            <div
                                className="badge-detail-header"
                                style={{
                                    background: `linear-gradient(135deg, ${GRADE_COLORS[selectedBadge.grade]?.bg || "#f3f4f6"} 0%, white 100%)`,
                                }}
                            >
                                <div className="badge-detail-icon">
                                    {selectedBadge.icon || "🏅"}
                                </div>
                                <div className="badge-detail-info">
                                    <h3 className="badge-detail-name">{selectedBadge.badgeName}</h3>
                                    <div
                                        className="badge-detail-grade"
                                        style={{
                                            background: GRADE_COLORS[selectedBadge.grade]?.bg,
                                            color: GRADE_COLORS[selectedBadge.grade]?.text,
                                            border: `1px solid ${GRADE_COLORS[selectedBadge.grade]?.border}`,
                                        }}
                                    >
                                        {GRADE_LABELS[selectedBadge.grade]}
                                    </div>
                                </div>
                                <button
                                    className="badge-detail-close"
                                    onClick={() => setSelectedBadge(null)}
                                >
                                    ✕
                                </button>
                            </div>

                            <div className="badge-detail-body">
                                <div className="badge-detail-section">
                                    <div className="badge-detail-label">설명</div>
                                    <div className="badge-detail-value">
                                        {selectedBadge.description || "-"}
                                    </div>
                                </div>

                                <div className="badge-detail-section">
                                    <div className="badge-detail-label">카테고리</div>
                                    <div className="badge-detail-value">
                                        {CATEGORIES.find((c) => c.key === selectedBadge.category)?.icon}{" "}
                                        {CATEGORIES.find((c) => c.key === selectedBadge.category)?.label || selectedBadge.category}
                                    </div>
                                </div>

                                <div className="badge-detail-section">
                                    <div className="badge-detail-label">진행도</div>
                                    <div className="badge-detail-progress-wrapper">
                                        <div className="badge-detail-progress-bar">
                                            <div
                                                className="badge-detail-progress-fill"
                                                style={{
                                                    width: `${Math.min(100, selectedBadge.progressPercentage || 0)}%`,
                                                    background: GRADE_COLORS[selectedBadge.grade]?.border,
                                                }}
                                            />
                                        </div>
                                        <div className="badge-detail-progress-text">
                                            {selectedBadge.progress || 0} / {selectedBadge.targetValue || 0}
                                            {" "}({Math.round(selectedBadge.progressPercentage || 0)}%)
                                        </div>
                                    </div>
                                </div>

                                {selectedBadge.unlocked && selectedBadge.unlockedAt && (
                                    <div className="badge-detail-section">
                                        <div className="badge-detail-label">획득일</div>
                                        <div className="badge-detail-value">
                                            🎉 {new Date(selectedBadge.unlockedAt).toLocaleDateString("ko-KR")}
                                        </div>
                                    </div>
                                )}

                                <div className="badge-detail-status">
                                    {selectedBadge.unlocked ? (
                                        <span className="badge-status-unlocked">✅ 획득 완료!</span>
                                    ) : (
                                        <span className="badge-status-locked">🔒 아직 획득하지 않았어요</span>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
