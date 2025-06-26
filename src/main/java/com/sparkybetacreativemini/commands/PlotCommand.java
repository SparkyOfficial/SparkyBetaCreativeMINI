package com.sparkybetacreativemini.commands;

import com.sparkybetacreativemini.Plot;
import com.sparkybetacreativemini.Plot;
import com.sparkybetacreativemini.PlotManager;
import com.sparkybetacreativemini.SparkyBetaCreativeMini;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotCommand implements CommandExecutor {

    private final PlotManager plotManager;
    private final SparkyBetaCreativeMini plugin;
    private final Map<UUID, UUID> pendingInvites = new HashMap<>(); // Invited UUID -> Inviter's Plot Owner UUID

    public PlotCommand(SparkyBetaCreativeMini plugin, PlotManager plotManager) {
        this.plugin = plugin;
        this.plotManager = plotManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                plotManager.createPlot(player);
                break;
            case "home":
                plotManager.teleportHome(player);
                break;
            case "settings":
                Plot plot = plotManager.getPlotByOwner(player.getUniqueId());
                if (plot == null) {
                    player.sendMessage("§cYou don't own a plot to configure.");
                    return true;
                }
                com.sparkybetacreativemini.guis.SettingsGUI.open(player, plot);
                break;
            case "trust":
                handleTrustCommand(player, args, true);
                break;
            case "untrust":
                handleTrustCommand(player, args, false);
                break;
            case "visit":
                handleVisitCommand(player, args);
                break;
            case "list":
                handleListCommand(player);
                break;
            case "help":
                sendHelpMessage(player);
                break;
            case "leave":
                handleLeaveCommand(player);
                break;
            case "ban":
                handleBanCommand(player, args, true);
                break;
            case "unban":
                handleBanCommand(player, args, false);
                break;
            case "invite":
                handleInviteCommand(player, args);
                break;
            case "accept":
                handleAcceptCommand(player);
                break;
            default:
                player.sendMessage("§cUnknown command. Use /plot help.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§a--- SparkyBetaCreativeMINI Help ---");
        player.sendMessage("§e/plot create - Automatically finds and creates a new plot for you.");
        player.sendMessage("§e/plot home - Teleports you to your plot.");
        player.sendMessage("§e/plot settings - Opens the settings menu for your plot.");
        player.sendMessage("§e/plot trust <player> - Allows a player to build on your plot.");
        player.sendMessage("§e/plot untrust <player> - Revokes build access from a player.");
        player.sendMessage("§e/plot visit <player> - Visits a public plot.");
        player.sendMessage("§e/plot list - Lists all public plots.");
        player.sendMessage("§e/plot leave - Deletes your plot.");
        player.sendMessage("§e/plot ban <player> - Bans a player from your plot.");
        player.sendMessage("§e/plot unban <player> - Unbans a player from your plot.");
        player.sendMessage("§e/plot invite <player> - Invites a player to your plot.");
        player.sendMessage("§a-----------------------------------");
    }

    private void handleVisitCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /plot visit <player>");
            return;
        }

        Plot plot = plotManager.getPlotByOwnerName(args[1]);
        if (plot == null || !plot.isPublic()) {
            player.sendMessage("§cCould not find a public plot for that player.");
            return;
        }

        org.bukkit.Location home = plotManager.getPlotHome(plot);
        if (home != null) {
            player.teleport(home);
            player.sendMessage("§aTeleporting you to " + args[1] + "'s plot...");
        } else {
            player.sendMessage("§cError finding that plot's location.");
        }
    }

    private void handleListCommand(Player player) {
        java.util.List<Plot> publicPlots = plotManager.getPublicPlots();
        if (publicPlots.isEmpty()) {
            player.sendMessage("§cThere are currently no public plots to visit.");
            return;
        }

        player.sendMessage("§a--- Public Plots ---");
        for (Plot plot : publicPlots) {
            org.bukkit.OfflinePlayer owner = player.getServer().getOfflinePlayer(plot.getOwner());
            player.sendMessage("§e- " + (owner.getName() != null ? owner.getName() : "Unknown") + "'s Plot (/plot visit " + owner.getName() + ")");
        }
        player.sendMessage("§a--------------------");
    }

    private void handleTrustCommand(Player player, String[] args, boolean trust) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /plot " + (trust ? "trust" : "untrust") + " <player>");
            return;
        }

        Plot plot = plotManager.getPlotByOwner(player.getUniqueId());
        if (plot == null) {
            player.sendMessage("§cYou do not own a plot.");
            return;
        }

        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer '" + args[1] + "' not found online.");
            return;
        }

        if (trust) {
            plot.addTrustedPlayer(target.getUniqueId());
            player.sendMessage("§a" + target.getName() + " can now build on your plot.");
            target.sendMessage("§aYou have been trusted to build on " + player.getName() + "'s plot.");
        } else {
            plot.removeTrustedPlayer(target.getUniqueId());
            player.sendMessage("§a" + target.getName() + " can no longer build on your plot.");
            target.sendMessage("§cYour build access has been revoked from " + player.getName() + "'s plot.");
        }
    }

    private void handleBanCommand(Player player, String[] args, boolean ban) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /plot " + (ban ? "ban" : "unban") + " <player>");
            return;
        }

        Plot plot = plotManager.getPlotByOwner(player.getUniqueId());
        if (plot == null) {
            player.sendMessage("§cYou do not own a plot.");
            return;
        }

        org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage("§cPlayer '" + args[1] + "' not found.");
            return;
        }

        if (ban) {
            plot.addBannedPlayer(target.getUniqueId());
            player.sendMessage("§a" + target.getName() + " has been banned from your plot.");
        } else {
            plot.removeBannedPlayer(target.getUniqueId());
            player.sendMessage("§a" + target.getName() + " has been unbanned from your plot.");
        }
    }

    private void handleLeaveCommand(Player player) {
        Plot plot = plotManager.getPlotByOwner(player.getUniqueId());
        if (plot == null) {
            player.sendMessage("§cYou do not own a plot to leave.");
            return;
        }

        plotManager.deletePlot(plot);
        player.sendMessage("§aYou have successfully left your plot. It is now available for others.");
        // Consider teleporting them to spawn
        if (player.getBedSpawnLocation() != null) {
            player.teleport(player.getBedSpawnLocation());
        } else {
            player.teleport(player.getWorld().getSpawnLocation());
        }
    }

    private void handleInviteCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /plot invite <player>");
            return;
        }

        Plot plot = plotManager.getPlotByOwner(player.getUniqueId());
        if (plot == null) {
            player.sendMessage("§cYou do not own a plot to invite someone to.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer '" + args[1] + "' is not online.");
            return;
        }

        pendingInvites.put(target.getUniqueId(), player.getUniqueId());

        TextComponent message = new TextComponent(player.getName() + " has invited you to their plot. Click here to accept!");
        message.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot accept"));
        target.spigot().sendMessage(message);

        player.sendMessage("§aInvite sent to " + target.getName() + ". It will expire in 120 seconds.");

        // Expire the invite after 120 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingInvites.containsKey(target.getUniqueId())) {
                pendingInvites.remove(target.getUniqueId());
                target.sendMessage("§cThe plot invite from " + player.getName() + " has expired.");
            }
        }, 2400L); // 120 seconds * 20 ticks/second
    }

    private void handleAcceptCommand(Player player) {
        if (!pendingInvites.containsKey(player.getUniqueId())) {
            player.sendMessage("§cYou don't have any pending plot invites.");
            return;
        }

        UUID inviterId = pendingInvites.remove(player.getUniqueId());
        Plot plot = plotManager.getPlotByOwner(inviterId);

        if (plot == null) {
            player.sendMessage("§cThe plot you were invited to no longer exists.");
            return;
        }

        plotManager.teleportHome(player, plot);
        player.sendMessage("§aTeleporting to the plot...");
    }
}
