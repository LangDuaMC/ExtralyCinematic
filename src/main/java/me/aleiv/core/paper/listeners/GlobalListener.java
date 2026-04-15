package me.aleiv.core.paper.listeners;

import me.aleiv.core.paper.Core;
import me.aleiv.core.paper.events.GameTickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class GlobalListener implements Listener {

    Core instance;

    public GlobalListener(Core instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onGameTick(GameTickEvent e) {
        Bukkit.getScheduler().runTask(instance, () -> {

        });
    }

    /**
     * When a player leaves the server we ensure they are removed from the
     * active cinematic viewers set.  This prevents cinematics from continuing
     * to run for offline players and allows resources to be cleaned up early.
     *
     * @param event the quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        var game = instance.getGame();
        if (game.getViewers().contains(player.getUniqueId())) {
            game.getViewers().remove(player.getUniqueId());
        }
    }
}
