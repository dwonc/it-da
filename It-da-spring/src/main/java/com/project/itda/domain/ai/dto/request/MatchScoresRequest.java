package com.project.itda.domain.ai.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class MatchScoresRequest {
    private Long user_id;
    private List<Long> meeting_ids;
}
