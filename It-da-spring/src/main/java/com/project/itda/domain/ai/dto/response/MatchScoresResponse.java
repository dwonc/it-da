package com.project.itda.domain.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchScoresResponse {
    private boolean success;
    private Long userId;
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private Long meetingId;
        private Double predictedRating;
        private Double percentile;
        private Integer matchPercentage;
        private String matchLevel;
    }
}