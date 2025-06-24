package me._2818.partyTS.duels;

import lombok.Getter;
import me.makkuusen.timing.system.track.Track;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class DuelInvite {
    @Getter
    private final UUID challenger;
    @Getter
    private final UUID target;
    @Getter
    private final Track track;
    @Getter
    private final int laps;
    @Getter
    private final int pits;
    @Getter
    private final long timestamp;
    private final Plugin plugin;

    public DuelInvite(UUID challenger, UUID target, Track track, int laps, int pits, Plugin plugin) {
        this.challenger = challenger;
        this.target = target;
        this.track = track;
        this.laps = laps;
        this.pits = pits;
        this.timestamp = System.currentTimeMillis();
        this.plugin = plugin;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - timestamp) > (plugin.getConfig().getInt("duelinvitetimeout", 30) * 1000L);
    }
}
