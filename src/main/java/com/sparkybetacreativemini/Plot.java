package com.sparkybetacreativemini;

import org.bukkit.GameMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Plot {

    private final int x;
    private final int z;
    private UUID owner;
    private boolean pvpAllowed;
    private boolean isPublic;
    private GameMode visitorGameMode;
    private final List<UUID> trustedPlayers;
    private final List<UUID> bannedPlayers;
    private boolean mobSpawningAllowed;
    private boolean interactionAllowed;
    private boolean explosionsAllowed;

    public Plot(int x, int z, UUID owner) {
        this.x = x;
        this.z = z;
        this.owner = owner;
        // Default settings
        this.pvpAllowed = false;
        this.isPublic = false;
        this.visitorGameMode = GameMode.SURVIVAL;
        this.trustedPlayers = new ArrayList<>();
        this.bannedPlayers = new ArrayList<>();
        this.mobSpawningAllowed = false; // Default: no hostile mobs
        this.interactionAllowed = false; // Default: no interaction for visitors
        this.explosionsAllowed = false;  // Default: no explosions
    }

    //<editor-fold desc="Getters and Setters">
    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean isPvpAllowed() {
        return pvpAllowed;
    }

    public void setPvpAllowed(boolean pvpAllowed) {
        this.pvpAllowed = pvpAllowed;
    }

    public boolean isMobSpawningAllowed() {
        return mobSpawningAllowed;
    }

    public void setMobSpawningAllowed(boolean mobSpawningAllowed) {
        this.mobSpawningAllowed = mobSpawningAllowed;
    }

    public boolean isInteractionAllowed() {
        return interactionAllowed;
    }

    public void setInteractionAllowed(boolean interactionAllowed) {
        this.interactionAllowed = interactionAllowed;
    }

    public boolean isExplosionsAllowed() {
        return explosionsAllowed;
    }

    public void setExplosionsAllowed(boolean explosionsAllowed) {
        this.explosionsAllowed = explosionsAllowed;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public GameMode getVisitorGameMode() {
        return visitorGameMode;
    }

    public void setVisitorGameMode(GameMode visitorGameMode) {
        this.visitorGameMode = visitorGameMode;
    }

    public List<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void addTrustedPlayer(UUID player) {
        if (!trustedPlayers.contains(player)) {
            trustedPlayers.add(player);
        }
    }

    public void removeTrustedPlayer(UUID player) {
        trustedPlayers.remove(player);
    }

    public List<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public boolean isBanned(UUID player) {
        return bannedPlayers.contains(player);
    }

    public void addBannedPlayer(UUID player) {
        if (!bannedPlayers.contains(player)) {
            bannedPlayers.add(player);
        }
    }

    public void removeBannedPlayer(UUID player) {
        bannedPlayers.remove(player);
    }
    //</editor-fold>
}
