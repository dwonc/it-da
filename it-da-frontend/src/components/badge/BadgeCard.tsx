// src/components/badge/BadgeCard.tsx
import React from "react";
import type { UserBadgeDto } from "@/api/badge.api";
import { categoryLabel, gradeLabel, safeIcon } from "./badge.utils";

type Props = {
    badge: UserBadgeDto;
    onClick: (badge: UserBadgeDto) => void;
};

export default function BadgeCard({ badge, onClick }: Props): React.ReactElement {
    const locked = !badge.unlocked;

    return (
        <button
            type="button"
            onClick={() => onClick(badge)}
            style={{ ...styles.card, opacity: locked ? 0.55 : 1 }}
        >
            <div style={styles.top}>
                <div style={styles.icon}>{safeIcon(badge.icon)}</div>
                <div style={styles.meta}>
                    <div style={styles.name}>{badge.badgeName}</div>
                    <div style={styles.sub}>
                        {categoryLabel[badge.category]} · {gradeLabel[badge.grade]}
                    </div>
                </div>
            </div>

            <div style={styles.desc}>{badge.description ?? ""}</div>

            <div style={styles.progressWrap}>
                <div style={styles.progressText}>
                    {badge.unlocked ? "획득 완료" : `진행 ${badge.progress} / ${badge.targetValue}`}
                </div>
                <div style={styles.bar}>
                    <div style={{ ...styles.fill, width: `${Math.min(100, badge.progressPercentage ?? 0)}%` }} />
                </div>
            </div>
        </button>
    );
}

const styles: Record<string, React.CSSProperties> = {
    card: {
        width: "100%",
        textAlign: "left",
        border: "1px solid rgba(0,0,0,0.08)",
        borderRadius: 14,
        padding: 12,
        background: "#fff",
        cursor: "pointer",
    },
    top: { display: "flex", gap: 10, alignItems: "center", marginBottom: 8 },
    icon: {
        width: 44,
        height: 44,
        borderRadius: 12,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: 22,
        background: "rgba(0,0,0,0.04)",
    },
    meta: { flex: 1 },
    name: { fontSize: 15, fontWeight: 700 },
    sub: { fontSize: 12, opacity: 0.7, marginTop: 2 },
    desc: { fontSize: 12, opacity: 0.85, minHeight: 32 },
    progressWrap: { marginTop: 10 },
    progressText: { fontSize: 12, opacity: 0.75, marginBottom: 6 },
    bar: {
        width: "100%",
        height: 10,
        borderRadius: 999,
        background: "rgba(0,0,0,0.08)",
        overflow: "hidden",
    },
    fill: { height: "100%", borderRadius: 999, background: "rgba(0,0,0,0.45)" },
};
