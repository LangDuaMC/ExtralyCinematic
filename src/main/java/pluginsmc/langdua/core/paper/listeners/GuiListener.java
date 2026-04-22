package pluginsmc.langdua.core.paper.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.guis.*;

public class GuiListener implements Listener {

    private final Core instance;

    public GuiListener(Core instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        InventoryHolder holder = event.getClickedInventory().getHolder();

        if (holder instanceof CinematicDashboardGUI gui) {
            event.setCancelled(true);
            gui.handle(event);
        } else if (holder instanceof CinematicGUI gui) {
            event.setCancelled(true);
            gui.handle(event);
        } else if (holder instanceof CinematicTrackGUI gui) {
            event.setCancelled(true);
            gui.handle(event);
        } else if (holder instanceof CinematicFrameGUI gui) {
            event.setCancelled(true);
            gui.handle(event);
        } else if (holder instanceof CinematicTimelineGUI gui) {
            event.setCancelled(true);
            gui.handle(event);
        } else if (holder instanceof CommandEditorGUI gui) {
            event.setCancelled(true);
            gui.handle(event);
        }
    }
}