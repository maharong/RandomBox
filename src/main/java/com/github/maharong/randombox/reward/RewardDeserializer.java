package com.github.maharong.randombox.reward;

import java.util.Map;

/**
 * 리워드 정보를 YML에서 로드된 Map 형태로부터 파싱하여
 * Reward 객체로 변환하는 유틸리티 클래스입니다.
 */
public class RewardDeserializer {

    /**
     * Map 데이터를 기반으로 Reward 객체를 생성합니다.
     *
     * @param map 리워드 정보를 담고 있는 Map (box.yml에서 로드됨)
     * @return 변환된 Reward 인스턴스
     * @throws IllegalArgumentException 필수 키 누락 또는 지원되지 않는 타입일 경우 발생
     */
    public static Reward fromMap(Map<String, Object> map) {
        if (!map.containsKey("type"))
            throw new IllegalArgumentException("리워드 항목에 'type'이 존재하지 않습니다.");

        // type을 분리하고, 나머지는 data로 넘김
        RewardType type;
        try {
            type = RewardType.valueOf(String.valueOf(map.get("type")).toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 리워드 타입입니다: " + map.get("type"));
        }

        // 복사본에서 type 제거 (data 맵에서)
        map.remove("type");

        return new Reward(type, map);
    }
}
