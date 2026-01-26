// src/main/java/com/project/itda/domain/social/repository/ChatMessageRepository.java
package com.project.itda.domain.social.repository;

import com.project.itda.domain.social.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    // ⭐ 배지용: 사용자의 채팅 메시지 수
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.sender.userId = :userId")
    int countBySenderUserId(@Param("userId") Long userId);
}