package com.project.itda.domain.badge.repository;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.enums.BadgeConditionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    /**
     * 배지 코드로 단건 조회
     */
    Optional<Badge> findByBadgeCode(String badgeCode);

    /**
     * 조건 타입으로 배지 목록 조회
     */
    List<Badge> findByConditionType(BadgeConditionType conditionType);

    /**
     * Seeder(초기화) 중복 방지용: DB에 존재하는 badge_code 목록 조회
     */
    @Query("select b.badgeCode from Badge b")
    List<String> findAllBadgeCodes();
}
