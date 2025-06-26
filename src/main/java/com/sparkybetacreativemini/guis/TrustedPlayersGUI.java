package com.sparkybetacreativemini.guis;

import com.sparkybetacreativemini.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.UUID;

public class TrustedPlayersGUI {

    public static final String GUI_TITLE = "Trusted Players";

    public static void open(Player player, Plot plot) {
        // 54 slots for more players
        Inventory gui = Bukkit.createInventory(null, 54, GUI_TITLE);

        // Add Player Item
        ItemStack addItem = new ItemStack(Material.LIME_WOOL);
        var addMeta = addItem.getItemMeta();
        addMeta.setDisplayName("§aAdd Trusted Player");
        addMeta.setLore(Arrays.asList("§eClick here and type a player's name in chat."));
        addItem.setItemMeta(addMeta);
        gui.setItem(49, addItem); // Center of the bottom row

        // List current trusted players
        for (UUID trustedUUID : plot.getTrustedPlayers()) {
            OfflinePlayer trustedPlayer = Bukkit.getOfflinePlayer(trustedUUID);
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
            headMeta.setOwningPlayer(trustedPlayer);
            headMeta.setDisplayName("§c" + trustedPlayer.getName());
            headMeta.setLore(Arrays.asList("§eClick to remove this player."));
            playerHead.setItemMeta(headMeta);
            gui.addItem(playerHead);
        }

        player.openInventory(gui);
    }
}
