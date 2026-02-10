package com.project.itda.global.config;

import com.project.itda.domain.social.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ChatRoomService chatRoomService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        // 연결 시 처리 (필요시)
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String email = (String) headerAccessor.getSessionAttributes().get("userEmail");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (email != null && roomId != null) {
            chatRoomService.userLeft(roomId, email);
            log.info("🔌 WebSocket 연결 해제: roomId={}, email={}", roomId, email);
        }
    }
}
