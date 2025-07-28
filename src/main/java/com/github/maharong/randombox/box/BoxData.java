package com.github.maharong.randombox.box;

import com.github.maharong.randombox.reward.RewardGroup;
import org.bukkit.Material;
import java.util.List;

/**
 * 랜덤 박스 정보를 저장하는 클래스입니다.
 *
 * @param id 박스의 고유 ID (명령어 등에서 사용)
 * @param lootBox 박스를 열기 위한 티켓 아이템 정보
 * @param rewards 확률 기반 보상 그룹 목록
 * @param defaultRewards 확률 누락 시 적용되는 '꽝' 보상 그룹 (합산 확률이 100% 미만일 때 사용)
 */
public record BoxData(String id, LootBox lootBox,
                      List<RewardGroup> rewards, List<RewardGroup> defaultRewards) {

    /**
     * 박스를 열기 위한 티켓 정보입니다.
     *
     * @param material 티켓의 아이템 종류 (예: PAPER, NAME_TAG 등)
     * @param name 티켓의 표시 이름 (MiniMessage 형식 허용, 메시지 출력에도 사용)
     * @param lore 티켓의 로어 (한 줄씩 입력, MiniMessage 형식 허용)
     * @param shiny 티켓에 반짝임 효과 부여 여부
     */
    public record LootBox(Material material, String name, List<String> lore, boolean shiny) {
    }

    /**
     * 박스 이름 반환 (lootBox.name 사용)
     */
    public String displayName() {
        return lootBox.name();
    }
}
