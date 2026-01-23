package com.project.itda.domain.badge.seed;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.repository.BadgeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class BadgeSeeder {

    private final BadgeRepository badgeRepository;

    @PostConstruct
    @Transactional
    public void seedBadges() {
        // 1) DB에 이미 존재하는 badge_code 목록
        Set<String> existingCodes = new HashSet<>(badgeRepository.findAllBadgeCodes());

        // 2) 시드로 넣을 배지들 (형님 프로젝트의 실제 생성 로직으로 채우시면 됩니다)
        List<Badge> seedBadges = buildSeedBadges();

        // 3) Seeder 내부 중복도 방지하기 위한 Set
        Set<String> seenInSeeder = new HashSet<>();

        List<Badge> toInsert = new ArrayList<>();
        for (Badge b : seedBadges) {
            String code = b.getBadgeCode();

            // (A) seeder 내부 중복 방지
            if (!seenInSeeder.add(code)) {
                log.warn("[BadgeSeeder] duplicate badge_code in seeder definition: {}", code);
                continue;
            }

            // (B) DB에 이미 있으면 스킵
            if (existingCodes.contains(code)) {
                continue;
            }

            toInsert.add(b);
        }

        if (toInsert.isEmpty()) {
            log.info("[BadgeSeeder] no new badges to insert.");
            return;
        }

        badgeRepository.saveAll(toInsert);
        log.info("[BadgeSeeder] inserted {} badges.", toInsert.size());
    }

    /**
     * 여기에 형님이 만들던 125개 배지 정의를 구성해 넣으시면 됩니다.
     * 지금은 예시로만 몇 개 넣었습니다.
     */
    private List<Badge> buildSeedBadges() {
        List<Badge> list = new ArrayList<>();

        // 예시: TIME_MORNING_5를 실수로 두 번 넣으면,
        // 위의 seenInSeeder 로직이 두 번째 것을 자동으로 버립니다.
        // list.add(Badge.create("TIME_MORNING_5", ...));
        // list.add(Badge.create("TIME_MORNING_5", ...)); // <- 들어와도 안전

        return list;
    }
}
