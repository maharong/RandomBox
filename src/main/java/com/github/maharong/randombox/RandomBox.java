package com.github.maharong.randombox;

import com.github.maharong.randombox.box.BoxManager;
import com.github.maharong.randombox.command.RandomBoxCommand;
import com.github.maharong.randombox.listener.BoxUseListener;
import com.github.maharong.randombox.util.MessageUtil;
import com.github.maharong.randombox.util.VaultUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public final class RandomBox extends JavaPlugin {
    private static RandomBox instance;
    private final BoxManager boxManager = new BoxManager();

    public static RandomBox getInstance() { return instance; }

    @Override
    public void onEnable() {
        instance = this;
        // Vault 체크
        VaultUtil.setup(this);
        // 메시지 설정 로딩
        saveResource("message.yml", false);
        File messageFile = new File(getDataFolder(), "message.yml");
        YamlConfiguration messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        MessageUtil.load(messageConfig);
        // box.yml 로드
        loadBoxConfig();
        // 커맨드, 리스너 등록
        Objects.requireNonNull(getCommand("randombox")).setExecutor(new RandomBoxCommand(boxManager));
        getServer().getPluginManager().registerEvents(new BoxUseListener(this), this);
        getLogger().info("플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        // 비활성화
        getLogger().info("플러그인이 비활성화되었습니다.");
    }

    public BoxManager getBoxManager() {
        return boxManager;
    }

    private void loadBoxConfig() {
        File file = new File(getDataFolder(), "box.yml");

        if (!file.exists()) {
            saveResource("box.yml", false); // 처음 실행 시 jar에서 추출
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        boxManager.load(config);
    }
}
