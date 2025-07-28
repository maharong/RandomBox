package com.github.maharong.randombox.util;

import com.github.maharong.randombox.RandomBox;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class NBTUtil {
    private static final NamespacedKey BOX_ID_KEY = new NamespacedKey(RandomBox.getInstance(), "box-id");

    public static String getBoxId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(BOX_ID_KEY, PersistentDataType.STRING)) return null;
        return meta.getPersistentDataContainer().get(BOX_ID_KEY, PersistentDataType.STRING);
    }
    public static void setBoxId(ItemStack item, String boxId) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(BOX_ID_KEY, PersistentDataType.STRING, boxId);
        item.setItemMeta(meta);
    }

}
