// src/components/badge/BadgeFilters.tsx
import React from "react";
import type { BadgeCategory, BadgeGrade } from "@/types/badge";
import { categoryLabel, gradeLabel, type SortKey } from "./badge.utils";

type Props = {
    category: BadgeCategory | "ALL";
    setCategory: (v: BadgeCategory | "ALL") => void;

    grade: BadgeGrade | "ALL";
    setGrade: (v: BadgeGrade | "ALL") => void;

    onlyUnlocked: boolean;
    setOnlyUnlocked: (v: boolean) => void;

    keyword: string;
    setKeyword: (v: string) => void;

    sortKey: SortKey;
    setSortKey: (v: SortKey) => void;
};

function isSortKey(v: string): v is SortKey {
    return v === "EARNED_FIRST" || v === "PROGRESS_DESC" || v === "GRADE_DESC" || v === "NAME_ASC";
}

export default function BadgeFilters(props: Props): React.ReactElement {
    const { category, setCategory, grade, setGrade, onlyUnlocked, setOnlyUnlocked, keyword, setKeyword, sortKey, setSortKey } =
        props;

    return (
        <div style={styles.wrap}>
            <div style={styles.row}>
                <select
                    style={styles.select}
                    value={category}
                    onChange={(e) => {
                        const v = e.target.value;
                        if (v === "ALL") setCategory("ALL");
                        else setCategory(v as BadgeCategory);
                    }}
                >
                    <option value="ALL">전체 카테고리</option>
                    {Object.entries(categoryLabel).map(([k, v]) => (
                        <option key={k} value={k}>
                            {v}
                        </option>
                    ))}
                </select>

                <select
                    style={styles.select}
                    value={grade}
                    onChange={(e) => {
                        const v = e.target.value;
                        if (v === "ALL") setGrade("ALL");
                        else setGrade(v as BadgeGrade);
                    }}
                >
                    <option value="ALL">전체 등급</option>
                    {Object.entries(gradeLabel).map(([k, v]) => (
                        <option key={k} value={k}>
                            {v}
                        </option>
                    ))}
                </select>

                <select
                    style={styles.select}
                    value={sortKey}
                    onChange={(e) => {
                        const v = e.target.value;
                        if (isSortKey(v)) setSortKey(v);
                    }}
                >
                    <option value="EARNED_FIRST">정렬: 획득 우선</option>
                    <option value="PROGRESS_DESC">정렬: 진행도 높은 순</option>
                    <option value="GRADE_DESC">정렬: 등급 높은 순</option>
                    <option value="NAME_ASC">정렬: 이름순</option>
                </select>

                <label style={styles.checkboxLabel}>
                    <input type="checkbox" checked={onlyUnlocked} onChange={(e) => setOnlyUnlocked(e.target.checked)} />
                    <span style={{ marginLeft: 6 }}>획득만 보기</span>
                </label>
            </div>

            <input
                style={styles.search}
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="배지 이름/코드 검색"
            />
        </div>
    );
}

const styles: Record<string, React.CSSProperties> = {
    wrap: {
        border: "1px solid rgba(0,0,0,0.08)",
        borderRadius: 12,
        padding: 12,
        background: "#fff",
        marginBottom: 12,
    },
    row: {
        display: "flex",
        gap: 8,
        flexWrap: "wrap",
        alignItems: "center",
        marginBottom: 10,
    },
    select: {
        height: 36,
        padding: "0 10px",
        borderRadius: 10,
        border: "1px solid rgba(0,0,0,0.12)",
    },
    checkboxLabel: {
        display: "flex",
        alignItems: "center",
        height: 36,
        padding: "0 10px",
        borderRadius: 10,
        border: "1px solid rgba(0,0,0,0.08)",
        background: "rgba(0,0,0,0.02)",
    },
    search: {
        width: "100%",
        height: 40,
        padding: "0 12px",
        borderRadius: 10,
        border: "1px solid rgba(0,0,0,0.12)",
    },
};
