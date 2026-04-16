package pluginsmc.langdua.core.paper.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;

import java.util.Arrays;

public class CinematicDashboardGUI {
    private final Core instance;

    public CinematicDashboardGUI(Core instance) {
        this.instance = instance;
    }

    public Inventory getDashboardGUI(Cinematic cinematic) {
        Inventory inv = Bukkit.createInventory(null, 27, "Cine: " + cinematic.getName());

        inv.setItem(10, createItem(Material.ITEM_FRAME, ChatColor.AQUA + "Edit Frames & Commands", ChatColor.GRAY + "Edit frame coordinates and commands."));
        inv.setItem(11, createItem(Material.ENDER_EYE, ChatColor.LIGHT_PURPLE + "Visualize Path", ChatColor.GRAY + "Preview the flight path with particles."));
        inv.setItem(12, createItem(Material.COMPASS, ChatColor.YELLOW + "Focus Target",
                ChatColor.GRAY + "Status: " + (cinematic.hasFocus() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                ChatColor.GRAY + "Click to toggle (Locks to your current location)."));
        inv.setItem(13, createItem(Material.BLAZE_POWDER, ChatColor.GOLD + "Camera Shake",
                ChatColor.GRAY + "Intensity: " + cinematic.getShakeIntensity(),
                ChatColor.GRAY + "Left Click: +1", ChatColor.GRAY + "Right Click: -1"));
        inv.setItem(14, createItem(Material.SPYGLASS, ChatColor.DARK_AQUA + "Dolly Zoom (FOV)",
                ChatColor.GRAY + "Start: " + cinematic.getStartZoom() + " | End: " + cinematic.getEndZoom(),
                ChatColor.GRAY + "Use /cinematic zoom to configure."));
        inv.setItem(15, createItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Play Cinematic", ChatColor.GRAY + "Play this cinematic instantly."));
        inv.setItem(16, createItem(Material.BARRIER, ChatColor.RED + "Delete Cinematic", ChatColor.GRAY + "Click to delete (Cannot be undone)."));

        inv.setItem(26, createItem(Material.OAK_DOOR, ChatColor.RED + "Back", ChatColor.GRAY + "Return to the main list."));

        return inv;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}