package com.project.itda.global.websocket;

import com.project.itda.domain.social.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ChatRoomService chatRoomService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // ChatStompController의 sendMessage에서 저장한 세션 정보를 꺼냅니다.
        String email = (String) headerAccessor.getSessionAttributes().get("userEmail");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");

        if (email != null && roomId != null) {
            // ✅ 연결이 끊기면 참여자 테이블에서 삭제하여 카운트 감소
            chatRoomService.leaveChatRoom(roomId, email);
        }
    }
}