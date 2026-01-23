// src/api/badge.api.ts
import axios from "axios";
import type { UserBadgeDto } from "@/types/badge";
export type { UserBadgeDto, BadgeCategory, BadgeGrade } from "@/types/badge";

const http = axios.create({
    baseURL: import.meta.env?.VITE_API_BASE_URL ?? "http://localhost:8080",
    withCredentials: true,
    headers: { "Content-Type": "application/json" },
});

/**
 * ✅ 개발용 임시 유저 ID
 * - 로그인/세션 붙기 전까지는 프런트에서 고정값으로 보냅니다.
 */
function getDevUserId(): string {
    const saved = localStorage.getItem("devUserId");
    return saved && saved.trim() !== "" ? saved : "1";
}

// ✅ 모든 요청에 X-User-Id 헤더 자동 추가
http.interceptors.request.use((config) => {
    const userId = getDevUserId();
    config.headers = config.headers ?? {};
    config.headers["X-User-Id"] = userId;
    return config;
});

export async function getUserBadges(): Promise<UserBadgeDto[]> {
    const res = await http.get<UserBadgeDto[]>("/api/badges");
    return res.data;
}

export async function updateAllBadges(): Promise<unknown> {
    const res = await http.post("/api/badges/update-all");
    return res.data;
}

export async function updateBadgeProgress(badgeCode: string): Promise<unknown> {
    const res = await http.post(`/api/badges/${encodeURIComponent(badgeCode)}/update`);
    return res.data;
}
