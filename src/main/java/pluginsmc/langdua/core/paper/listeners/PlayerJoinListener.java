package pluginsmc.langdua.core.paper.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pluginsmc.langdua.core.paper.Core;

public class PlayerJoinListener implements Listener {
    private final Core instance;

    public PlayerJoinListener(Core instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            String autoPlay = instance.getConfig().getString("auto-play-on-first-join", "");
            if (autoPlay != null && !autoPlay.isEmpty() && instance.getGame().getCinematics().containsKey(autoPlay)) {
                instance.getServer().getScheduler().runTaskLater(instance, () -> {
                    if (event.getPlayer().isOnline()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cinematic play " + event.getPlayer().getName() + " " + autoPlay);
                    }
                }, 40L);
            }
        }
    }
}