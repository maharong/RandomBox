package com.github.maharong.randombox.reward;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * 리워드 그룹 리스트에서 확률 기반으로 하나를 추첨하는 유틸 클래스
 */
public class RewardSelector {

    /**
     * 전체 그룹 중 하나를 확률 기반으로 추첨합니다.
     *
     * @param groups 리워드 그룹 리스트
     * @param player 대상 플레이어
     * @return 추첨된 그룹 (없으면 null)
     */
    public static RewardGroup draw(List<RewardGroup> groups, Player player) {
        double roll = Math.random() * 100;
        double cumulative = 0.0;

        for (RewardGroup group : groups) {
            cumulative += group.chance();
            if (roll <= cumulative) {
                return group;
            }
        }

        // 꽝일 경우
        return null;
    }
}
