package com.github.maharong.randombox.listener;

import com.github.maharong.randombox.RandomBox;
import com.github.maharong.randombox.box.BoxData;
import com.github.maharong.randombox.reward.RewardGroup;
import com.github.maharong.randombox.reward.RewardSelector;
import com.github.maharong.randombox.util.NBTUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BoxUseListener implements Listener {

    private final RandomBox plugin;

    public BoxUseListener(RandomBox plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUseBox(PlayerInteractEvent event) {
        // 오프핸드 중복 실행 방지
        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        // NBT로부터 box-id 추출
        String boxId = NBTUtil.getBoxId(item);
        if (boxId == null) return;

        // box.yml에 정의된 박스 확인
        BoxData box = plugin.getBoxManager().getBox(boxId);
        if (box == null) return;

        // 이벤트 중복 실행 방지
        event.setCancelled(true);

        // 수량 1개 이하 → 제거, 그 외엔 1 감소
        int amount = item.getAmount();
        if (amount <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(amount - 1);
            player.getInventory().setItemInMainHand(item);
        }

        // 보상 그룹 추첨
        List<RewardGroup> groups = box.rewards();
        RewardGroup drawn = RewardSelector.draw(groups, player);
        if (drawn != null) {
            drawn.giveAll(player); // 보상 지급
        } else {
            // 꽝 보상
            List<RewardGroup> defaults = box.defaultRewards();
            if (defaults != null && !defaults.isEmpty()) {
                for (RewardGroup fallback : defaults) {
                    fallback.giveAll(player);
                }
            }
        }
    }
}
