package com.sparkybetacreativemini.listeners;

import com.sparkybetacreativemini.Plot;
import com.sparkybetacreativemini.SparkyBetaCreativeMini;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final SparkyBetaCreativeMini plugin;

    public ChatListener(SparkyBetaCreativeMini plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPlayerAddingTrust().containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        Plot plot = plugin.getPlayerAddingTrust().get(player.getUniqueId());
        String targetName = event.getMessage();
        Player target = Bukkit.getPlayer(targetName);

        // Must run on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target == null) {
                player.sendMessage("§cPlayer '" + targetName + "' not found online.");
            } else {
                plot.addTrustedPlayer(target.getUniqueId());
                player.sendMessage("§aSuccessfully added " + target.getName() + " to your plot.");
                target.sendMessage("§aYou have been trusted to build on " + player.getName() + "'s plot.");
            }
            // Remove player from the map so they can chat normally
            plugin.getPlayerAddingTrust().remove(player.getUniqueId());
        });
    }
}
