// src/main/java/com/project/itda/domain/user/repository/MeetingParticipationRepository.java
package com.project.itda.domain.user.repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.project.itda.domain.meeting.entity.MeetingParticipation;
import com.project.itda.domain.user.enums.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingParticipationRepository extends JpaRepository<MeetingParticipation, Long> {

    // 사용자별 상태별 참여 목록
    List<MeetingParticipation> findByUserUserIdAndStatusOrderByIdDesc(
            Long userId,
            ParticipationStatus status
    );

    // 특정 사용자의 특정 모임 참여 기록
    Optional<MeetingParticipation> findByUserUserIdAndMeetingMeetingId(
            Long userId,
            Long meetingId
    );

    // ============ ⭐ 배지용 메서드 추가 ============

    /**
     * 완료된 참여 횟수 (배지용)
     */
    @Query("SELECT COUNT(mp) FROM MeetingParticipation mp WHERE mp.user.userId = :userId AND mp.status = 'COMPLETED'")
    int countCompletedByUserId(@Param("userId") Long userId);

    /**
     * 카테고리별 완료된 참여 횟수 (배지용)
     */
    @Query("""
            SELECT COUNT(mp) 
            FROM MeetingParticipation mp 
            WHERE mp.user.userId = :userId 
              AND mp.status = 'COMPLETED'
              AND mp.meeting.category = :category
            """)
    int countCompletedByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    /**
     * 참여 완료한 고유 카테고리 수 (배지용)
     */
    @Query("""

            SELECT COUNT(DISTINCT mp.meeting.category) 
        FROM MeetingParticipation mp 
        WHERE mp.user.userId = :userId 
          AND mp.status = 'COMPLETED'
        """)
    int countDistinctCategoryByUserId(@Param("userId") Long userId);

 }