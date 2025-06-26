package com.sparkybetacreativemini;

import com.sparkybetacreativemini.commands.PlotCommand;
import com.sparkybetacreativemini.listeners.ChatListener;
import com.sparkybetacreativemini.listeners.GUIListener;
import com.sparkybetacreativemini.listeners.PlotProtectionListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SparkyBetaCreativeMini extends JavaPlugin {

    private PlotManager plotManager;
    private final Map<UUID, Plot> playerAddingTrust = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("SparkyBetaCreativeMINI is starting up...");

        // Create plot world if it doesn't exist
        createPlotWorld();

        // Initialize managers
        this.plotManager = new PlotManager(this);

        // Register commands
        getCommand("plot").setExecutor(new PlotCommand(this, plotManager));

        // Register listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new PlotProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getLogger().info("SparkyBetaCreativeMINI has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (plotManager != null) {
            plotManager.savePlots();
        }
        getLogger().info("SparkyBetaCreativeMINI has been disabled!");
    }

    private void createPlotWorld() {
        String worldName = "plots";
        if (Bukkit.getWorld(worldName) == null) {
            getLogger().info("Plot world '" + worldName + "' not found. Creating it now...");
            WorldCreator wc = new WorldCreator(worldName);
            wc.environment(World.Environment.NORMAL);
            wc.type(WorldType.FLAT);

            World plotWorld = wc.createWorld();
            if (plotWorld != null) {
                // The plot grid is 100x100 plots, each 256x256 blocks.
                // This requires a 25600x25600 block area.
                // We'll center the border so it covers the positive quadrant from 0 to 25600.
                double center = 25600.0 / 2.0;
                plotWorld.getWorldBorder().setCenter(center, center);
                plotWorld.getWorldBorder().setSize(25600.0);
                plotWorld.setSpawnLocation((int)center, 65, (int)center);
                getLogger().info("Plot world '" + worldName + "' created successfully.");
            } else {
                getLogger().severe("Could not create plot world '" + worldName + "'!");
            }
        }
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    public Map<UUID, Plot> getPlayerAddingTrust() {
        return playerAddingTrust;
    }
}
