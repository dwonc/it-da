// src/utils/badge/getBadgeEmoji.ts

export type BadgeLike = {
    badgeCode?: string;
    category?: string;
    grade?: string;
    badgeName?: string;
    name?: string;
    icon?: string | null;
};

function norm(v?: string): string {
    return String(v ?? "").trim().toLowerCase();
}

/**
 * - 유니코드 프로퍼티 이스케이프(\p{...})를 안 씁니다.
 * - "짧고, URL/파일경로/숫자만 아니면" 이모지로 간주
 */
function looksLikeEmoji(s: string): boolean {
    const t = String(s ?? "").trim();
    if (!t) return false;

    if (
        t.includes("/") ||
        t.includes("http") ||
        t.includes(".png") ||
        t.includes(".jpg") ||
        t.includes(".jpeg") ||
        t.includes(".svg") ||
        t.includes(".webp")
    ) {
        return false;
    }

    if (/^\d+$/.test(t)) return false;

    return t.length <= 4;
}

export function getBadgeEmoji(badge: BadgeLike): string {
    const code = norm(badge.badgeCode);
    const category = norm(badge.category);
    const title = norm(badge.badgeName ?? badge.name);

    const blob = `${code} ${category} ${title}`;

    if (blob.includes("streak") || blob.includes("attendance") || blob.includes("연속") || blob.includes("출석")) {
        return "📆";
    }
    if (blob.includes("review") || blob.includes("리뷰")) {
        return "✍️";
    }
    if (blob.includes("chat") || blob.includes("채팅") || blob.includes("메시지") || blob.includes("message")) {
        return "💬";
    }
    if (blob.includes("participation") || blob.includes("meeting") || blob.includes("모임") || blob.includes("참여")) {
        return "🤝";
    }

    const iconRaw = String(badge.icon ?? "").trim();
    if (looksLikeEmoji(iconRaw)) return iconRaw;

    return "🏅";
}
