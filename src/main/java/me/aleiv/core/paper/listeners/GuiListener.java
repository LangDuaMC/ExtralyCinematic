package me.aleiv.core.paper.listeners;

import me.aleiv.core.paper.Core;
import me.aleiv.core.paper.guis.CinematicFrameGUI;
import me.aleiv.core.paper.objects.Cinematic;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiListener implements Listener {

    private Core instance;

    public GuiListener(Core instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();

        if (title.equals("Cinematics")) {
            handleCinematicListClick(event, player);
        } else if (title.startsWith("Editing: ")) {
            handleFrameEditorClick(event, player, title);
        }
    }

    private void handleCinematicListClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        String cinematicName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        var game = instance.getGame();
        var cinematics = game.getCinematics();
        Cinematic cinematic = cinematics.get(cinematicName);

        if (cinematic == null) {
            player.sendMessage(ChatColor.RED + "Cinematic not found.");
            return;
        }

        if (event.getClick() == ClickType.RIGHT) {
            // Delete cinematic
            cinematics.remove(cinematicName);
            instance.getStorageManager().save(cinematics);
            player.sendMessage(ChatColor.GREEN + "Deleted cinematic: " + cinematicName);
            player.closeInventory();
            instance.getCommandManager().getCommand("cinematic").execute(player, "edit");

        } else if (event.getClick() == ClickType.LEFT) {
            // Open frame editor GUI
            CinematicFrameGUI frameGUI = new CinematicFrameGUI(instance, cinematic);
            player.openInventory(frameGUI.getGUI());
        }
    }

    private void handleFrameEditorClick(InventoryClickEvent event, Player player, String title) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        String cinematicName = title.substring("Editing: ".length());
        var game = instance.getGame();
        var cinematics = game.getCinematics();
        Cinematic cinematic = cinematics.get(cinematicName);

        if (cinematic == null) {
            player.sendMessage(ChatColor.RED + "Cinematic not found.");
            player.closeInventory();
            return;
        }

        // Handle back button
        if (event.getCurrentItem().getType() == Material.ARROW && event.getSlot() == 48) {
            player.closeInventory();
            instance.getCommandManager().getCommand("cinematic").execute(player, "edit");
            return;
        }

        // Handle frame deletion
        if (event.getClick() == ClickType.RIGHT) {
            int clickedSlot = event.getRawSlot();
            if (clickedSlot >= 0 && clickedSlot < cinematic.getFrames().size()) {
                cinematic.getFrames().remove(clickedSlot);
                instance.getStorageManager().save(cinematics);
                player.sendMessage(ChatColor.GREEN + "Frame " + clickedSlot + " deleted from cinematic " + cinematicName + ".");
                // Refresh the GUI
                player.openInventory(new CinematicFrameGUI(instance, cinematic).getGUI());
            } else {
                player.sendMessage(ChatColor.RED + "Invalid frame selected for deletion.");
            }
        } else if (event.getClick() == ClickType.LEFT) {
            // TODO: Open command editor GUI for the frame
            player.sendMessage(ChatColor.YELLOW + "Command editor for frames is not implemented yet.");
        }
    }
}
