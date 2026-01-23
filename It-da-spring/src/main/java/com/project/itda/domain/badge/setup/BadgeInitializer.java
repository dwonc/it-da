// src/main/java/com/project/itda/domain/badge/setup/BadgeInitializer.java
package com.project.itda.domain.badge.setup;

import com.project.itda.domain.badge.entity.Badge;
import com.project.itda.domain.badge.enums.BadgeCategory;
import com.project.itda.domain.badge.enums.BadgeConditionType;
import com.project.itda.domain.badge.enums.BadgeGrade;
import com.project.itda.domain.badge.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 애플리케이션 시작 시 125개 배지를 자동 생성합니다.
 * 이미 badges 테이블에 데이터가 있으면 스킵합니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BadgeInitializer implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (badgeRepository.count() > 0) {
            log.info("Badges already initialized. skipping...");
            return;
        }

        List<Badge> badges = new ArrayList<>(125);

        // 1) 참여 15개
        addParticipationBadges(badges);

        // 2) AI 10개
        addAiBadges(badges);

        // 3) 거리 8개
        addDistanceBadges(badges);

        // 4) 시간대 12개
        addTimeBadges(badges);

        // 5) 성향 18개
        addPersonalityBadges(badges);

        // 6) 카테고리 20개
        addCategoryBadges20(badges);

        // 7) 리뷰 12개
        addReviewBadges(badges);

        // 8) 소셜 10개
        addSocialBadges(badges);

        // 9) 주최 8개
        addHostBadges(badges);

        // 10) 특별 12개
        addSpecialBadges(badges);

        if (badges.size() != 125) {
            throw new IllegalStateException("BadgeInitializer must create exactly 125 badges. current=" + badges.size());
        }

        badgeRepository.saveAll(badges);
        log.info("Badges initialized. count={}", badges.size());
    }

    private void addParticipationBadges(List<Badge> badges) {
        // 참여 횟수 8개
        badges.add(create("participate_1", "첫 발걸음", "첫 모임 참여 완료", "🌟",
                BadgeGrade.COMMON, BadgeCategory.PARTICIPATION, BadgeConditionType.PARTICIPATION_COUNT, null, 1));
        badges.add(create("participate_3", "신입", "3회 모임 참여", "🎈",
                BadgeGrade.COMMON, BadgeCategory.PARTICIPATION, BadgeConditionType.PARTICIPATION_COUNT, null, 3));
        badges.add(create("participate_5", "입문자", "5회 모임 참여", "🎊",
                BadgeGrade.COMMON, BadgeCategory.PARTICIPATION, BadgeConditionType.PARTICIPATION_COUNT, null, 5));
        badges.add(create("participate_10", "열정러", "10회 모임 참여", "🔥",
                BadgeGrade.RARE, BadgeCategory.PARTICIPATION, BadgeConditionType.PARTICIPATION_COUNT, null, 10));
        badges.add(create("participate_20", "중급자", "20회 모임 참여", "🎖️",
                BadgeGrade.RARE, BadgeCategory.PARTICIPATION, BadgeConditionType.PARTICIPATION_COUNT, null, 20));
        badges.add(create("participate_30", "베테랑", "30회 모임 참여", "🏅",
                BadgeGrade.EPIC, BadgeCategory.PARTICIPATION, BadgeConditionType.PARTICIPATION_COUNT, null, 30));
        badges.add(create("participate_50", "마스터", "50회 모임 참여", "👑",
                BadgeGrade.EPIC, BadgeCategory.PARTICIPATION, BadgeConditionType.PARTICIPATION_COUNT, null, 50));
        badges.add(create("participate_100", "레전드", "100회 모임 참여", "⭐",
                BadgeGrade.LEGENDARY, BadgeCategory.PARTICIPATION, BadgeConditionType.PARTICIPATION_COUNT, null, 100));

        // 연속 7개(15 맞추기: 8 + 7 = 15)
        badges.add(create("consecutive_days_3", "3일 연속", "3일 연속 참여", "📆",
                BadgeGrade.COMMON, BadgeCategory.PARTICIPATION, BadgeConditionType.CONSECUTIVE_DAYS, null, 3));
        badges.add(create("consecutive_days_7", "매일 매일", "7일 연속 참여", "📆",
                BadgeGrade.RARE, BadgeCategory.PARTICIPATION, BadgeConditionType.CONSECUTIVE_DAYS, null, 7));
        badges.add(create("consecutive_days_14", "2주 챌린지", "14일 연속 참여", "📆",
                BadgeGrade.EPIC, BadgeCategory.PARTICIPATION, BadgeConditionType.CONSECUTIVE_DAYS, null, 14));
        badges.add(create("consecutive_days_30", "한 달 챌린지", "30일 연속 참여", "🌙",
                BadgeGrade.EPIC, BadgeCategory.PARTICIPATION, BadgeConditionType.CONSECUTIVE_DAYS, null, 30));
        badges.add(create("consecutive_days_100", "백일 장인", "100일 연속 참여", "🏆",
                BadgeGrade.LEGENDARY, BadgeCategory.PARTICIPATION, BadgeConditionType.CONSECUTIVE_DAYS, null, 100));

        badges.add(create("consecutive_weeks_4", "4주 연속", "4주 연속 참여", "💪",
                BadgeGrade.RARE, BadgeCategory.PARTICIPATION, BadgeConditionType.CONSECUTIVE_WEEKS, null, 4));
        badges.add(create("consecutive_weeks_12", "분기 러너", "12주 연속 참여", "💪",
                BadgeGrade.LEGENDARY, BadgeCategory.PARTICIPATION, BadgeConditionType.CONSECUTIVE_WEEKS, null, 12));
    }

    private void addAiBadges(List<Badge> badges) {
        badges.add(create("ai_first", "AI 믿고 가기", "AI 추천 모임 첫 참여", "🎯",
                BadgeGrade.COMMON, BadgeCategory.AI, BadgeConditionType.AI_RECOMMENDATION_COUNT, null, 1));
        badges.add(create("ai_recommend_5", "취향 저격", "AI 추천 5회 참여", "💯",
                BadgeGrade.RARE, BadgeCategory.AI, BadgeConditionType.AI_RECOMMENDATION_COUNT, null, 5));
        badges.add(create("ai_recommend_10", "AI 마니아", "AI 추천 10회 참여", "🎲",
                BadgeGrade.EPIC, BadgeCategory.AI, BadgeConditionType.AI_RECOMMENDATION_COUNT, null, 10));
        badges.add(create("ai_recommend_30", "AI 중독", "AI 추천 30회 참여", "🤖",
                BadgeGrade.LEGENDARY, BadgeCategory.AI, BadgeConditionType.AI_RECOMMENDATION_COUNT, null, 30));

        badges.add(create("ai_satisfaction_5", "만족도 예측왕", "예측 만족도 4.5+ 모임 5회", "✨",
                BadgeGrade.EPIC, BadgeCategory.AI, BadgeConditionType.HIGH_SATISFACTION_COUNT, null, 5));
        badges.add(create("ai_satisfaction_20", "만족도 장인", "예측 만족도 4.5+ 모임 20회", "✨",
                BadgeGrade.LEGENDARY, BadgeCategory.AI, BadgeConditionType.HIGH_SATISFACTION_COUNT, null, 20));

        badges.add(create("ai_match_80", "매칭 감각", "매칭률 80% 이상", "🤝",
                BadgeGrade.RARE, BadgeCategory.AI, BadgeConditionType.AI_MATCH_RATE, null, 80));
        badges.add(create("ai_match_95", "완벽 매칭", "매칭률 95% 이상", "🤝",
                BadgeGrade.LEGENDARY, BadgeCategory.AI, BadgeConditionType.AI_MATCH_RATE, null, 95));

        badges.add(create("ai_accuracy_70", "예측 감", "예측 정확도 70%+", "📊",
                BadgeGrade.EPIC, BadgeCategory.AI, BadgeConditionType.PREDICTION_ACCURACY, null, 70));
        badges.add(create("ai_accuracy_90", "데이터 드리븐", "예측 정확도 90%+", "📊",
                BadgeGrade.LEGENDARY, BadgeCategory.AI, BadgeConditionType.PREDICTION_ACCURACY, null, 90));
    }

    private void addDistanceBadges(List<Badge> badges) {
        badges.add(create("distance_1km_10", "집근처 러너", "1km 이내 모임 10회", "🏠",
                BadgeGrade.COMMON, BadgeCategory.DISTANCE, BadgeConditionType.DISTANCE_RANGE_COUNT, "0-1", 10));
        badges.add(create("distance_3km_20", "동네 탐험가", "3km 이내 모임 20회", "🚶",
                BadgeGrade.COMMON, BadgeCategory.DISTANCE, BadgeConditionType.DISTANCE_RANGE_COUNT, "0-3", 20));
        badges.add(create("distance_5km_30", "활동 반경 확장", "5km 이내 모임 30회", "🧭",
                BadgeGrade.RARE, BadgeCategory.DISTANCE, BadgeConditionType.DISTANCE_RANGE_COUNT, "0-5", 30));
        badges.add(create("distance_10km_50", "원정대", "10km 이내 모임 50회", "🗺️",
                BadgeGrade.EPIC, BadgeCategory.DISTANCE, BadgeConditionType.DISTANCE_RANGE_COUNT, "0-10", 50));

        badges.add(create("distance_total_50", "누적 50km", "누적 거리 50km", "🧩",
                BadgeGrade.RARE, BadgeCategory.DISTANCE, BadgeConditionType.TOTAL_DISTANCE, null, 50));
        badges.add(create("distance_total_200", "누적 200km", "누적 거리 200km", "🧩",
                BadgeGrade.EPIC, BadgeCategory.DISTANCE, BadgeConditionType.TOTAL_DISTANCE, null, 200));
        badges.add(create("distance_total_1000", "지도 마스터", "누적 거리 1000km", "🗺️",
                BadgeGrade.LEGENDARY, BadgeCategory.DISTANCE, BadgeConditionType.TOTAL_DISTANCE, null, 1000));

        badges.add(create("region_10", "전국구", "10개 이상 지역 방문", "🌏",
                BadgeGrade.LEGENDARY, BadgeCategory.DISTANCE, BadgeConditionType.REGION_COUNT, null, 10));
    }

    private void addTimeBadges(List<Badge> badges) {
        // 4구간 * 3단계 = 12개
        addTimeSlotTier(badges, "morning", "아침", "🌅");
        addTimeSlotTier(badges, "afternoon", "오후", "☀️");
        addTimeSlotTier(badges, "evening", "저녁", "🌙");
        addTimeSlotTier(badges, "night", "밤", "🦉");
    }

    private void addTimeSlotTier(List<Badge> badges, String param, String label, String icon) {
        badges.add(create("time_" + param + "_5", label + " 5회", label + " 모임 5회", icon,
                BadgeGrade.COMMON, BadgeCategory.TIME, BadgeConditionType.TIME_SLOT_COUNT, param, 5));
        badges.add(create("time_" + param + "_15", label + " 15회", label + " 모임 15회", icon,
                BadgeGrade.RARE, BadgeCategory.TIME, BadgeConditionType.TIME_SLOT_COUNT, param, 15));
        badges.add(create("time_" + param + "_40", label + " 40회", label + " 모임 40회", icon,
                BadgeGrade.EPIC, BadgeCategory.TIME, BadgeConditionType.TIME_SLOT_COUNT, param, 40));
    }

    private void addPersonalityBadges(List<Badge> badges) {
        // 6성향 * 3단계 = 18개
        String[] types = {"calm", "active", "planner", "free", "talkative", "quiet"};
        String[] names = {"차분", "활동", "계획", "자유", "수다", "조용"};
        String[] icons = {"🧘", "🏃", "🗓️", "🕊️", "💬", "🌿"};

        for (int i = 0; i < types.length; i++) {
            String param = types[i];
            String name = names[i];
            String icon = icons[i];

            badges.add(create("personality_" + param + "_5", name + " 매칭 5회", name + " 성향 매칭 5회", icon,
                    BadgeGrade.COMMON, BadgeCategory.PERSONALITY, BadgeConditionType.PERSONALITY_MATCH_COUNT, param, 5));
            badges.add(create("personality_" + param + "_20", name + " 매칭 20회", name + " 성향 매칭 20회", icon,
                    BadgeGrade.RARE, BadgeCategory.PERSONALITY, BadgeConditionType.PERSONALITY_MATCH_COUNT, param, 20));
            badges.add(create("personality_" + param + "_60", name + " 매칭 60회", name + " 성향 매칭 60회", icon,
                    BadgeGrade.EPIC, BadgeCategory.PERSONALITY, BadgeConditionType.PERSONALITY_MATCH_COUNT, param, 60));
        }
    }

    private void addCategoryBadges20(List<Badge> badges) {
        // 7카테고리 중 6개는 3단계(18개), 마지막 1개는 2단계(2개) => 총 20개
        String[] categories = {"sports", "food", "cafe", "culture", "study", "hobby", "social"};
        String[] names = {"스포츠", "맛집", "카페", "문화예술", "스터디", "취미활동", "소셜"};
        String[][] icons = {
                {"🏃", "⚽", "🏅"},
                {"🍽️", "🍴", "👨‍🍳"},
                {"☕", "🧋", "🥤"},
                {"🎨", "🎭", "🖼️"},
                {"📖", "📚", "🎓"},
                {"🎯", "🎪", "🌟"},
                {"💬", "🎉", "🌐"}
        };

        for (int i = 0; i < categories.length; i++) {
            String param = names[i]; // 실제 프로젝트에서 categoryParam을 무엇으로 쓸지 확정되면 그에 맞춰 교체
            String codePrefix = "cat_" + categories[i];
            String display = names[i];

            // 6개는 3단계
            if (i < 6) {
                badges.add(create(codePrefix + "_3", display + " 입문", display + " 3회", icons[i][0],
                        BadgeGrade.COMMON, BadgeCategory.CATEGORY, BadgeConditionType.CATEGORY_COUNT, param, 3));
                badges.add(create(codePrefix + "_10", display + " 러버", display + " 10회", icons[i][1],
                        BadgeGrade.RARE, BadgeCategory.CATEGORY, BadgeConditionType.CATEGORY_COUNT, param, 10));
                badges.add(create(codePrefix + "_30", display + " 마스터", display + " 30회", icons[i][2],
                        BadgeGrade.EPIC, BadgeCategory.CATEGORY, BadgeConditionType.CATEGORY_COUNT, param, 30));
            } else {
                // 마지막 1개는 2단계만
                badges.add(create(codePrefix + "_3", display + " 입문", display + " 3회", icons[i][0],
                        BadgeGrade.COMMON, BadgeCategory.CATEGORY, BadgeConditionType.CATEGORY_COUNT, param, 3));
                badges.add(create(codePrefix + "_10", display + " 러버", display + " 10회", icons[i][1],
                        BadgeGrade.RARE, BadgeCategory.CATEGORY, BadgeConditionType.CATEGORY_COUNT, param, 10));
            }
        }
    }

    private void addReviewBadges(List<Badge> badges) {
        // 리뷰 12개
        badges.add(create("review_1", "첫 후기", "후기 1개 작성", "✍️",
                BadgeGrade.COMMON, BadgeCategory.REVIEW, BadgeConditionType.REVIEW_COUNT, null, 1));
        badges.add(create("review_5", "후기 작성자", "후기 5개 작성", "📝",
                BadgeGrade.COMMON, BadgeCategory.REVIEW, BadgeConditionType.REVIEW_COUNT, null, 5));
        badges.add(create("review_10", "후기왕", "후기 10개 작성", "✨",
                BadgeGrade.RARE, BadgeCategory.REVIEW, BadgeConditionType.REVIEW_COUNT, null, 10));
        badges.add(create("review_30", "후기 마스터", "후기 30개 작성", "📚",
                BadgeGrade.EPIC, BadgeCategory.REVIEW, BadgeConditionType.REVIEW_COUNT, null, 30));

        badges.add(create("positive_review_5", "긍정 에너지", "긍정 후기 5개", "😊",
                BadgeGrade.RARE, BadgeCategory.REVIEW, BadgeConditionType.POSITIVE_REVIEW_COUNT, null, 5));
        badges.add(create("positive_review_15", "포지티브", "긍정 후기 15개", "🌟",
                BadgeGrade.EPIC, BadgeCategory.REVIEW, BadgeConditionType.POSITIVE_REVIEW_COUNT, null, 15));

        badges.add(create("rating_45", "별점 4.5+", "평균 평점 4.5+", "⭐",
                BadgeGrade.RARE, BadgeCategory.REVIEW, BadgeConditionType.AVERAGE_RATING, null, 45));
        badges.add(create("rating_48", "별점 4.8+", "평균 평점 4.8+", "💯",
                BadgeGrade.EPIC, BadgeCategory.REVIEW, BadgeConditionType.AVERAGE_RATING, null, 48));
        badges.add(create("rating_49", "별점 4.9+", "평균 평점 4.9+", "🏆",
                BadgeGrade.LEGENDARY, BadgeCategory.REVIEW, BadgeConditionType.AVERAGE_RATING, null, 49));

        badges.add(create("positive_rate_80", "좋은 사람", "긍정률 80%+", "☀️",
                BadgeGrade.RARE, BadgeCategory.REVIEW, BadgeConditionType.POSITIVE_RATE, null, 80));
        badges.add(create("positive_rate_90", "햇살", "긍정률 90%+", "☀️",
                BadgeGrade.EPIC, BadgeCategory.REVIEW, BadgeConditionType.POSITIVE_RATE, null, 90));
        badges.add(create("positive_rate_95", "햇살같은 사람", "긍정률 95%+", "☀️",
                BadgeGrade.LEGENDARY, BadgeCategory.REVIEW, BadgeConditionType.POSITIVE_RATE, null, 95));
    }

    private void addSocialBadges(List<Badge> badges) {
        // 소셜 10개
        badges.add(create("friend_1", "친구 만들기", "첫 친구 추가", "🤝",
                BadgeGrade.COMMON, BadgeCategory.SOCIAL, BadgeConditionType.FRIEND_COUNT, null, 1));
        badges.add(create("friend_5", "친구 5명", "친구 5명 추가", "👫",
                BadgeGrade.COMMON, BadgeCategory.SOCIAL, BadgeConditionType.FRIEND_COUNT, null, 5));
        badges.add(create("friend_10", "친구 10명", "친구 10명 추가", "👬",
                BadgeGrade.RARE, BadgeCategory.SOCIAL, BadgeConditionType.FRIEND_COUNT, null, 10));
        badges.add(create("friend_30", "인싸", "친구 30명 추가", "🌐",
                BadgeGrade.EPIC, BadgeCategory.SOCIAL, BadgeConditionType.FRIEND_COUNT, null, 30));
        badges.add(create("friend_100", "커뮤니티 리더", "친구 100명", "👨‍👩‍👧‍👦",
                BadgeGrade.LEGENDARY, BadgeCategory.SOCIAL, BadgeConditionType.FRIEND_COUNT, null, 100));

        badges.add(create("chat_50", "말문 트임", "채팅 50회", "💬",
                BadgeGrade.COMMON, BadgeCategory.SOCIAL, BadgeConditionType.CHAT_COUNT, null, 50));
        badges.add(create("chat_100", "수다쟁이", "채팅 100회", "💬",
                BadgeGrade.RARE, BadgeCategory.SOCIAL, BadgeConditionType.CHAT_COUNT, null, 100));
        badges.add(create("chat_300", "채팅 마스터", "채팅 300회", "📱",
                BadgeGrade.EPIC, BadgeCategory.SOCIAL, BadgeConditionType.CHAT_COUNT, null, 300));
        badges.add(create("chat_500", "채팅왕", "채팅 500회", "📱",
                BadgeGrade.EPIC, BadgeCategory.SOCIAL, BadgeConditionType.CHAT_COUNT, null, 500));
        badges.add(create("chat_1000", "메신저", "채팅 1000회", "📱",
                BadgeGrade.LEGENDARY, BadgeCategory.SOCIAL, BadgeConditionType.CHAT_COUNT, null, 1000));
    }

    private void addHostBadges(List<Badge> badges) {
        // 주최 8개
        badges.add(create("host_1", "첫 주최", "첫 모임 주최", "🎪",
                BadgeGrade.RARE, BadgeCategory.HOST, BadgeConditionType.HOST_COUNT, null, 1));
        badges.add(create("host_3", "주최 3회", "3회 모임 주최", "🎪",
                BadgeGrade.RARE, BadgeCategory.HOST, BadgeConditionType.HOST_COUNT, null, 3));
        badges.add(create("host_5", "오거나이저", "5회 모임 주최", "🎯",
                BadgeGrade.RARE, BadgeCategory.HOST, BadgeConditionType.HOST_COUNT, null, 5));
        badges.add(create("host_10", "기획자", "10회 모임 주최", "🎭",
                BadgeGrade.EPIC, BadgeCategory.HOST, BadgeConditionType.HOST_COUNT, null, 10));
        badges.add(create("host_15", "프로 기획자", "15회 모임 주최", "🎭",
                BadgeGrade.EPIC, BadgeCategory.HOST, BadgeConditionType.HOST_COUNT, null, 15));
        badges.add(create("host_30", "이벤트 마스터", "30회 모임 주최", "👨‍💼",
                BadgeGrade.LEGENDARY, BadgeCategory.HOST, BadgeConditionType.HOST_COUNT, null, 30));

        // 호스트 관련(분위기 매칭/특별 날짜 등으로 확장 가능)
        badges.add(create("host_vibe_10", "분위기 메이커", "분위기 매칭 10회", "🎉",
                BadgeGrade.EPIC, BadgeCategory.HOST, BadgeConditionType.VIBE_MATCH_COUNT, "host", 10));
        badges.add(create("host_vibe_30", "무드 장인", "분위기 매칭 30회", "🎉",
                BadgeGrade.LEGENDARY, BadgeCategory.HOST, BadgeConditionType.VIBE_MATCH_COUNT, "host", 30));
    }

    private void addSpecialBadges(List<Badge> badges) {
        // 특별 12개: 시즌 4종 * 3단계 = 12
        addSeasonTier(badges, "spring", "봄", "🌸");
        addSeasonTier(badges, "summer", "여름", "🌻");
        addSeasonTier(badges, "autumn", "가을", "🍁");
        addSeasonTier(badges, "winter", "겨울", "⛄");
    }

    private void addSeasonTier(List<Badge> badges, String season, String name, String icon) {
        badges.add(create("season_" + season + "_1", name + " 1회", name + " 시즌 참여 1회", icon,
                BadgeGrade.COMMON, BadgeCategory.SPECIAL, BadgeConditionType.SEASON_PARTICIPATION, season, 1));
        badges.add(create("season_" + season + "_5", name + " 5회", name + " 시즌 참여 5회", icon,
                BadgeGrade.RARE, BadgeCategory.SPECIAL, BadgeConditionType.SEASON_PARTICIPATION, season, 5));
        badges.add(create("season_" + season + "_15", name + " 15회", name + " 시즌 참여 15회", icon,
                BadgeGrade.EPIC, BadgeCategory.SPECIAL, BadgeConditionType.SEASON_PARTICIPATION, season, 15));
    }

    private Badge create(String code, String name, String description, String icon,
                         BadgeGrade grade, BadgeCategory category,
                         BadgeConditionType conditionType, String conditionParam, int targetValue) {
        return Badge.builder()
                .badgeCode(code)
                .badgeName(name)
                .description(description)
                .icon(icon)
                .grade(grade)
                .category(category)
                .conditionType(conditionType)
                .conditionParam(conditionParam)
                .targetValue(targetValue)
                .build();
    }
}
