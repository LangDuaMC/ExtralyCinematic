package pluginsmc.langdua.core.paper.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.guis.CinematicDashboardGUI;
import pluginsmc.langdua.core.paper.guis.CinematicFrameGUI;
import pluginsmc.langdua.core.paper.guis.CinematicGUI;
import pluginsmc.langdua.core.paper.guis.CommandEditorGUI;

public class GuiListener implements Listener {
    public GuiListener(Core instance) {
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof CinematicGUI cinematicGUI) {
            cinematicGUI.handle(event);
            return;
        }
        if (holder instanceof CinematicDashboardGUI dashboardGUI) {
            dashboardGUI.handle(event);
            return;
        }
        if (holder instanceof CinematicFrameGUI frameGUI) {
            frameGUI.handle(event);
            return;
        }
        if (holder instanceof CommandEditorGUI commandEditorGUI) {
            commandEditorGUI.handle(event);
        }
    }
}
