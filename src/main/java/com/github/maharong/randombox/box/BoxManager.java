package com.github.maharong.randombox.box;

import com.github.maharong.randombox.RandomBox;
import com.github.maharong.randombox.reward.RewardGroup;
import com.github.maharong.randombox.util.MessageUtil;
import com.github.maharong.randombox.util.YamlUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 랜덤 박스 전체를 관리하는 클래스입니다.
 * box.yml에 정의된 모든 박스를 파싱하고, ID로 접근할 수 있도록 저장합니다.
 */
public class BoxManager {
    private final Map<String, BoxData> boxMap = new HashMap<>();

    /**
     * box.yml을 파싱하여 모든 박스를 로드합니다.
     * 구성 오류 발생 시 플러그인을 비활성화합니다.
     *
     * @param config box.yml의 루트 설정 (boxes 섹션 포함)
     */
    public void load(FileConfiguration config) {
        boxMap.clear();

        ConfigurationSection boxesSection = config.getConfigurationSection("boxes");
        if (boxesSection == null) {
            error("box.yml 에 'boxes' 섹션이 존재하지 않습니다.");
            return;
        }

        for (String id : boxesSection.getKeys(false)) {
            ConfigurationSection section = boxesSection.getConfigurationSection(id);
            if (section == null) continue;

            try {
                // lootbox
                ConfigurationSection lootSec = section.getConfigurationSection("lootbox");
                if (lootSec == null) throw new IllegalArgumentException("lootbox 섹션이 없습니다.");
                Material material = Material.matchMaterial(lootSec.getString("material", "PAPER"));
                if (material == null) throw new IllegalArgumentException("잘못된 lootbox material입니다.");

                String name = lootSec.getString("name", "<gray>랜덤 박스");
                List<String> lore = lootSec.getStringList("lore");
                boolean shiny = lootSec.getBoolean("shiny", false);

                BoxData.LootBox lootBox = new BoxData.LootBox(material, name, lore, shiny);

                // rewards
                List<?> tempList = section.getList("rewards");
                if (tempList == null) {
                    Bukkit.getLogger().warning("[RandomBox] '" + id + "' 박스: rewards 섹션이 null입니다.");
                } else {
                    Bukkit.getLogger().info("[RandomBox] '" + id + "' 박스: rewards 섹션 로드 성공, 항목 수: " + tempList.size());
                }
                List<RewardGroup> rewards = YamlUtil.parseRewardGroups(
                        (List<Map<String, Object>>) (tempList != null ? tempList : new ArrayList<>())
                );
                double totalChance = rewards.stream().mapToDouble(RewardGroup::chance).sum();

                // default.rewards
                List<?> tempDefaultList = section.getList("default.rewards");
                List<RewardGroup> defaultRewards = new ArrayList<>();

                if (tempDefaultList != null && !tempDefaultList.isEmpty()) {
                    Object first = tempDefaultList.getFirst();
                    if (first instanceof Map<?, ?> map && !map.containsKey("rewards")) {
                        // 단일 Reward 리스트로 추정 → 감싸서 RewardGroup 생성
                        List<Map<String, Object>> rawRewards = new ArrayList<>();
                        for (Object obj : tempDefaultList) {
                            if (obj instanceof Map<?, ?> m) {
                                rawRewards.add((Map<String, Object>) m);
                            }
                        }
                        RewardGroup group = new RewardGroup(0.0, YamlUtil.parseRewards(rawRewards), null);
                        defaultRewards.add(group);
                    } else {
                        // 정상 구조 (RewardGroup 리스트)
                        List<Map<String, Object>> rawGroups = new ArrayList<>();
                        for (Object obj : tempDefaultList) {
                            if (obj instanceof Map<?, ?> m) {
                                rawGroups.add((Map<String, Object>) m);
                            }
                        }
                        defaultRewards = YamlUtil.parseRewardGroups(rawGroups);
                    }
                }

                // 1. 100 초과 검사
                if (totalChance > 100.0) {
                    throw new IllegalArgumentException("보상 확률 총합이 100을 초과합니다. 현재: " + totalChance + "%");
                }

                // 2. 100 미만일 경우 default 보상 검사 및 보정 추가
                if (totalChance < 100.0) {
                    if (defaultRewards.isEmpty()) {
                        throw new IllegalArgumentException("확률 총합이 100 미만인데 default.rewards가 존재하지 않습니다.");
                    }

                    // 직접 보정용 RewardGroup 생성
                    double remaining = 100.0 - totalChance;

                    // 첫 RewardGroup에서 rewards 목록만 꺼내서 보정
                    RewardGroup rawGroup = defaultRewards.getFirst();
                    RewardGroup defaultGroup = new RewardGroup(
                            remaining,
                            rawGroup.rewards(), // <- rewards 항목만 파싱
                            null // 조건 없음
                    );

                    rewards.add(defaultGroup);
                }

                BoxData data = new BoxData(id, lootBox, rewards, defaultRewards);
                boxMap.put(id, data);

            } catch (Exception e) {
                error("박스 '" + id + "' 로딩 중 오류 발생: " + e.getMessage());
                return;
            }
        }

        Bukkit.getLogger().info("[RandomBox] box.yml 로딩 완료 - 총 " + boxMap.size() + "개 박스 등록됨.");
    }

    private void error(String message) {
        Bukkit.getLogger().severe("[RandomBox] 오류: " + message);
        Bukkit.getPluginManager().disablePlugin(RandomBox.getInstance());
    }

    /**
     * ID로 박스를 조회합니다.
     *
     * @param id 박스 ID
     * @return 해당 박스 데이터, 없으면 null
     */
    public BoxData getBox(String id) {
        return boxMap.get(id);
    }

    /**
     * 등록된 모든 박스를 반환합니다.
     *
     * @return ID → 박스 데이터 Map
     */
    public Map<String, BoxData> getAllBoxes() {
        return boxMap;
    }

    public void sendBoxList(CommandSender sender) {
        if (boxMap.isEmpty()) {
            MessageUtil.send(sender, "box.empty");
            return;
        }

        MessageUtil.send(sender, "box.list.header");
        for (String id : boxMap.keySet()) {
            BoxData data = boxMap.get(id);
            MessageUtil.send(sender, "box.list.entry", "{id}", id, "{name}", data.lootBox().name());
        }
    }

    public void sendBoxInfo(CommandSender sender, String id) {
        BoxData data = boxMap.get(id);
        if (data == null) {
            MessageUtil.send(sender, "box.not-found", "{id}", id);
            return;
        }

        MessageUtil.send(sender, "box.info.header", "{id}", id);
        MessageUtil.send(sender, "box.info.name", "{name}", data.lootBox().name());
        MessageUtil.send(sender, "box.info.material", "{material}", data.lootBox().material().name());
        MessageUtil.send(sender, "box.info.shiny", "{shiny}", String.valueOf(data.lootBox().shiny()));
        MessageUtil.send(sender, "box.info.rewards", "{count}", String.valueOf(data.rewards().size()));
    }

    public boolean giveBox(Player player, String id, int amount) {
        BoxData data = boxMap.get(id);
        if (data == null) return false;

        ItemStack item = BoxCreator.create(id, data.lootBox());
        item.setAmount(amount);

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            // 인벤토리 공간 부족 → 바닥에 드랍
            leftover.values().forEach(stack -> player.getWorld().dropItemNaturally(player.getLocation(), stack));
        }

        return true;
    }

    public void reload() {
        FileConfiguration boxConfig =
                YamlConfiguration.loadConfiguration(new File(RandomBox.getInstance().getDataFolder(), "box.yml"));
        load(boxConfig);
        MessageUtil.reload();
    }
    public List<String> getAllBoxIds() {
        return new ArrayList<>(boxMap.keySet());
    }

}
