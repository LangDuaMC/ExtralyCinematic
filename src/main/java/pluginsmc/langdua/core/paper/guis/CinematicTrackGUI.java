package pluginsmc.langdua.core.paper.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.CinematicTrack;

import java.util.ArrayList;
import java.util.List;

public class CinematicTrackGUI {
    private final Core instance;

    public CinematicTrackGUI(Core instance) {
        this.instance = instance;
    }

    public Inventory getTrackGUI(Cinematic cinematic) {
        Inventory inv = Bukkit.createInventory(null, 54, "Tracks: " + cinematic.getName());

        int slot = 0;
        for (CinematicTrack track : cinematic.getTracks().values()) {
            if (slot >= 45) {
                break;
            }
            inv.setItem(slot++, createTrackItem(cinematic, track));
        }

        inv.setItem(45, createControlItem(Material.OAK_DOOR, ChatColor.RED + "Back", ChatColor.GRAY + "Return to dashboard"));
        inv.setItem(46, createControlItem(Material.WRITABLE_BOOK, ChatColor.GREEN + "Create Track", ChatColor.GRAY + "Type new track id in chat"));
        inv.setItem(47, createControlItem(Material.CLOCK, ChatColor.YELLOW + "Set Track Duration", ChatColor.GRAY + "Select track then type duration"));
        inv.setItem(48, createControlItem(Material.REPEATER, ChatColor.AQUA + "Open Timeline", ChatColor.GRAY + "Manage clip order and transitions"));
        inv.setItem(49, createControlItem(Material.CHEST_MINECART, ChatColor.GOLD + "Append Main Track", ChatColor.GRAY + "Quick append primary track to timeline"));
        return inv;
    }

    private ItemStack createTrackItem(Cinematic cinematic, CinematicTrack track) {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + track.getId());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Frames: " + ChatColor.WHITE + track.getFrames().size());
            lore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + track.getDurationTicks() + " ticks");
            lore.add(ChatColor.GRAY + "Primary: " + (Cinematic.DEFAULT_TRACK_ID.equals(track.getId()) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
            lore.add("");
            lore.add(ChatColor.GREEN + "Left Click: Open track frames");
            lore.add(ChatColor.YELLOW + "Right Click: Append track to timeline");
            lore.add(ChatColor.AQUA + "Shift Left: Set as only timeline clip");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createControlItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
