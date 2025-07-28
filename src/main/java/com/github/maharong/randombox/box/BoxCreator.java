package com.github.maharong.randombox.box;

import com.github.maharong.randombox.util.MessageUtil;
import com.github.maharong.randombox.util.NBTUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code BoxCreator}는 {@link BoxData.LootBox} 데이터를 기반으로
 * 실제 게임 내에서 사용할 수 있는 {@link ItemStack} 형태의 랜덤 박스 아이템을 생성하는 유틸리티 클래스입니다.
 */
public class BoxCreator {
    /**
     * 주어진 {@link BoxData.LootBox} 데이터를 바탕으로 커스텀 박스(랜덤 박스 아이템)을 생성합니다.
     *
     * @param lootBoxData 박스 생성에 필요한 아이템 정보 (소재, 이름, 로어, 반짝임 등)
     * @return 생성된 {@link ItemStack} 객체. MiniMessage 포맷이 적용된 이름과 로어를 포함합니다.
     */
    public static ItemStack create(String id, BoxData.LootBox lootBoxData) {
        // 티켓 아이템 생성 (예: NAME_TAG)
        ItemStack item = new ItemStack(lootBoxData.material());
        ItemMeta meta = item.getItemMeta();

        MiniMessage mm = MiniMessage.miniMessage();

        if (meta != null) {
            // 아이템 이름 설정 (MiniMessage 적용)
            meta.displayName(mm.deserialize(lootBoxData.name()));

            // 로어 (MiniMessage 적용)
            List<String> rawLore = lootBoxData.lore();
            List<Component> lore = new ArrayList<>();
            if (rawLore != null) {
                for (String line : rawLore) {
                    lore.add(mm.deserialize(line));
                }
            }

            // 사용법 자동 추가 (message.yml에서 불러옴)
            String usageRaw = MessageUtil.getPlain("box.usage-lore", "<gray><!i>우클릭으로 사용");
            lore.add(mm.deserialize(usageRaw));
            meta.lore(lore);

            // 반짝임 효과 적용
            if (lootBoxData.shiny()) {
                meta.setEnchantmentGlintOverride(true);
            }

            item.setItemMeta(meta);
        }
        NBTUtil.setBoxId(item, id);
        return item;
    }
}
