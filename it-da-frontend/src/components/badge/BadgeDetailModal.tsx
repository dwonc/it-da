// src/components/badge/BadgeDetailModal.tsx
import React from "react";
import type { UserBadgeDto } from "@/types/badge";
import { getBadgeEmoji } from "@/utils/badge/getBadgeEmoji";

type Props = {
    open: boolean;
    badge: UserBadgeDto | null;
    onClose: () => void;
};

export default function BadgeDetailModal({ open, badge, onClose }: Props): React.ReactElement | null {
    if (!open || !badge) return null;

    return (
        <div style={styles.backdrop} onClick={onClose} role="presentation">
            <div style={styles.modal} onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
                <div style={styles.header}>
                    <div style={styles.titleRow}>
                        <div style={styles.icon}>{getBadgeEmoji(badge)}</div>
                        {/* ✅ 제목은 badgeName만: T01/R10 같은 icon 코드 절대 출력 금지 */}
                        <div style={styles.title}>{badge.badgeName ?? ""}</div>
                    </div>

                    <button type="button" onClick={onClose} style={styles.closeBtn}>
                        닫기
                    </button>
                </div>

                <div style={styles.body}>
                    {/* ✅ “획득 · PARTICIPATION · COMMON” 같은 텍스트 출력 금지 */}
                    {/* 설명 */}
                    <div style={styles.label}>설명</div>
                    <div style={styles.text}>{badge.description ?? "-"}</div>

                    {/* 진행도 */}
                    <div style={{ marginTop: 14 }}>
                        <div style={styles.label}>진행도</div>
                        <div style={styles.text}>
                            {badge.progress} / {badge.targetValue ?? 0} ({badge.progressPercentage ?? 0}%)
                        </div>
                    </div>

                    {badge.unlockedAt ? (
                        <div style={{ marginTop: 14 }}>
                            <div style={styles.label}>획득일</div>
                            <div style={styles.text}>{badge.unlockedAt}</div>
                        </div>
                    ) : null}
                </div>

                <div style={styles.footer}>
                    <button type="button" onClick={onClose} style={styles.okBtn}>
                        확인
                    </button>
                </div>
            </div>
        </div>
    );
}

const styles: Record<string, React.CSSProperties> = {
    backdrop: {
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.55)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 16,
        zIndex: 9999,
    },
    modal: {
        width: "min(760px, 100%)",
        borderRadius: 18,
        border: "1px solid rgba(255,255,255,0.10)",
        background: "rgba(20,20,20,0.96)",
        color: "rgba(255,255,255,0.92)",
        overflow: "hidden",
    },
    header: {
        padding: 16,
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: 12,
        borderBottom: "1px solid rgba(255,255,255,0.08)",
    },
    titleRow: { display: "flex", alignItems: "center", gap: 10 },
    icon: { fontSize: 26, lineHeight: 1 },
    title: { fontSize: 18, fontWeight: 900 },
    closeBtn: {
        padding: "8px 12px",
        borderRadius: 12,
        border: "1px solid rgba(255,255,255,0.12)",
        background: "rgba(255,255,255,0.06)",
        color: "rgba(255,255,255,0.92)",
        fontWeight: 900,
        cursor: "pointer",
    },
    body: { padding: 16 },
    label: { fontSize: 12, opacity: 0.75, fontWeight: 900, marginBottom: 6 },
    text: { fontSize: 14, lineHeight: 1.5, opacity: 0.95 },
    footer: {
        padding: 16,
        display: "flex",
        justifyContent: "flex-end",
        borderTop: "1px solid rgba(255,255,255,0.08)",
    },
    okBtn: {
        padding: "10px 14px",
        borderRadius: 12,
        border: "1px solid rgba(255,255,255,0.12)",
        background: "rgba(255,255,255,0.08)",
        color: "rgba(255,255,255,0.92)",
        fontWeight: 900,
        cursor: "pointer",
    },
};
