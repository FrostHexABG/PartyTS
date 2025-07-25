package me._2818.partyTS.commands;

import me._2818.partyTS.party.Party;
import me._2818.partyTS.party.PartyManager;
import me._2818.partyTS.party.PartyRaceManager;
import me._2818.partyTS.utils.FontInfo;
import me.makkuusen.timing.system.api.TimingSystemAPI;
import me.makkuusen.timing.system.track.Track;
import me.makkuusen.timing.system.track.locations.TrackLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PartyCommand implements CommandExecutor {
    private final PartyManager partyManager;
    private final PartyRaceManager partyRaceManager;
    private final Plugin plugin;

    public PartyCommand(PartyManager partyManager, PartyRaceManager partyRaceManager, Plugin plugin) {
        this.partyManager = partyManager;
        this.partyRaceManager = partyRaceManager;
        this.plugin = plugin;
    }
    
    private TextColor getPrimaryColor() {
        return TextColor.fromHexString("#" + plugin.getConfig().getString("primarycolour", "0260C6"));
    }
    
    private TextColor getSecondaryColor() {
        return TextColor.fromHexString("#" + plugin.getConfig().getString("secondarycolour", "4cafff"));
    }
    
    private TextColor getDefaultColor() {
        return TextColor.fromHexString("#" + plugin.getConfig().getString("defaultcolour", "ffffff"));
    }
    
    private TextColor getAcceptColor() {
        return TextColor.fromHexString("#" + plugin.getConfig().getString("acceptcolour", "23ff00"));
    }
    
    private TextColor getDenyColor() {
        return TextColor.fromHexString("#" + plugin.getConfig().getString("denycolour", "ff0000"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party invite <player>");
                    return true;
                }
                handleInvite(player, args[1]);
                break;
            case "accept":
                handleAccept(player);
                break;
            case "decline":
                handleDecline(player);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party kick <player>");
                    return true;
                }
                if (args[1].equalsIgnoreCase(player.getName())) {
                    player.sendMessage("§cYou cannot kick yourself!");
                    return true;
                }
                handleKick(player, args[1]);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "list", "info":
                handleList(player);
                break;
            case "reload":
                if (!player.hasPermission("partyts.reload")) {
                    player.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                handleReload(player);
                break;
            case "race":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party race <track> [laps] [pits]");
                    return true;
                }
                handleRace(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        Component helpMessage = Component.text()
                .append(Component.text("=== ", getSecondaryColor()))
                .append(Component.text("Party Commands", getPrimaryColor()))
                .append(Component.text(" ===", getSecondaryColor()))
                .append(Component.newline())
                .append(Component.text("/party create ", getSecondaryColor()))
                .append(Component.text("- Create a new party", getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("/party invite ", getSecondaryColor()))
                .append(Component.text("- Invite a player to your party", getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("/party accept ", getSecondaryColor()))
                .append(Component.text("- Accept a party invite", getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("/party decline ", getSecondaryColor()))
                .append(Component.text("- Decline a party invite", getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("/party kick ", getSecondaryColor()))
                .append(Component.text("- Kick a player from your party", getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("/party leave ", getSecondaryColor()))
                .append(Component.text("- Leave your current party", getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("/party list ", getSecondaryColor()))
                .append(Component.text("- List party members", getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("/party info ", getSecondaryColor()))
                .append(Component.text("- List party members", getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("/party race ", getSecondaryColor()))
                .append(Component.text("- Start a party race", getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("/party reload ", getSecondaryColor()))
                .append(Component.text("- Reload the configuration", getDefaultColor()))
                .build();

        player.sendMessage(helpMessage);
    }

    private void handleCreate(Player player) {
        Party party = partyManager.createParty(player);
        if (party == null) {
            player.sendMessage("§cYou are already in a party!");
            return;
        }
        player.sendMessage("§aParty created successfully!");
    }

    private void handleInvite(Player player, String targetName) {
        Party party = partyManager.getPlayerParty(player);

        if (party == null) {
            party = partyManager.createParty(player);
            if (party == null) {
                player.sendMessage("§cFailed to create a party!");
                return;
            }
            player.sendMessage("§aParty created automatically!");
        }

        Player target = player.getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        if (partyManager.isOnInviteCooldown(player, target)) {
            player.sendMessage("§cYou must wait " + partyManager.getInviteCooldownSeconds(player, target) + " seconds before inviting that player again.");
            return;
        }

        if (partyManager.invitePlayer(party, target)) {
            player.sendMessage("§aInvite sent to " + target.getName() + "!");

            String acceptText = "[Accept]";
            String denyText = "[Decline]";
            String buttonSeparator = "  ";

            int line1Width = FontInfo.getStringWidth("You have been invited to join ", false)
                    + FontInfo.getStringWidth(player.getName(), false)
                    + FontInfo.getStringWidth("'s party!", false);

            int buttonsWidth = FontInfo.getStringWidth(acceptText, true)
                    + FontInfo.getStringWidth(buttonSeparator, false)
                    + FontInfo.getStringWidth(denyText, true);

            String expirationText = "This invite will expire in " + plugin.getConfig().getInt("invitetimeout", 30) + " seconds";
            int expirationWidth = FontInfo.getStringWidth(expirationText, false);

            int maxWidth = Math.max(line1Width, Math.max(buttonsWidth, expirationWidth));

            String buttonsPadding = FontInfo.getPadding(maxWidth, buttonsWidth);

            Component inviteMessage = Component.text()
                    .append(Component.text("You have been invited to join ", getPrimaryColor()))
                    .append(Component.text(player.getName(), getSecondaryColor()))
                    .append(Component.text("'s party!", getPrimaryColor()))
                    .append(Component.newline())
                    .append(Component.text(buttonsPadding))
                    .append(Component.text(acceptText, getAcceptColor(), TextDecoration.BOLD)
                            .clickEvent(ClickEvent.runCommand("/party accept")))
                    .append(Component.text(buttonSeparator, getDefaultColor()))
                    .append(Component.text(denyText, getDenyColor(), TextDecoration.BOLD)
                            .clickEvent(ClickEvent.runCommand("/party decline")))
                    .append(Component.newline())
                    .append(Component.text(expirationText, NamedTextColor.GRAY))
                    .build();
            target.sendMessage(inviteMessage);
        } else {
            if (partyManager.hasPendingInvite(target)) {
                player.sendMessage("§c" + target.getName() + " already has a pending invite!");
            } else {
                player.sendMessage("§c" + target.getName() + " is already in a party!");
            }
        }
    }

    private void handleAccept(Player player) {
        UUID inviterUUID = partyManager.getInviter(player);

        if (partyManager.acceptInvite(player)) {
            Party party = partyManager.getPlayerParty(player);
            Player inviter = inviterUUID != null ? Bukkit.getPlayer(inviterUUID) : null;

            player.sendMessage("§aYou have joined the party!");
            party.broadcastMessage("§a" + player.getName() + " has joined the party!");

            if (inviter != null && inviter.isOnline()) {
                inviter.sendMessage("§a" + player.getName() + " has accepted your party invite!");
            }
        } else {
            player.sendMessage("§cYou don't have any valid party invites!");
        }
    }

    private void handleDecline(Player player) {
        UUID inviterUUID = partyManager.getInviter(player);

        if (partyManager.declineInvite(player)) {
            Player inviter = inviterUUID != null ? Bukkit.getPlayer(inviterUUID) : null;

            player.sendMessage("§aYou have declined the party invite!");
            if (inviter != null && inviter.isOnline()) {
                inviter.sendMessage("§c" + player.getName() + " has declined your party invite!");
            }
        } else {
            player.sendMessage("§cYou don't have any valid party invites!");
        }
    }

    private void handleKick(Player player, String targetName) {
        Party party = partyManager.getPlayerParty(player);
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }

        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can kick players!");
            return;
        }

        Player target = player.getServer().getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        if (partyManager.kickPlayer(party, player, target)) {
            player.sendMessage("§aKicked " + target.getName() + " from the party!");
            target.sendMessage("§cYou have been kicked from the party!");
        } else {
            player.sendMessage("§cCould not kick " + target.getName() + " from the party!");
        }
    }

    private void handleLeave(Player player) {
        if (partyManager.leaveParty(player)) {
            player.sendMessage("§aYou have left the party!");
        } else {
            player.sendMessage("§cYou are not in a party!");
        }
    }

    private void handleList(Player player) {
        Party party = partyManager.getPlayerParty(player);
        if (party == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }

        Player leader = player.getServer().getPlayer(party.getLeader());

        ComponentBuilder<TextComponent, TextComponent.Builder> partyInfo = Component.text()
                .append(Component.text("=== ", getSecondaryColor()))
                .append(Component.text("Party Members", getPrimaryColor(), TextDecoration.BOLD))
                .append(Component.text(" ===", getSecondaryColor()))
                .append(Component.newline())
                .append(Component.text("Leader: " + leader.getName(), getDefaultColor()))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Members: " + party.getMembers().size(), getDefaultColor()))
                .append(Component.newline())
                .append(Component.text("\uD83D\uDC51 ", getSecondaryColor()))
                .append(Component.text(leader.getName(), getPrimaryColor()))
                .append(Component.newline());

        for (UUID memberUUID : party.getMembers()) {
            Player member = player.getServer().getPlayer(memberUUID);
            if (member.getName().equals(leader.getName())) continue;
            partyInfo.append(Component.text("- ", getSecondaryColor()))
                    .append(Component.text(member.getName(), getDefaultColor()))
                    .append(Component.newline());
        }

        partyInfo.append(Component.newline())
                .append(Component.text("[Invite]", getAcceptColor(), TextDecoration.BOLD)
                        .clickEvent(ClickEvent.suggestCommand("/party invite ")))
                .append(Component.text(" "))
                .append(Component.text("[Leave]", getDenyColor(), TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/party leave")));

        player.sendMessage(partyInfo.build());
    }

    private void handleReload(Player player) {
        try {
            plugin.reloadConfig();
            player.sendMessage("§aConfiguration reloaded successfully!");
        } catch (Exception e) {
            player.sendMessage("§cFailed to reload configuration: " + e.getMessage());
            plugin.getLogger().warning("Failed to reload configuration: " + e.getMessage());
        }
    }

    private void handleRace(Player player, String[] args) {
        Party playerParty = partyManager.getPlayerParty(player);

        if (playerParty == null) {
            player.sendMessage("§cYou are not in a party!");
            return;
        }

        if (!playerParty.getLeader().equals(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can start a race!");
            return;
        }

        var maybeDriver = TimingSystemAPI.getDriverFromRunningHeat(player.getUniqueId());

        if (maybeDriver.isPresent()) {
            player.sendMessage("§cYou are already in a heat!");
            return;
        }

        var possibleTrack = TimingSystemAPI.getTrack(args[1]);

        if (possibleTrack.isEmpty()) {
            player.sendMessage("§cTrack not found!");
            return;
        }

        Track t = possibleTrack.get();

        if (!t.isOpen()) {
            player.sendMessage("§cTrack is not open!");
            return;
        }

        if (t.getTrackLocations().getLocations(TrackLocation.Type.GRID).size() < playerParty.getMembers().size()) {
            player.sendMessage("§cNot enough grids!");
            return;
        }

        int laps = 3;

        if (args.length >= 3) {
            try {
                laps = Math.min(Integer.parseInt(args[2]), plugin.getConfig().getInt("maxlaps", 100));
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid number of laps!");
                return;
            }
        }

        int pits = 0;

        if (args.length >= 4) {
            try {
                pits = Math.min(Integer.parseInt(args[3]), Math.max(Math.abs(laps - 2), 0));
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid number of pits!");
                return;
            }
        }

        partyRaceManager.startPartyRace(player, t, laps, pits);
    }
}