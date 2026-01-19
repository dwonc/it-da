package com.project.itda.domain.social.controller;

import com.project.itda.domain.social.repository.ChatParticipantRepository;
import com.project.itda.domain.social.service.ChatMessageService;
import com.project.itda.domain.social.service.ChatRoomService;
import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;


    // ChatStompController.java 수정
    @MessageMapping("/chat/send/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, Map<String, String> message, SimpMessageHeaderAccessor headerAccessor) {
        String email = message.get("email");

        // 💡 1. 자신의 상태를 먼저 DB에 반영 (인원수 카운트 정확도 향상)
        chatRoomService.updateLastReadAt(roomId, email);

        // 💡 2. 이제 3명 중 2명이 접속 안 했더라도 DB에는 3명이 있으므로 count는 3이 됨
        long participantCount = chatParticipantRepository.countByChatRoomId(roomId);

        // 💡 3. unreadCount = 3(전체) - 1(나) = 2 (나머지 2명이 아직 안 읽음)
        long unreadCount = Math.max(0, participantCount - 1);

        User sender = userRepository.findByEmail(email).orElseThrow();
        message.put("senderNickname", sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
        message.put("unreadCount", String.valueOf(unreadCount));
        message.put("senderId", String.valueOf(sender.getUserId()));
        message.put("messageId", String.valueOf(System.currentTimeMillis()));

        chatMessageService.saveMessage(email, roomId, message.get("content"));
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }
    @MessageMapping("/chat/read/{roomId}")
    public void markAsRead(@DestinationVariable Long roomId, Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String email = payload.get("email");

        // ✅ 핵심: 입장(Read) 신호가 올 때 세션에 정보를 저장해야
        // 나중에 WebSocketEventListener가 누구인지 알고 지울 수 있습니다.
        headerAccessor.getSessionAttributes().put("userEmail", email);
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        chatRoomService.updateLastReadAt(roomId, email);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/read", payload);
    }
}