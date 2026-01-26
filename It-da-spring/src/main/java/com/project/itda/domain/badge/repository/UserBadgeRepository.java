// src/main/java/com/project/itda/domain/badge/repository/UserBadgeRepository.java
package com.project.itda.domain.badge.repository;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.entity.UserBadge;
import com.project.itda.domain.badge.enums.BadgeGrade;
import com.project.itda.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserId(Long userId);

    List<UserBadge> findByUserIdAndUnlocked(Long userId, Boolean unlocked);



    Optional<UserBadge> findByUserIdAndBadge_BadgeId(Long userId, Long badgeId);

    Optional<UserBadge> findByUserIdAndBadge_BadgeCode(Long userId, String badgeCode);

    @Query("""
            SELECT ub 
            FROM UserBadge ub 
            WHERE ub.userId = :userId 
              AND ub.unlocked = false 
              AND ub.progress >= ub.badge.targetValue
            """)
    List<UserBadge> findUnlockableBadges(@Param("userId") Long userId);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.userId = :userId AND ub.unlocked = true")
    Long countUnlockedBadges(@Param("userId") Long userId);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.userId = :userId AND ub.unlocked = true AND ub.badge.grade = :grade")
    Long countUnlockedBadgesByGrade(@Param("userId") Long userId, @Param("grade") BadgeGrade grade);

    boolean existsByUserIdAndBadge(Long userId, Badge badge);
}
