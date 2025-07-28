package com.github.maharong.randombox.reward;

import com.github.maharong.randombox.util.MessageUtil;
import com.github.maharong.randombox.util.VaultUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * box.yml 내 단일 리워드 항목을 표현하는 클래스입니다.
 * 각 리워드는 타입(ITEM, MONEY, EXP 등)과 관련 속성 데이터를 포함합니다.
 */
public record Reward(RewardType type, Map<String, Object> data) {

    /**
     * 해당 리워드를 실행하여 플레이어에게 보상을 지급합니다.
     *
     * @param player 보상을 받을 대상 플레이어
     */
    public void give(Player player) {
        switch (type) {
            case ITEM -> giveItem(player);
            case MONEY -> giveMoney(player);
            case EXP -> giveExp(player);
            case COMMAND -> runCommand(player);
            case BROADCAST -> sendBroadcast(player);
            case MESSAGE -> sendMessage(player);
        }
    }

    // ------------------------------------------------------------------
    // 각 리워드 타입별 실행 메서드
    // ------------------------------------------------------------------

    /**
     * ITEM 타입 보상: 일반 또는 커스텀 아이템을 플레이어 인벤토리에 지급
     */
    private void giveItem(Player player) {
        String itemId = (String) data.get("item"); // 아이템 종류 (예: DIAMOND_SWORD)
        int amount = ((Number) data.getOrDefault("amount", 1)).intValue(); // 수량
        ItemStack item = new ItemStack(Objects.requireNonNull(Material.matchMaterial(itemId)), amount);

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 아이템 이름 지정 (MiniMessage 지원)
            if (data.containsKey("name")) {
                meta.displayName(MiniMessage.miniMessage().deserialize((String) data.get("name")));
            }

            // 아이템 로어 지정
            if (data.containsKey("lore")) {
                @SuppressWarnings("unchecked")
                List<String> loreList = (List<String>) data.get("lore");
                List<Component> lore = loreList.stream()
                        .map(line -> MiniMessage.miniMessage().deserialize(line))
                        .toList();
                meta.lore(lore);
            }

            // 반짝임 효과
            if ((boolean) data.getOrDefault("shiny", false)) {
                meta.setEnchantmentGlintOverride(true);
            }

            // 인챈트 추가 처리
            if (data.containsKey("enchant")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> enchantMap = (Map<String, Object>) data.get("enchant");
                enchantMap.forEach((enchantName, levelObj) -> {
                    NamespacedKey key = NamespacedKey.minecraft(enchantName.toLowerCase());
                    Enchantment enchant = Registry.ENCHANTMENT.get(key);
                    if (enchant != null) {
                        int level = ((Number) levelObj).intValue();
                        meta.addEnchant(enchant, level, true);
                    }
                });
            }
            item.setItemMeta(meta);
        }

        player.getInventory().addItem(item);
    }

    /**
     * MONEY 타입 보상: Vault를 통해 플레이어에게 돈 지급
     */
    private void giveMoney(Player player) {
        double amount = ((Number) data.getOrDefault("amount", 0)).doubleValue();
        if (!VaultUtil.isEnabled()) {
            player.sendMessage(MessageUtil.get("plugin.vault-missing", Map.of()));
            Bukkit.getLogger().warning("Vault 미탑재 상태에서 MONEY 보상이 무시됩니다.");
            return; // 보상 미지급
        }
        VaultUtil.give(player, amount);
    }

    /**
     * EXP 타입 보상: 플레이어에게 경험치 지급
     */
    private void giveExp(Player player) {
        int amount = ((Number) data.getOrDefault("amount", 0)).intValue();
        player.giveExp(amount);
    }

    /**
     * COMMAND 타입 보상: 콘솔 명령어 실행, {player}는 대상 플레이어 이름으로 치환
     */
    private void runCommand(Player player) {
        String command = (String) data.get("command");
        String replaced = command.replace("{player}", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replaced);
    }

    /**
     * BROADCAST 타입 보상: 서버 전체에 MiniMessage로 브로드캐스트 메시지 전송
     */
    private void sendBroadcast(Player player) {
        String text = (String) data.get("text");
        Component rawMessage = MessageUtil.getFromRaw(text, Map.of("player", player.getName()));

        String prefix = MessageUtil.getPlain("prefix.box", "");
        Component finalMessage = Component.text("")
                .append(MiniMessage.miniMessage().deserialize(prefix))
                .append(Component.space())
                .append(rawMessage);

        Bukkit.broadcast(finalMessage);
    }

    /**
     * MESSAGE 타입 보상: 대상 플레이어에게 개인 메시지 전송
     */
    private void sendMessage(Player player) {
        String text = (String) data.get("text");
        Component rawMessage = MessageUtil.getFromRaw(text, Map.of("player", player.getName()));

        // prefix 수동 부착
        String prefix = MessageUtil.getPlain("prefix.box", "");
        Component finalMessage = Component.text("")
                .append(MiniMessage.miniMessage().deserialize(prefix))
                .append(Component.space())
                .append(rawMessage);

        player.sendMessage(finalMessage);
    }
}
