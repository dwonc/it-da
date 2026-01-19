package com.project.itda.domain.social.service;

import com.project.itda.domain.social.dto.response.ChatMessageResponse;
import com.project.itda.domain.social.entity.ChatMessage;
import com.project.itda.domain.social.entity.ChatRoom;
import com.project.itda.domain.social.enums.MessageType;
import com.project.itda.domain.social.repository.ChatMessageRepository;
import com.project.itda.domain.social.repository.ChatParticipantRepository;
import com.project.itda.domain.social.repository.ChatRoomRepository;
import com.project.itda.domain.user.entity.User;
import com.project.itda.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository; // 유저 조회를 위해 추가
    private final ChatRoomRepository chatRoomRepository; // 방 조회를 위해 추가
    private final ChatParticipantRepository chatParticipantRepository;

    public List<ChatMessage> getMessagesByRoom(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
    }

    @Transactional
    public void saveMessage(String email, Long chatRoomId, String content) {
        // 1. 보낸 사람 조회
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없음"));

        // 2. 채팅방 조회
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없음"));

        // 3. 메시지 엔티티 생성 및 저장
        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .chatRoom(room)
                .content(content)
                .type(MessageType.TEXT) // 기본 타입 설정
                .build();

        chatMessageRepository.save(message);
    }
    public List<ChatMessageResponse> getChatMessages(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
        long totalParticipants = chatParticipantRepository.countByChatRoomId(roomId);

        return messages.stream().map(msg -> {
            // ✅ 보낸 사람의 닉네임이 없으면 username을 사용하도록 확정
            String nickname = msg.getSender().getNickname();
            String finalName = (nickname != null && !nickname.trim().isEmpty())
                    ? nickname : msg.getSender().getUsername();

            long readCount = chatParticipantRepository.countByChatRoomIdAndLastReadAtAfter(roomId, msg.getCreatedAt());
            int unreadCount = (int) (totalParticipants - readCount - 1);

            return ChatMessageResponse.builder()
                    .messageId(msg.getId())
                    .senderId(msg.getSender().getUserId())
                    .senderNickname(finalName) // 💡 "익" 대신 실제 이름 주입
                    .content(msg.getContent())
                    .type(msg.getType())
                    .sentAt(msg.getCreatedAt())
                    .unreadCount(Math.max(0, unreadCount))
                    .build();
        }).collect(Collectors.toList());
    }
    @Transactional
    public void updateLastReadAt(Long roomId, String email) {
        // 1. 참여자 정보 조회
        com.project.itda.domain.social.entity.ChatParticipant participant =
                chatParticipantRepository.findByChatRoomIdAndUserEmail(roomId, email)
                        .orElseThrow(() -> new RuntimeException("참여자가 아닙니다."));

        // 2. 마지막 읽은 시간 갱신 (이미 ChatParticipant 엔티티에 메서드 추가됨)
        participant.updateLastReadAt(java.time.LocalDateTime.now());
    }
}