package com.github.maharong.randombox.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultUtil {
    private static Economy econ;

    public static void setup(Plugin plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            plugin.getLogger().warning("Vault가 설치되어 있지 않아 MONEY 보상이 비활성화됩니다.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        } else {
            plugin.getLogger().warning("Vault 경제 시스템 초기화 실패: Economy provider 없음.");
        }
    }

    public static boolean isEnabled() {
        return econ != null;
    }

    public static void give(Player player, double amount) {
        if (!isEnabled()) return;
        econ.depositPlayer(player, amount);
    }
}
