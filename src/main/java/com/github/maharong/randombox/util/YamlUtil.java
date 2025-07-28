package com.github.maharong.randombox.util;

import com.github.maharong.randombox.reward.Reward;
import com.github.maharong.randombox.reward.RewardGroup;
import com.github.maharong.randombox.reward.RewardDeserializer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlUtil {

    /**
     * rewards 섹션(List<Map>)을 RewardGroup 리스트로 파싱합니다.
     *
     * @param rawList YML의 rewards 또는 default.rewards 리스트
     * @return RewardGroup 목록
     */
    public static List<RewardGroup> parseRewardGroups(List<Map<String, Object>> rawList) {
        List<RewardGroup> result = new ArrayList<>();
        if (rawList == null) {
            Bukkit.getLogger().warning("[RandomBox] parseRewardGroups: rawList가 null입니다.");
            return result;
        }

        for (Object obj : rawList) {
            if (obj instanceof Map<?, ?> map) {
                try {
                    result.add(RewardGroup.fromMap((Map<String, Object>) map));
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[RandomBox] RewardGroup 파싱 중 오류 발생: " + e.getMessage());
                    Bukkit.getLogger().severe("문제 객체: " + map);
                }
            } else {
                Bukkit.getLogger().warning("[RandomBox] 예상치 못한 객체 타입: " + obj.getClass().getName());
            }
        }

        return result;
    }

    public static List<Reward> parseRewards(List<Map<String, Object>> list) {
        List<Reward> rewards = new ArrayList<>();
        for (Map<String, Object> map : list) {
            try {
                rewards.add(RewardDeserializer.fromMap(map));
            } catch (Exception e) {
                Bukkit.getLogger().severe("[RandomBox] Reward default 파싱 중 오류 발생: " + e.getMessage());
                Bukkit.getLogger().severe("문제 객체: " + map);
            }
        }
        return rewards;
    }
}

