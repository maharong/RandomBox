package com.github.maharong.randombox.util;

import com.github.maharong.randombox.RandomBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class MessageUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static FileConfiguration messages;

    private static final Map<String, String> prefixMap = new HashMap<>();
    private static final Map<String, Set<String>> prefixSkipMap = new HashMap<>();

    // 메시지 파일 로드
    public static void load(FileConfiguration config) {
        messages = config;
        // prefix 항목 로딩
        if (config.isConfigurationSection("prefix")) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("prefix")).getKeys(false)) {
                prefixMap.put(key, config.getString("prefix." + key));
            }
        }

        // prefix-skip 항목 로딩
        if (config.isConfigurationSection("prefix-skip")) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("prefix-skip")).getKeys(false)) {
                List<String> list = config.getStringList("prefix-skip." + key);
                prefixSkipMap.put(key, new HashSet<>(list));
            }
        }
    }

    // 메시지 Component 반환 (플레이스홀더 O)
    public static Component get(String path, Map<String, String> placeholders) {
        String raw = messages.getString(path);
        if (raw == null) {
            raw = "<red>[오류] 메시지를 찾을 수 없습니다.";
        }
        // 접두어 처리
        String prefix = getPrefix(path);
        if (prefix != null) {
            raw = prefix + " " + raw;
        }

        for (String key : placeholders.keySet()) {
            raw = raw.replace("{" + key + "}", "<" + key + ">");
        }
        TagResolver.Builder resolver = TagResolver.builder();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolver.resolver(Placeholder.parsed(entry.getKey(), entry.getValue()));
        }
        return MINI.deserialize(raw, resolver.build());
    }

    // 메시지 Component 반환 (플레이스홀더 X)
    public static Component get(String path) {
        return get(path, Map.of());
    }

    // 메세지 String 반환
    public static String getPlain(String path, String defaultValue) {
        return messages.getString(path, defaultValue);
    }

    // 리스트 반환
    public static List<String> getStringList(String path) {
        return messages.getStringList(path);
    }

    /**
     * 메시지를 플레이스홀더와 함께 전송합니다.
     * 짝수 개의 문자열 키-값 쌍으로 플레이스홀더를 간단히 전달할 수 있습니다.
     * 예:
     * MessageUtil.send(player, "item.give.success",
     *     "{player}", player.getName(),
     *     "{item}", "spin_sword");
     *
     * @param sender 대상 명령어 실행자
     * @param path message.yml 내 메시지 경로
     * @param replacements "{key}", "value" 형식의 문자열 쌍
     */
    public static void send(CommandSender sender, String path, String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("짝수 개의 플레이스홀더 키-값이 필요합니다.");
        }

        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < replacements.length; i += 2) {
            placeholders.put(replacements[i].replace("{", "").replace("}", ""), replacements[i + 1]);
        }

        send(sender, path, placeholders);
    }

    // 메시지 전송 (플레이스홀더 O)
    public static void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(get(path, placeholders));
    }

    // 메시지 전송 (플레이스홀더 X)
    public static void send(CommandSender sender, String path) {
        sender.sendMessage(get(path));
    }

    // 문자열 리스트 반환 (prefix 없이 일반 출력용)
    public static List<String> getPlainList(String path) {
        if (!messages.contains(path)) return Collections.emptyList();
        return messages.getStringList(path);
    }

    public static void sendRaw(CommandSender sender, String raw) {
        sender.sendMessage(MINI.deserialize(raw));
    }

    // 플레이스홀더를 MiniMessage 용으로 치환
    public static Component getFromRaw(String raw, Map<String, String> placeholders) {
        for (String key : placeholders.keySet()) {
            raw = raw.replace("{" + key + "}", "<" + key + ">");
        }

        TagResolver.Builder resolver = TagResolver.builder();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolver.resolver(Placeholder.parsed(entry.getKey(), entry.getValue()));
        }

        return MINI.deserialize(raw, resolver.build());
    }

    // 메세지에서 bool 값 불러오기
    public static boolean getBoolean(String path, boolean defaultValue) {
        if (!messages.contains(path)) return defaultValue;
        return messages.getBoolean(path);
    }

    // 접두어 조회
    private static String getPrefix(String path) {
        for (String category : prefixMap.keySet()) {
            if (path.startsWith(category + ".")) {
                Set<String> skipList = prefixSkipMap.getOrDefault(category, Set.of());
                if (skipList.contains(path)) {
                    return null;
                }
                return prefixMap.get(category);
            }
        }
        return null;
    }

    public static void reload() {
        File file = new File(RandomBox.getInstance().getDataFolder(), "message.yml");
        if (!file.exists()) {
            RandomBox.getInstance().saveResource("message.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        load(config); // prefixMap, prefixSkipMap도 재로딩됨
    }
}