package com.project.itda.domain.social.repository;

import com.project.itda.domain.social.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByUserUserId(Long userId);
    List<ChatParticipant> findByChatRoomId(Long chatRoomId);
    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.chatRoom WHERE cp.user.email = :email")
    List<ChatParticipant> findByUserEmail(@Param("email") String email);
    // 특정 방의 총 참여자 수
    long countByChatRoomId(Long chatRoomId);

    // 메시지 생성 시간 이후에 방을 읽은 사람 수 계산
    long countByChatRoomIdAndLastReadAtAfter(Long chatRoomId, LocalDateTime messageTime);

    // 유저와 방 ID로 참여자 정보 찾기
    Optional<ChatParticipant> findByChatRoomIdAndUserEmail(Long chatRoomId, String email);

    @Query("SELECT cp.lastReadAt FROM ChatParticipant cp WHERE cp.chatRoom.id = :roomId")
    List<LocalDateTime> findAllLastReadAtByRoomId(@Param("roomId") Long roomId);

    /**
     * ✅ 특정 메시지를 읽지 않은 참여자 수 계산 (발송자 제외)
     * 메시지 생성 시각보다 lastReadAt이 이전이거나 null인 참여자
     */
    @Query("""
           select count(p)
           from ChatParticipant p
           where p.chatRoom.id = :roomId
           and p.user.userId <> :senderId
           and (p.lastReadAt is null or p.lastReadAt < :createdAt)
            """)
    long countUnreadExcludingSender(Long roomId, Long senderId, LocalDateTime createdAt);


    /**
     * ✅ 실제로 읽지 않은 사람 수
     * = 메시지 생성 시각 이후에 lastReadAt을 업데이트하지 않은 사람
     * = 발송자 제외, 메시지 생성 이후 READ 하지 않은 사람
     */
    @Query("SELECT COUNT(p) FROM ChatParticipant p " +
            "WHERE p.chatRoom.id = :roomId " +
            "AND p.user.userId != :senderId " +
            "AND (p.lastReadAt IS NULL OR p.lastReadAt < :messageCreatedAt)")
    long countActualUnread(
            @Param("roomId") Long roomId,
            @Param("senderId") Long senderId,
            @Param("messageCreatedAt") LocalDateTime messageCreatedAt
    );


    // ✅ 새로운 메서드: 현재 활성 사용자도 제외
    @Query("SELECT COUNT(p) FROM ChatParticipant p " +
            "WHERE p.chatRoom.id = :roomId " +
            "AND p.user.userId != :senderId " +
            "AND (p.lastReadAt IS NULL OR p.lastReadAt < :messageCreatedAt) " +
            "AND (p.lastReadAt IS NULL OR p.lastReadAt < :now)")  // ✅ 최근 활동 기준
    long countActualUnread(
            @Param("roomId") Long roomId,
            @Param("senderId") Long senderId,
            @Param("messageCreatedAt") LocalDateTime messageCreatedAt,
            @Param("now") LocalDateTime now
    );

    /**
     * ✅ 특정 시각 이전에 마지막으로 읽은 참여자 수 (= 해당 메시지를 읽지 않은 사람 수)
     */
    @Query("SELECT COUNT(cp) FROM ChatParticipant cp " +
            "WHERE cp.chatRoom.id = :roomId " +
            "AND (cp.lastReadAt IS NULL OR cp.lastReadAt < :messageCreatedAt)")
    long countByRoomIdAndLastReadAtBefore(
            @Param("roomId") Long roomId,
            @Param("messageCreatedAt") LocalDateTime messageCreatedAt
    );


    @Query("SELECT COUNT(cp) > 0 FROM ChatParticipant cp WHERE cp.chatRoom.id = :chatRoomId AND cp.user.userId = :userId")
    boolean existsByChatRoomIdAndUserId(@Param("chatRoomId") Long chatRoomId, @Param("userId") Long userId);
}