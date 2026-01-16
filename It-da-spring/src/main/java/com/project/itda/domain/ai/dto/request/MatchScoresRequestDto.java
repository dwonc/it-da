package com.project.itda.domain.ai.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MatchScoresRequestDto {
    private Long userId;
    private List<Long> meetingIds;
}
