package me._2818.partyTS.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import me._2818.partyTS.party.PartyManager;
import me._2818.partyTS.party.PartyRaceManager;
import me.makkuusen.timing.system.api.events.driver.DriverFinishHeatEvent;
import me.makkuusen.timing.system.participant.DriverState;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.util.UUID;

public class PartyRaceListener implements Listener {
    private final PartyManager partyManager;
    private final Plugin plugin;
    private final PartyRaceManager partyRaceManager;

    public PartyRaceListener(PartyManager partyManager, Plugin plugin, PartyRaceManager partyRaceManager) {
        this.partyManager = partyManager;
        this.plugin = plugin;
        this.partyRaceManager = partyRaceManager;
    }

    @EventHandler
    public void onPlayerFinish(DriverFinishHeatEvent event) {
        long offlineCount = event.getDriver().getHeat().getDrivers().values().stream()
                .filter(driver -> driver.getTPlayer().getPlayer() == null && driver.getState() != DriverState.FINISHED)
                .count();

        if (event.getDriver().getPosition() == event.getDriver().getHeat().getDrivers().size() - offlineCount) {
            partyRaceManager.endPartyRace(event.getDriver().getHeat());
            return;
        }

        if (event.getDriver().getPosition() == 1) {
            for (UUID memberUUID : partyManager.getPlayerParty(event.getDriver().getTPlayer().getPlayer()).getMembers()) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member != null) {
                    member.sendMessage("§aA player finished the race, you have 30s to finish!");
                }
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                partyRaceManager.endPartyRace(event.getDriver().getHeat());
            }, 600L);
        }
    }
}
