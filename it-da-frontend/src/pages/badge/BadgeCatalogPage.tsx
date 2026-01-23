// src/pages/badge/BadgeCatalogPage.tsx
import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useBadges } from "@/hooks/badge/useBadges";
import type { UserBadgeDto } from "@/types/badge";
import BadgeGrid from "@/components/badge/BadgeGrid";
import BadgeDetailModal from "@/components/badge/BadgeDetailModal";
import { formatBadgeName } from "@/utils/badge/formatBadgeName";
import { getBadgeEmoji } from "@/utils/badge/getBadgeEmoji";

type Tab = "EARNED" | "LOCKED";

export default function BadgeCatalogPage(): React.ReactElement {
    const navigate = useNavigate();

    const { data, isLoading, isError, refetch } = useBadges();
    const badges: UserBadgeDto[] = Array.isArray(data) ? data : [];

    const [tab, setTab] = useState<Tab>("EARNED");
    const [selected, setSelected] = useState<UserBadgeDto | null>(null);
    const [open, setOpen] = useState(false);

    // ✅ 이 페이지에서만: 강제로 풀폭 + 배경 통일 + 가운데 몰림 방지
    useEffect(() => {
        const html = document.documentElement;
        const body = document.body;
        const root = document.getElementById("root");

        const prevHtmlBg = html.style.background;
        const prevBodyBg = body.style.background;
        const prevBodyMargin = body.style.margin;
        const prevBodyWidth = body.style.width;

        const prevRootMaxWidth = root?.style.maxWidth ?? "";
        const prevRootWidth = root?.style.width ?? "";
        const prevRootMargin = root?.style.margin ?? "";
        const prevRootDisplay = root?.style.display ?? "";
        const prevRootJustify = root?.style.justifyContent ?? "";
        const prevRootAlign = root?.style.alignItems ?? "";

        html.style.background = "#f6f7fb";
        body.style.background = "#f6f7fb";
        body.style.margin = "0";
        body.style.width = "100%";

        if (root) {
            root.style.maxWidth = "none";
            root.style.width = "100%";
            root.style.margin = "0";
            root.style.display = "block";
            root.style.justifyContent = "initial";
            root.style.alignItems = "initial";
        }

        return () => {
            html.style.background = prevHtmlBg;
            body.style.background = prevBodyBg;
            body.style.margin = prevBodyMargin;
            body.style.width = prevBodyWidth;

            if (root) {
                root.style.maxWidth = prevRootMaxWidth;
                root.style.width = prevRootWidth;
                root.style.margin = prevRootMargin;
                root.style.display = prevRootDisplay;
                root.style.justifyContent = prevRootJustify;
                root.style.alignItems = prevRootAlign;
            }
        };
    }, []);

    const earned = useMemo(() => badges.filter((b) => b.unlocked === true), [badges]);
    const locked = useMemo(() => badges.filter((b) => b.unlocked !== true), [badges]);

    const recentTop3 = useMemo(() => {
        return earned
            .slice()
            .sort((a, b) => (Date.parse(b.unlockedAt ?? "") || 0) - (Date.parse(a.unlockedAt ?? "") || 0))
            .slice(0, 3);
    }, [earned]);

    const list = tab === "EARNED" ? earned : locked;

    return (
        <div style={styles.page}>
            <div style={styles.container}>
                {/* ✅ 헤더: 좌(화살표) / 중(도감) / 우(새로고침) */}
                <header style={styles.header}>
                    <div style={styles.headerLeft}>
                        <button
                            type="button"
                            style={styles.backBtn}
                            onClick={() => navigate("/mypage/badges")}
                            aria-label="마이페이지로 이동"
                            title="마이페이지로 이동"
                        >
                            {/* ✅ 문자(←) 대신 SVG 사용: 안 보이는 문제 원천 차단 */}
                            <svg
                                width="18"
                                height="18"
                                viewBox="0 0 24 24"
                                fill="none"
                                xmlns="http://www.w3.org/2000/svg"
                                aria-hidden="true"
                            >
                                <path
                                    d="M15 18L9 12L15 6"
                                    stroke="currentColor"
                                    strokeWidth="2.5"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                />
                            </svg>
                        </button>
                    </div>

                    <h1 style={styles.title}>도감</h1>

                    <div style={styles.headerRight}>
                        <button type="button" style={styles.primaryBtn} onClick={() => void refetch()}>
                            새로고침
                        </button>
                    </div>
                </header>

                {/* ✅ 최근 획득 (문구만 남김: "최근 획득 3개" -> "최근 획득") */}
                <section style={{ marginTop: 18 }}>
                    <h2 style={styles.sectionTitle}>최근 획득</h2>

                    {isLoading && <div style={styles.infoBox}>불러오는 중...</div>}
                    {!isLoading && isError && <div style={styles.errorBox}>불러오기에 실패했습니다.</div>}

                    {!isLoading && !isError && (
                        <div style={styles.recentGrid}>
                            {recentTop3.length === 0 ? (
                                <div style={styles.infoBox}>아직 획득한 배지가 없습니다.</div>
                            ) : (
                                recentTop3.map((b) => (
                                    <button
                                        key={String(b.badgeId ?? b.badgeCode)}
                                        type="button"
                                        style={styles.recentCard}
                                        onClick={() => {
                                            setSelected(b);
                                            setOpen(true);
                                        }}
                                    >
                                        <div style={styles.recentTop}>
                                            <div style={styles.recentIcon}>{getBadgeEmoji(b)}</div>
                                            <div style={styles.recentName}>{formatBadgeName(b.badgeName)}</div>
                                        </div>
                                        {b.description ? <div style={styles.recentDesc}>{b.description}</div> : null}
                                    </button>
                                ))
                            )}
                        </div>
                    )}
                </section>

                {/* 탭 */}
                <section style={{ marginTop: 20 }}>
                    <div style={styles.tabs}>
                        <button
                            type="button"
                            onClick={() => setTab("EARNED")}
                            style={{ ...styles.tabBtn, ...(tab === "EARNED" ? styles.tabActive : null) }}
                        >
                            획득 ({earned.length})
                        </button>
                        <button
                            type="button"
                            onClick={() => setTab("LOCKED")}
                            style={{ ...styles.tabBtn, ...(tab === "LOCKED" ? styles.tabActive : null) }}
                        >
                            미획득 ({locked.length})
                        </button>
                    </div>

                    <div style={{ marginTop: 14 }}>
                        {!isLoading && !isError && (
                            <BadgeGrid
                                badges={list}
                                onBadgeClick={(b) => {
                                    setSelected(b);
                                    setOpen(true);
                                }}
                            />
                        )}
                    </div>
                </section>
            </div>

            <BadgeDetailModal
                open={open}
                badge={selected}
                onClose={() => {
                    setOpen(false);
                    setSelected(null);
                }}
            />
        </div>
    );
}

const styles: Record<string, React.CSSProperties> = {
    page: {
        minHeight: "100vh",
        width: "100%",
        background: "#f6f7fb",
        color: "#111",
    },

    // ✅ 화면 가운데 몰림 방지 + 보기 좋은 최대폭
    container: {
        width: "100%",
        maxWidth: 1440,
        margin: "0 auto",
        padding: "28px 32px",
        boxSizing: "border-box",
    },

    header: {
        display: "grid",
        gridTemplateColumns: "1fr auto 1fr",
        alignItems: "center",
        gap: 12,
        width: "100%",
    },
    headerLeft: {
        minWidth: 0,
        display: "flex",
        alignItems: "center",
        justifyContent: "flex-start",
    },
    headerRight: {
        minWidth: 0,
        display: "flex",
        alignItems: "center",
        justifyContent: "flex-end",
    },

    title: {
        fontSize: 30,
        fontWeight: 900,
        letterSpacing: -0.4,
        margin: 0,
        textAlign: "center",
        lineHeight: 1.1,
    },

    // ✅ 버튼 안에 SVG가 무조건 가운데 오게
    backBtn: {
        width: 40,
        height: 40,
        borderRadius: 12,
        border: "1px solid #e5e7eb",
        background: "#ffffff",
        cursor: "pointer",
        boxShadow: "0 1px 2px rgba(0,0,0,0.06)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        color: "#111827",
    },

    primaryBtn: {
        padding: "10px 14px",
        borderRadius: 12,
        border: "1px solid #e5e7eb",
        background: "#ffffff",
        color: "#111",
        fontWeight: 900,
        cursor: "pointer",
        boxShadow: "0 1px 2px rgba(0,0,0,0.06)",
        whiteSpace: "nowrap",
    },

    sectionTitle: {
        margin: "10px 0 12px 0",
        fontSize: 14,
        fontWeight: 900,
        color: "#111827",
    },

    infoBox: {
        padding: 12,
        fontSize: 13,
        color: "#6b7280",
        background: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: 12,
    },

    errorBox: {
        padding: 12,
        fontSize: 13,
        fontWeight: 900,
        color: "#b91c1c",
        background: "#fff5f5",
        border: "1px solid #fecaca",
        borderRadius: 12,
    },

    recentGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
        gap: 12,
        width: "100%",
    },

    recentCard: {
        borderRadius: 14,
        border: "1px solid #e5e7eb",
        background: "#ffffff",
        padding: 14,
        textAlign: "left",
        cursor: "pointer",
        boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
        width: "100%",
        boxSizing: "border-box",
    },

    recentTop: {
        display: "flex",
        alignItems: "center",
        gap: 10,
    },

    recentIcon: { fontSize: 26, lineHeight: 1 },
    recentName: { fontWeight: 900, fontSize: 15, color: "#111827" },
    recentDesc: { marginTop: 8, fontSize: 13, color: "#4b5563", lineHeight: 1.4 },

    tabs: {
        display: "flex",
        gap: 8,
        flexWrap: "wrap",
    },

    tabBtn: {
        padding: "8px 12px",
        borderRadius: 999,
        border: "1px solid #e5e7eb",
        background: "#ffffff",
        color: "#374151",
        fontWeight: 900,
        cursor: "pointer",
        opacity: 0.9,
    },

    tabActive: {
        background: "#111827",
        border: "1px solid #111827",
        color: "#ffffff",
        opacity: 1,
    },
};
