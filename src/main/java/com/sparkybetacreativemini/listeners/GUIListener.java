package com.sparkybetacreativemini.listeners;

import com.sparkybetacreativemini.Plot;
import com.sparkybetacreativemini.SparkyBetaCreativeMini;
import com.sparkybetacreativemini.guis.SettingsGUI;
import com.sparkybetacreativemini.guis.TrustedPlayersGUI;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener {

    private final SparkyBetaCreativeMini plugin;

    public GUIListener(SparkyBetaCreativeMini plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String viewTitle = event.getView().getTitle();

        // Route click events to the correct handler based on GUI title
        if (viewTitle.equals(SettingsGUI.GUI_TITLE)) {
            handleSettingsGUIClick(event);
        } else if (viewTitle.equals(TrustedPlayersGUI.GUI_TITLE)) {
            handleTrustedPlayersGUIClick(event);
        }
    }

    private void handleSettingsGUIClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        Plot plot = plugin.getPlotManager().getPlotByOwner(player.getUniqueId());

        if (plot == null) {
            player.closeInventory();
            player.sendMessage("§cCould not find your plot data.");
            return;
        }

        int slot = event.getSlot();
        switch (slot) {
            case 10: // PvP
                plot.setPvpAllowed(!plot.isPvpAllowed());
                break;
            case 12: // Public
                plot.setPublic(!plot.isPublic());
                break;
            case 14: // Gamemode
                GameMode current = plot.getVisitorGameMode();
                GameMode next = switch (current) {
                    case SURVIVAL -> GameMode.CREATIVE;
                    case CREATIVE -> GameMode.ADVENTURE;
                    case ADVENTURE -> GameMode.SPECTATOR;
                    case SPECTATOR -> GameMode.SURVIVAL;
                };
                plot.setVisitorGameMode(next);
                break;
            case 16: // Trusted Players
                TrustedPlayersGUI.open(player, plot);
                return; // Don't reopen settings GUI
            case 19: // Mob Spawning
                plot.setMobSpawningAllowed(!plot.isMobSpawningAllowed());
                break;
            case 21: // Interaction
                plot.setInteractionAllowed(!plot.isInteractionAllowed());
                break;
            case 23: // Explosions
                plot.setExplosionsAllowed(!plot.isExplosionsAllowed());
                break;
            default:
                return; // Clicked on empty space
        }
        // Re-open the GUI to show the updated information
        SettingsGUI.open(player, plot);
    }

    private void handleTrustedPlayersGUIClick(InventoryClickEvent event) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        Plot plot = plugin.getPlotManager().getPlotByOwner(player.getUniqueId());

        if (plot == null) {
            player.closeInventory();
            player.sendMessage("§cCould not find your plot data.");
            return;
        }

        if (event.getSlot() == 49) { // Add Player
            plugin.getPlayerAddingTrust().put(player.getUniqueId(), plot);
            player.closeInventory();
            player.sendMessage("§aPlease type the name of the player you want to trust in chat.");
        } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() == org.bukkit.Material.PLAYER_HEAD) { // Remove Player
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) event.getCurrentItem().getItemMeta();
            if (meta != null && meta.getOwningPlayer() != null) {
                plot.removeTrustedPlayer(meta.getOwningPlayer().getUniqueId());
                TrustedPlayersGUI.open(player, plot); // Refresh GUI
            }
        }
    }
}
