package com.project.itda.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Builder
public class SessionInfoResponse {
    private String sessionId;
    private Long userId;
    private String username;
    private String userStatus;
    private Date creationTime;
    private Date lastAccessedTime;
    private Integer maxInactiveInterval;
    private Boolean isLoggedIn;
}