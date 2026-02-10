package com.project.itda.domain.social.controller;

import com.project.itda.domain.social.entity.ChatMessage;
import com.project.itda.domain.social.entity.ChatParticipant;
import com.project.itda.domain.social.enums.MessageType;
import com.project.itda.domain.social.repository.ChatMessageRepository;
import com.project.itda.domain.social.repository.ChatParticipantRepository;
import com.project.itda.domain.social.service.ChatMessageService;
import com.project.itda.domain.social.service.ChatRoomService;
import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatStompController {

    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;


    @MessageMapping("/chat/send/{roomId}")
    @Transactional
    public void sendMessage(@DestinationVariable Long roomId, Map<String, Object> message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String email = (String) message.get("email");
            User sender = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + email));

            // ✅ 발송자만 lastReadAt 업데이트
            chatRoomService.userJoined(roomId, email);

            String finalNickname = (sender.getNickname() != null && !sender.getNickname().trim().isEmpty())
                    ? sender.getNickname()
                    : sender.getUsername();

            String typeStr = message.getOrDefault("type", "TALK").toString().toUpperCase();
            MessageType messageType;

            try {
                messageType = MessageType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ 알 수 없는 메시지 타입: {}, TALK으로 대체", typeStr);
                messageType = MessageType.TALK;
            }

            Object rawMetadata = message.get("metadata");
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (rawMetadata instanceof Map)
                    ? (Map<String, Object>) rawMetadata
                    : null;

            // ✅ 1. 먼저 메시지 저장 (unreadCount 임시로 0)
            com.project.itda.domain.social.entity.ChatMessage savedMsg;

            if (messageType == MessageType.BILL || (metadata != null && !metadata.isEmpty())) {
                savedMsg = chatMessageService.saveMessageWithMetadata(
                        email, roomId, (String) message.get("content"),
                        messageType, metadata, 0
                );
            } else {
                savedMsg = chatMessageService.saveMessage(
                        email, roomId, (String) message.get("content"),
                        messageType, 0
                );
            }

            if (savedMsg == null || savedMsg.getId() == null) {
                log.error("❌ 메시지 저장 실패!");
                return;
            }

            // ✅ 2. DB 기반 미읽음 수 계산
            long totalUnread = chatParticipantRepository.countUnreadExcludingSender(
                    roomId, sender.getUserId(), savedMsg.getCreatedAt()
            );

            // ✅ 3. 활성 사용자 목록 (방어적 복사)
            Set<String> activeEmails = new HashSet<>(chatRoomService.getActiveUserEmails(roomId));

            // 🔍 디버깅: 활성 사용자 목록 출력
            log.info("🔍 [ACTIVE CHECK] roomId={}, 전체 활성 사용자: {}", roomId, activeEmails);

            activeEmails.remove(email); // 발송자 제외

            log.info("🔍 [ACTIVE CHECK] 발송자 제외 후: {}", activeEmails);

            // ✅ 4. 최종 unreadCount = DB 미읽음 - 활성 사용자
            int finalUnreadCount = (int) Math.max(0, totalUnread - activeEmails.size());

            log.info("📊 unreadCount 계산: totalUnread={}, activeUsers={}, final={}, 발송자={}",
                    totalUnread, activeEmails.size(), finalUnreadCount, email);

            // ✅ 5. DB에 저장
            savedMsg.setUnreadCount(finalUnreadCount);
            chatMessageRepository.save(savedMsg);

            // ✅ 6. 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("messageId", savedMsg.getId());
            response.put("senderId", sender.getUserId());
            response.put("senderNickname", finalNickname);
            response.put("content", message.get("content"));
            response.put("type", messageType.name());
            response.put("sentAt", savedMsg.getCreatedAt().toString());
            response.put("email", email);
            response.put("unreadCount", finalUnreadCount);

            if (metadata != null && !metadata.isEmpty()) {
                response.put("metadata", metadata);
            }

            messagingTemplate.convertAndSend("/topic/room/" + roomId, response);

            log.info("✅ 메시지 전송 완료 - messageId: {}, finalUnreadCount: {}", savedMsg.getId(), finalUnreadCount);

        } catch (Exception e) {
            log.error("❌ 메시지 전송 중 에러 발생", e);
            throw new RuntimeException("메시지 전송 실패: " + e.getMessage());
        }
    }

    @MessageMapping("/chat/join/{roomId}")
    @Transactional
    public void joinRoom(@DestinationVariable Long roomId, Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String email = payload.get("email");

        log.info("🔗 사용자 채팅방 입장: roomId={}, email={}", roomId, email);

        // ✅ 1. 활성 사용자로 등록 + lastReadAt 업데이트
        chatRoomService.userJoined(roomId, email);

        // ✅ 2. 세션에 저장
        Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("userEmail", email);
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        // ✅ 3. 최근 메시지들의 unreadCount 재계산 (JOIN 시 감소)
        List<ChatMessage> recentMessages = chatMessageRepository
                .findTop50ByChatRoomIdOrderByCreatedAtDesc(roomId);

        log.info("🔍 JOIN 처리: {} 개 메시지 재계산", recentMessages.size());

        for (ChatMessage msg : recentMessages) {
            // ✅ DB 쿼리로 정확한 미읽음 수 계산
            long actualUnread = chatParticipantRepository.countUnreadExcludingSender(
                    roomId, msg.getSender().getUserId(), msg.getCreatedAt()
            );

            int finalUnreadCount = (int) actualUnread;

            if (msg.getUnreadCount() != finalUnreadCount) {
                log.info("🔄 JOIN으로 인한 unreadCount 변경: {} -> {}", msg.getUnreadCount(), finalUnreadCount);
                msg.setUnreadCount(finalUnreadCount);
                chatMessageRepository.save(msg);

                Map<String, Object> updateSignal = new HashMap<>();
                updateSignal.put("type", "UNREAD_UPDATE");
                updateSignal.put("messageId", msg.getId());
                updateSignal.put("unreadCount", finalUnreadCount);
                updateSignal.put("email", email);

                messagingTemplate.convertAndSend("/topic/room/" + roomId, updateSignal);

                log.info("📤 UNREAD_UPDATE 전송 (JOIN): messageId={}, unreadCount={}",
                        msg.getId(), finalUnreadCount);
            }
        }

        // 🔍 디버깅: 활성 사용자 목록 확인
        Set<String> activeEmails = chatRoomService.getActiveUserEmails(roomId);
        log.info("🔍 [ACTIVE USERS] roomId={}, activeEmails={}", roomId, activeEmails);
    }

    @MessageMapping("/chat/read/{roomId}")
    @Transactional
    public void markAsRead(@DestinationVariable Long roomId, @Payload Map<String, String> payload) {
        String email = payload.get("email");
        log.info("📖 READ 신호 수신: roomId={}, email={}", roomId, email);

        // ✅ 1. 먼저 lastReadAt 업데이트
        chatRoomService.userJoined(roomId, email);

        // ✅ 2. 최근 메시지 가져오기
        List<ChatMessage> recentMessages = chatMessageRepository
                .findTop50ByChatRoomIdOrderByCreatedAtDesc(roomId);

        log.info("🔍 READ 처리 시작: {} 개 메시지 처리", recentMessages.size());

        // ✅ 3. 각 메시지의 unreadCount 재계산
        for (ChatMessage msg : recentMessages) {
            // ✅ DB 쿼리로 정확한 미읽음 수 계산 (이미 lastReadAt 반영됨!)
            long actualUnread = chatParticipantRepository.countUnreadExcludingSender(
                    roomId, msg.getSender().getUserId(), msg.getCreatedAt()
            );

            int finalUnreadCount = (int) actualUnread;

            log.info("📊 메시지 ID={}, 발송자={}, DB미읽음={}, 현재DB값={}, 계산값={}",
                    msg.getId(),
                    msg.getSender().getEmail(),
                    actualUnread,
                    msg.getUnreadCount(),
                    finalUnreadCount);

            if (msg.getUnreadCount() != finalUnreadCount) {
                log.info("🔄 업데이트 필요! {} -> {}", msg.getUnreadCount(), finalUnreadCount);
                msg.setUnreadCount(finalUnreadCount);
                chatMessageRepository.save(msg);

                Map<String, Object> updateSignal = new HashMap<>();
                updateSignal.put("type", "UNREAD_UPDATE");
                updateSignal.put("messageId", msg.getId());
                updateSignal.put("unreadCount", finalUnreadCount);
                updateSignal.put("email", email);

                messagingTemplate.convertAndSend("/topic/room/" + roomId, updateSignal);

                log.info("📤 UNREAD_UPDATE 전송: messageId={}, unreadCount={}",
                        msg.getId(), finalUnreadCount);
            }
        }

        log.info("✅ READ 처리 완료: roomId={}, email={}", roomId, email);
    }

    /**
     * 특정 메시지의 현재 unreadCount를 계산
     * = 메시지 생성 시각보다 lastReadAt이 이전인 참여자 수
     */
    public int calculateUnreadCount(Long roomId, Long messageId) {
        // 1. 메시지 조회
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다"));

        // 2. 이 메시지를 읽지 않은 참여자 수 계산
        long unreadCount = chatParticipantRepository.countByRoomIdAndLastReadAtBefore(
                roomId,
                message.getCreatedAt()
        );

        return (int) unreadCount;
    }
    @MessageMapping("/chat/leave/{roomId}")
    @Transactional
    public void leaveRoom(@DestinationVariable Long roomId, @Payload Map<String, String> payload) {
        String email = payload.get("email");
        log.info("👋 사용자 퇴장: roomId={}, email={}", roomId, email);

        // ✅ 1. activeUsers에서 제거
        chatRoomService.userLeft(roomId, email);

        // ✅ 2. 최근 메시지들의 unreadCount 재계산 (LEAVE 시 증가)
        List<ChatMessage> recentMessages = chatMessageRepository
                .findTop50ByChatRoomIdOrderByCreatedAtDesc(roomId);

        log.info("🔍 LEAVE 처리: {} 개 메시지 재계산", recentMessages.size());

        for (ChatMessage msg : recentMessages) {
            // ✅ DB 쿼리로 정확한 미읽음 수 계산
            long actualUnread = chatParticipantRepository.countUnreadExcludingSender(
                    roomId, msg.getSender().getUserId(), msg.getCreatedAt()
            );

            int finalUnreadCount = (int) actualUnread;

            if (msg.getUnreadCount() != finalUnreadCount) {
                log.info("🔄 LEAVE로 인한 unreadCount 변경: {} -> {}", msg.getUnreadCount(), finalUnreadCount);
                msg.setUnreadCount(finalUnreadCount);
                chatMessageRepository.save(msg);

                Map<String, Object> updateSignal = new HashMap<>();
                updateSignal.put("type", "UNREAD_UPDATE");
                updateSignal.put("messageId", msg.getId());
                updateSignal.put("unreadCount", finalUnreadCount);
                updateSignal.put("email", email);

                messagingTemplate.convertAndSend("/topic/room/" + roomId, updateSignal);

                log.info("📤 UNREAD_UPDATE 전송 (LEAVE): messageId={}, unreadCount={}",
                        msg.getId(), finalUnreadCount);
            }
        }
    }


}