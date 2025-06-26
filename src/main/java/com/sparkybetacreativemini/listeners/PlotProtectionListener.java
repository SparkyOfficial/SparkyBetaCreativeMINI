package com.sparkybetacreativemini.listeners;

import com.sparkybetacreativemini.Plot;
import com.sparkybetacreativemini.SparkyBetaCreativeMini;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlotProtectionListener implements Listener {

    private final SparkyBetaCreativeMini plugin;

    public PlotProtectionListener(SparkyBetaCreativeMini plugin) {
        this.plugin = plugin;
    }

    private boolean canBuild(Player player, Plot plot) {
        if (plot.getOwner() == null) return true; // Unclaimed plot
        return plot.getOwner().equals(player.getUniqueId()) || plot.getTrustedPlayers().contains(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().getWorld().getName().equalsIgnoreCase("plots")) {
            return;
        }

        Player player = event.getPlayer();
        Plot plot = plugin.getPlotManager().getPlotAt(event.getBlock().getLocation());

        if (plot != null) {
            if (!canBuild(player, plot)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot build here! This plot is owned by " + plugin.getServer().getOfflinePlayer(plot.getOwner()).getName() + ".");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().getWorld().getName().equalsIgnoreCase("plots")) {
            return;
        }

        Player player = event.getPlayer();
        Plot plot = plugin.getPlotManager().getPlotAt(event.getBlock().getLocation());

        if (plot != null) {
            if (!canBuild(player, plot)) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot build here! This plot is owned by " + plugin.getServer().getOfflinePlayer(plot.getOwner()).getName() + ".");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().getWorld().getName().equalsIgnoreCase("plots")) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        Plot plot = plugin.getPlotManager().getPlotAt(event.getClickedBlock().getLocation());

        if (plot != null && !plot.isInteractionAllowed()) {
            if (!canBuild(event.getPlayer(), plot)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cYou cannot interact with items on this plot.");
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getLocation().getWorld() == null || !event.getLocation().getWorld().getName().equalsIgnoreCase("plots")) {
            return;
        }

        Plot plot = plugin.getPlotManager().getPlotAt(event.getLocation());

        if (plot != null && !plot.isExplosionsAllowed()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getLocation().getWorld() == null || !event.getLocation().getWorld().getName().equalsIgnoreCase("plots")) {
            return;
        }

        // Allow passive mobs, villagers, etc.
        EntityType type = event.getEntityType();
        if (type.isAlive() && type.isSpawnable() && type != EntityType.PLAYER) {
            // Use instanceof checks for broader compatibility than getCategory()
            Class<?> entityClass = type.getEntityClass();
            if (entityClass != null && (Animals.class.isAssignableFrom(entityClass) || 
                                        WaterMob.class.isAssignableFrom(entityClass) || 
                                        Ambient.class.isAssignableFrom(entityClass) || 
                                        Villager.class.isAssignableFrom(entityClass))) {
                return; // Always allow passive/neutral mobs
            }
        }

        Plot plot = plugin.getPlotManager().getPlotAt(event.getLocation());

        if (plot != null && !plot.isMobSpawningAllowed()) {
            // Only block natural spawns, not from spawn eggs etc.
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Optimization: check if player actually moved to a new block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Plot plot = plugin.getPlotManager().getPlotAt(event.getTo());

        if (plot != null && plot.isBanned(player.getUniqueId())) {
            // An owner cannot be banned from their own plot, this is a safeguard.
            if (plot.getOwner().equals(player.getUniqueId())) {
                return;
            }

            // Prevent entering the plot by teleporting them back to the previous location.
            event.setCancelled(true);
            player.teleport(event.getFrom());
            player.sendMessage("§cYou are banned from this plot.");
        }
    }
}
