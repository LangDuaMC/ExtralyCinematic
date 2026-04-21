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
        // Tăng lên 36 slots để bố cục rộng rãi, sang trọng hơn
        Inventory inv = Bukkit.createInventory(null, 36, "Cine: " + cinematic.getName());

        // --- HÀNG TÍNH NĂNG CHÍNH ---
        inv.setItem(10, createItem(Material.ITEM_FRAME, ChatColor.AQUA + "Edit Frames & Commands", ChatColor.GRAY + "Edit frame coordinates and commands."));
        inv.setItem(11, createItem(Material.ENDER_EYE, ChatColor.LIGHT_PURPLE + "Visualize Path", ChatColor.GRAY + "Preview the flight path with particles."));

        inv.setItem(12, createItem(Material.CLOCK, ChatColor.YELLOW + "Duration",
                ChatColor.GRAY + "Current: " + ChatColor.WHITE + cinematic.getDuration() + "s",
                ChatColor.GRAY + "Click to set cinematic duration."));

        inv.setItem(13, createItem(Material.JUKEBOX, ChatColor.GOLD + "Background Music (BGM)",
                ChatColor.GRAY + "Current: " + ChatColor.WHITE + (cinematic.getBgmSound() != null ? cinematic.getBgmSound() : "None"),
                ChatColor.GRAY + "Left Click: Set BGM", ChatColor.GRAY + "Right Click: Clear BGM"));

        inv.setItem(14, createItem(Material.SPYGLASS, ChatColor.DARK_AQUA + "Dolly Zoom (FOV)",
                ChatColor.GRAY + "Start: " + ChatColor.WHITE + cinematic.getStartZoom() + ChatColor.GRAY + " | End: " + ChatColor.WHITE + cinematic.getEndZoom(),
                ChatColor.GRAY + "Left Click: Set Start Zoom", ChatColor.GRAY + "Right Click: Set End Zoom"));

        inv.setItem(15, createItem(Material.BLAZE_POWDER, ChatColor.RED + "Camera Shake",
                ChatColor.GRAY + "Intensity: " + ChatColor.WHITE + cinematic.getShakeIntensity(),
                ChatColor.GRAY + "Click to type exact intensity."));

        inv.setItem(16, createItem(Material.COMPASS, ChatColor.GREEN + "Focus Target",
                ChatColor.GRAY + "Status: " + (cinematic.hasFocus() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                ChatColor.GRAY + "Click to toggle (Locks to your current location)."));

        // --- HÀNG ACTION ---
        inv.setItem(22, createItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Play Cinematic", ChatColor.GRAY + "Play this cinematic instantly."));
        inv.setItem(27, createItem(Material.OAK_DOOR, ChatColor.RED + "Back", ChatColor.GRAY + "Return to the main list."));
        inv.setItem(35, createItem(Material.BARRIER, ChatColor.DARK_RED + "Delete Cinematic", ChatColor.GRAY + "Click to delete (Cannot be undone)."));

        // Fill background với kính đen để tối ưu hóa click (Chống click vào ô trống gây lỗi)
        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, glass);
            }
        }

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