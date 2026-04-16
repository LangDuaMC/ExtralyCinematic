package pluginsmc.langdua.core.paper.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import pluginsmc.langdua.core.paper.Core;

public class GlobalListener implements Listener {

    private final Core instance;

    public GlobalListener(Core instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Chặn nút Shift (Ngồi) nếu người chơi đang xem Cinematic
        if (instance.getGame().getViewers().contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Tự động xóa người chơi khỏi danh sách người xem nếu họ thoát game đột ngột
        instance.getGame().getViewers().remove(event.getPlayer().getUniqueId());
    }
}