package com.sparkybetacreativemini.guis;

import com.sparkybetacreativemini.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SettingsGUI {

    public static final String GUI_TITLE = "Plot Settings";

    public static void open(Player player, Plot plot) {
        Inventory gui = Bukkit.createInventory(null, 36, GUI_TITLE);

        // --- Row 1: Core Settings ---
        // PvP Setting Item
        ItemStack pvpItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta pvpMeta = pvpItem.getItemMeta();
        pvpMeta.setDisplayName("§cPvP Settings");
        pvpMeta.setLore(Arrays.asList("§7Status: " + (plot.isPvpAllowed() ? "§aAllowed" : "§cDisallowed"), "§eClick to toggle"));
        pvpItem.setItemMeta(pvpMeta);

        // Public Setting Item
        ItemStack publicItem = new ItemStack(Material.OAK_SIGN);
        ItemMeta publicMeta = publicItem.getItemMeta();
        publicMeta.setDisplayName("§bPlot Visibility");
        publicMeta.setLore(Arrays.asList("§7Status: " + (plot.isPublic() ? "§aPublic" : "§cPrivate"), "§eClick to toggle"));
        publicItem.setItemMeta(publicMeta);

        // Gamemode Setting Item
        ItemStack gamemodeItem = new ItemStack(Material.COMPASS);
        ItemMeta gamemodeMeta = gamemodeItem.getItemMeta();
        gamemodeMeta.setDisplayName("§dVisitor Gamemode");
        gamemodeMeta.setLore(Arrays.asList("§7Mode: §a" + plot.getVisitorGameMode().name(), "§eClick to cycle"));
        gamemodeItem.setItemMeta(gamemodeMeta);

        // Trusted Players Item
        ItemStack trustedItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta trustedMeta = trustedItem.getItemMeta();
        trustedMeta.setDisplayName("§6Trusted Players");
        trustedMeta.setLore(Arrays.asList("§eClick to manage trusted players"));
        trustedItem.setItemMeta(trustedMeta);

        // --- Row 2: New Flags ---
        // Mob Spawning Item
        ItemStack mobSpawningItem = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta mobSpawningMeta = mobSpawningItem.getItemMeta();
        mobSpawningMeta.setDisplayName("§2Mob Spawning");
        mobSpawningMeta.setLore(Arrays.asList("§7Hostile mobs: " + (plot.isMobSpawningAllowed() ? "§aAllowed" : "§cDisallowed"), "§eClick to toggle"));
        mobSpawningItem.setItemMeta(mobSpawningMeta);

        // Interaction Item
        ItemStack interactionItem = new ItemStack(Material.OAK_DOOR);
        ItemMeta interactionMeta = interactionItem.getItemMeta();
        interactionMeta.setDisplayName("§eInteraction");
        interactionMeta.setLore(Arrays.asList("§7Doors, buttons, etc.: " + (plot.isInteractionAllowed() ? "§aAllowed" : "§cDisallowed"), "§eClick to toggle"));
        interactionItem.setItemMeta(interactionMeta);

        // Explosions Item
        ItemStack explosionsItem = new ItemStack(Material.TNT);
        ItemMeta explosionsMeta = explosionsItem.getItemMeta();
        explosionsMeta.setDisplayName("§4Explosions");
        explosionsMeta.setLore(Arrays.asList("§7TNT, creepers, etc.: " + (plot.isExplosionsAllowed() ? "§aAllowed" : "§cDisallowed"), "§eClick to toggle"));
        explosionsItem.setItemMeta(explosionsMeta);

        // Set items in GUI
        gui.setItem(10, pvpItem);
        gui.setItem(12, publicItem);
        gui.setItem(14, gamemodeItem);
        gui.setItem(16, trustedItem);

        gui.setItem(19, mobSpawningItem);
        gui.setItem(21, interactionItem);
        gui.setItem(23, explosionsItem);

        player.openInventory(gui);
    }
}
