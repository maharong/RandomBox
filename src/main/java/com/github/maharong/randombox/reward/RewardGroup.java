package com.github.maharong.randombox.reward;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 보상 그룹 클래스입니다.
 * 그룹 단위로 확률을 적용하며, 하나의 그룹이 여러 보상을 포함할 수 있습니다.
 *
 * @param chance 확률 (%)
 * @param rewards 실제 보상 리스트
 */
public record RewardGroup(double chance, List<Reward> rewards, List<Reward> defaultRewards) {

    /**
     * Yaml에서 RewardGroup을 생성합니다.
     * @param section Map<String, Object> 형식의 raw map
     * @return RewardGroup 인스턴스
     */
    @SuppressWarnings("unchecked")
    public static RewardGroup fromMap(Map<String, Object> section) {
        double chance = ((Number) section.getOrDefault("chance", 0)).doubleValue();
        // rewards 섹션 null 방지
        List<Map<String, Object>> rawList = (List<Map<String, Object>>) section.get("rewards");
        if (rawList == null) {
            throw new IllegalArgumentException("'rewards' 항목이 존재하지 않거나 null입니다.");
        }

        List<Reward> rewards = new ArrayList<>();
        for (Map<String, Object> map : rawList) {
            try {
                rewards.add(RewardDeserializer.fromMap(map));
            } catch (Exception e) {
                throw new IllegalArgumentException("RewardGroup 내 보상 파싱 중 오류 발생: " + map + " → " + e.getMessage(), e);
            }
        }

        // default.rewards 는 선택적이므로 널 허용, 하지만 파싱 시 안전 처리
        List<Reward> defaultRewards = new ArrayList<>();
        if (section.containsKey("default")) {
            Object defaultObj = section.get("default");
            if (defaultObj instanceof Map<?, ?> defaultMap) {
                List<Map<String, Object>> rawDefault = (List<Map<String, Object>>) defaultMap.get("rewards");
                if (rawDefault != null) {
                    for (Map<String, Object> map : rawDefault) {
                        try {
                            defaultRewards.add(RewardDeserializer.fromMap(map));
                        } catch (Exception e) {
                            throw new IllegalArgumentException("RewardGroup의 default 보상 파싱 중 오류 발생: " + map + " → " + e.getMessage(), e);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("'default' 항목은 Map 형식이어야 합니다.");
            }
        }

        return new RewardGroup(chance, rewards, defaultRewards);
    }

    public void giveAll(Player player) {
        for (Reward reward : rewards) {
            reward.give(player);
        }
    }
}
