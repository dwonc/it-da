// src/components/badge/BadgeProgress.tsx
import React from "react";

type Props = {
    progress: number;
    targetValue: number;
};

export default function BadgeProgress(props: Props): React.ReactElement {
    const { progress, targetValue } = props;

    const safeTarget = Math.max(0, targetValue ?? 0);
    const safeProgress = Math.max(0, progress ?? 0);

    const percent =
        safeTarget === 0 ? 0 : Math.min(100, Math.floor((safeProgress * 100) / safeTarget));

    return (
        <div style={{ width: "100%" }}>
            <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, opacity: 0.8 }}>
                <span>{percent}%</span>
                <span>
          {safeProgress}/{safeTarget}
        </span>
            </div>

            <div
                style={{
                    height: 8,
                    borderRadius: 999,
                    background: "rgba(0,0,0,0.08)",
                    overflow: "hidden",
                    marginTop: 6,
                }}
            >
                <div
                    style={{
                        height: "100%",
                        width: `${percent}%`,
                        borderRadius: 999,
                        background: "rgba(107,79,79,0.9)",
                    }}
                />
            </div>
        </div>
    );
}
