package com.sparkybetacreativemini;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlotManager {

    private static final int PLOT_SIZE = 256;
    private final SparkyBetaCreativeMini plugin;
    private final Map<String, Plot> plots = new HashMap<>();
    private File plotsFile;
    private FileConfiguration plotsConfig;

    public PlotManager(SparkyBetaCreativeMini plugin) {
        this.plugin = plugin;
        setupPlotConfig();
        loadPlots();
    }

    private void setupPlotConfig() {
        plotsFile = new File(plugin.getDataFolder(), "plots.yml");
        if (!plotsFile.exists()) {
            try {
                plotsFile.getParentFile().mkdirs();
                plotsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create plots.yml!");
            }
        }
        plotsConfig = YamlConfiguration.loadConfiguration(plotsFile);
    }

    public void loadPlots() {
        plots.clear();
        ConfigurationSection plotsSection = plotsConfig.getConfigurationSection("plots");
        if (plotsSection != null) {
            for (String key : plotsSection.getKeys(false)) {
                try {
                    int x = plotsSection.getInt(key + ".x");
                    int z = plotsSection.getInt(key + ".z");
                    UUID owner = UUID.fromString(plotsSection.getString(key + ".owner"));

                    Plot plot = new Plot(x, z, owner);
                    plot.setPvpAllowed(plotsSection.getBoolean(key + ".pvp", false));
                    plot.setPublic(plotsSection.getBoolean(key + ".public", false));
                    plot.setVisitorGameMode(GameMode.valueOf(plotsSection.getString(key + ".gamemode", "SURVIVAL")));
                    // Load new flags, defaulting to false for old plots
                    plot.setMobSpawningAllowed(plotsSection.getBoolean(key + ".mob-spawning", false));
                    plot.setInteractionAllowed(plotsSection.getBoolean(key + ".interaction", false));
                    plot.setExplosionsAllowed(plotsSection.getBoolean(key + ".explosions", false));

                    List<String> trustedUuids = plotsSection.getStringList(key + ".trusted");
                    for (String uuidString : trustedUuids) {
                        plot.addTrustedPlayer(UUID.fromString(uuidString));
                    }

                    List<String> bannedUuids = plotsSection.getStringList(key + ".banned");
                    for (String uuidString : bannedUuids) {
                        plot.addBannedPlayer(UUID.fromString(uuidString));
                    }

                    plots.put(x + ";" + z, plot);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load plot: " + key + ". Reason: " + e.getMessage());
                }
            }
        }
        plugin.getLogger().info("Loaded " + plots.size() + " plots.");
    }

    public void savePlots() {
        // Create a new section to avoid issues with old data
        plotsConfig.createSection("plots");
        ConfigurationSection plotsSection = plotsConfig.getConfigurationSection("plots");

        for (Plot plot : plots.values()) {
            String key = plot.getX() + "_" + plot.getZ();
            ConfigurationSection plotSection = plotsSection.createSection(key);

            plotSection.set("x", plot.getX());
            plotSection.set("z", plot.getZ());
            plotSection.set("owner", plot.getOwner().toString());
            plotSection.set("pvp", plot.isPvpAllowed());
            plotSection.set("public", plot.isPublic());
            plotSection.set("gamemode", plot.getVisitorGameMode().name());
            // Save new flags
            plotSection.set("mob-spawning", plot.isMobSpawningAllowed());
            plotSection.set("interaction", plot.isInteractionAllowed());
            plotSection.set("explosions", plot.isExplosionsAllowed());

            List<String> trustedUuids = new ArrayList<>();
            for (UUID uuid : plot.getTrustedPlayers()) {
                trustedUuids.add(uuid.toString());
            }
            plotSection.set("trusted", trustedUuids);

            List<String> bannedUuids = new ArrayList<>();
            for (UUID uuid : plot.getBannedPlayers()) {
                bannedUuids.add(uuid.toString());
            }
            plotSection.set("banned", bannedUuids);
        }
        try {
            plotsConfig.save(plotsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save plots.yml!");
        }
    }

    public void createPlot(Player player) {
        if (getPlotByOwner(player.getUniqueId()) != null) {
            player.sendMessage("§cYou already own a plot! Use /plot home.");
            return;
        }

        World plotWorld = getPlotWorld();
        if (plotWorld == null) {
            player.sendMessage("§cCannot create a plot because the plot world is not loaded. Please contact an admin.");
            return;
        }

        // Simple linear search for now, can be optimized to spiral search
        for (int x = 0; x < 100; x++) {
            for (int z = 0; z < 100; z++) {
                if (!plots.containsKey(x + ";" + z)) {
                    Plot newPlot = new Plot(x, z, player.getUniqueId());
                    plots.put(x + ";" + z, newPlot);
                    player.sendMessage("§aYou have been assigned plot (" + x + ", " + z + ")!");
                    teleportHome(player);
                    return;
                }
            }
        }
        player.sendMessage("§cCould not find an empty plot. Please contact an admin.");
    }

    public void deletePlot(Plot plot) {
        if (plot == null) return;
        plots.remove(plot.getX() + ";" + plot.getZ());
        savePlots(); // Immediately save changes
    }

    public void teleportHome(Player player) {
        Plot plot = getPlotByOwner(player.getUniqueId());
        if (plot == null) {
            player.sendMessage("§cYou do not own a plot. Use /plot create to get one.");
            return;
        }
        teleportHome(player, plot);
    }

    public void teleportHome(Player player, Plot plot) {
        Location homeLocation = getPlotHome(plot);
        if (homeLocation == null) {
            player.sendMessage("§cCould not teleport to the plot. The plot world may not be loaded.");
            return;
        }
        player.teleport(homeLocation);
    }

    public Plot getPlotAt(Location location) {
        if (!location.getWorld().getName().equalsIgnoreCase("plots")) {
            return null;
        }
        int plotX = (int) Math.floor(location.getX() / PLOT_SIZE);
        int plotZ = (int) Math.floor(location.getZ() / PLOT_SIZE);
        return getPlotByCoords(plotX, plotZ);
    }

    public Plot getPlotByCoords(int x, int z) {
        return plots.get(x + ";" + z);
    }

    public Plot getPlotByOwnerName(String playerName) {
        for (Plot plot : plots.values()) {
            org.bukkit.OfflinePlayer owner = plugin.getServer().getOfflinePlayer(plot.getOwner());
            if (owner != null && owner.getName() != null && owner.getName().equalsIgnoreCase(playerName)) {
                return plot;
            }
        }
        return null;
    }

    public java.util.List<Plot> getPublicPlots() {
        return plots.values().stream().filter(Plot::isPublic).toList();
    }

    public Plot getPlotByOwner(UUID ownerId) {
        for (Plot plot : plots.values()) {
            if (plot.getOwner() != null && plot.getOwner().equals(ownerId)) {
                return plot;
            }
        }
        return null;
    }

    public Location getPlotHome(Plot plot) {
        World plotWorld = getPlotWorld();
        if (plotWorld == null) return null;

        double x = (plot.getX() * PLOT_SIZE) + (PLOT_SIZE / 2.0);
        double z = (plot.getZ() * PLOT_SIZE) + (PLOT_SIZE / 2.0);
        // Find a safe Y-level
        double y = plotWorld.getHighestBlockYAt((int) x, (int) z) + 1.5;

        return new Location(plotWorld, x, y, z);
    }

    public World getPlotWorld() {
        World plotWorld = plugin.getServer().getWorld("plots");
        if (plotWorld == null) {
            plugin.getLogger().warning("Plot world named 'plots' not found! Please create a flat world with this name.");
        }
        return plotWorld;
    }
}
