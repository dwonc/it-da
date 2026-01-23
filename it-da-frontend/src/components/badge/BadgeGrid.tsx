// src/components/badge/BadgeGrid.tsx
import React from "react";
import type { UserBadgeDto } from "@/types/badge";
import { formatBadgeName } from "@/utils/badge/formatBadgeName";
import { getBadgeEmoji } from "@/utils/badge/getBadgeEmoji";

type Props = {
    badges: UserBadgeDto[];
    onBadgeClick?: (badge: UserBadgeDto) => void;
};

function clampPercent(v: unknown): number {
    const n = typeof v === "number" && Number.isFinite(v) ? v : 0;
    return Math.max(0, Math.min(100, n));
}

export default function BadgeGrid({ badges, onBadgeClick }: Props): React.ReactElement {
    return (
        <div style={styles.grid}>
            {badges.map((b) => {
                const name = formatBadgeName(b.badgeName);
                const icon = getBadgeEmoji({
                    badgeCode: b.badgeCode,
                    category: String(b.category ?? ""),
                    grade: String(b.grade ?? ""),
                    badgeName: b.badgeName,
                    icon: b.icon ?? null,
                });

                const pct = clampPercent(b.progressPercentage);
                const progress = typeof b.progress === "number" ? b.progress : 0;
                const targetValue = typeof b.targetValue === "number" ? b.targetValue : null;

                return (
                    <button
                        key={String(b.badgeId ?? b.badgeCode)}
                        type="button"
                        style={{ ...styles.card, ...(b.unlocked ? styles.cardUnlocked : styles.cardLocked) }}
                        onClick={() => onBadgeClick?.(b)}
                    >
                        <div style={styles.topRow}>
                            <div style={styles.icon}>{icon}</div>
                            <div style={styles.name} title={name}>
                                {name}
                            </div>
                        </div>

                        <div style={styles.progressOuter}>
                            <div style={{ ...styles.progressInner, width: `${pct}%` }} />
                        </div>

                        <div style={styles.progressText}>
                            {targetValue === null ? (
                                <>
                                    {progress} ({Math.floor(pct)}%)
                                </>
                            ) : (
                                <>
                                    {progress} / {targetValue} ({Math.floor(pct)}%)
                                </>
                            )}
                        </div>
                    </button>
                );
            })}
        </div>
    );
}

const styles: Record<string, React.CSSProperties> = {
    grid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))",
        gap: 12,
        width: "100%",
    },
    card: {
        textAlign: "left",
        borderRadius: 14,
        border: "1px solid #e5e7eb",
        background: "#ffffff",
        padding: 14,
        cursor: "pointer",
        color: "#111827",
        boxShadow: "0 2px 10px rgba(0,0,0,0.05)",
        width: "100%",
        boxSizing: "border-box",
    },
    cardUnlocked: {
        opacity: 1,
    },
    cardLocked: {
        opacity: 0.45,
    },
    topRow: {
        display: "flex",
        alignItems: "center",
        gap: 10,
        minWidth: 0,
    },
    icon: {
        width: 36,
        height: 36,
        borderRadius: 12,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: 20,
        background: "#f3f4f6",
        border: "1px solid #e5e7eb",
        flex: "0 0 auto",
    },
    name: {
        fontWeight: 900,
        fontSize: 14,
        overflow: "hidden",
        textOverflow: "ellipsis",
        whiteSpace: "nowrap",
    },
    progressOuter: {
        marginTop: 10,
        height: 8,
        borderRadius: 999,
        background: "#f3f4f6",
        overflow: "hidden",
        border: "1px solid #e5e7eb",
    },
    progressInner: {
        height: "100%",
        background: "#111827",
    },
    progressText: {
        marginTop: 8,
        fontSize: 12,
        color: "#374151",
    },
};
