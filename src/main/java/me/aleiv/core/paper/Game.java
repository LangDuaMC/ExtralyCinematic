package me.aleiv.core.paper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.aleiv.core.paper.events.GameTickEvent;
import me.aleiv.core.paper.objects.Cinematic;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class Game extends BukkitRunnable {
    private final Core instance;

    private long gameTime = 0;
    private long startTime = 0;

    // Map of cinematic name to Cinematic object.  This map is mutated
    // through storage manager loading and command handlers.
    private HashMap<String, Cinematic> cinematics;
    // Set of players currently watching a cinematic.  Players are added
    // when they start playback and removed when playback finishes or
    // the player leaves the server.  This is used to detect if a
    // cinematic should continue updating a viewer.
    private Set<UUID> viewers;

    public Game(Core instance) {
        this.instance = instance;
        this.startTime = System.currentTimeMillis();
        cinematics = new HashMap<>();
        viewers = new HashSet<>();
    }

    @Override
    public void run() {
        var new_time = (int) (Math.floor((System.currentTimeMillis() - startTime) / 1000.0));
        gameTime = new_time;
        Bukkit.getPluginManager().callEvent(new GameTickEvent(new_time, true));
    }
}