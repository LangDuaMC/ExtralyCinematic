package pluginsmc.langdua.core.paper.listeners;

import pluginsmc.langdua.core.paper.Core;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class GlobalListener implements Listener {

    private final Core instance;

    public GlobalListener(Core instance) {
        this.instance = instance;
    }

    /**
     * When a player leaves the server we ensure they are removed from the
     * active cinematic viewers set. This prevents cinematics from continuing
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