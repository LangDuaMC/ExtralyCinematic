package pluginsmc.langdua.core.paper.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.TimelineClip;
import pluginsmc.langdua.core.paper.objects.TransitionMetadata;

import java.util.ArrayList;
import java.util.List;

public class CinematicTimelineGUI {
    private final Core instance;

    public CinematicTimelineGUI(Core instance) {
        this.instance = instance;
    }

    public Inventory getTimelineGUI(Cinematic cinematic) {
        Inventory inv = Bukkit.createInventory(null, 54, "Timeline: " + cinematic.getName());

        List<TimelineClip> timeline = cinematic.getTimeline();
        for (int i = 0; i < timeline.size() && i < 45; i++) {
            inv.setItem(i, createClipItem(i, timeline.get(i)));
        }

        inv.setItem(45, createControlItem(Material.OAK_DOOR, ChatColor.RED + "Back", ChatColor.GRAY + "Return to tracks"));
        inv.setItem(46, createControlItem(Material.BARRIER, ChatColor.DARK_RED + "Reset Timeline", ChatColor.GRAY + "Keep only main track"));
        inv.setItem(47, createControlItem(Material.BLACK_DYE, ChatColor.DARK_GRAY + "Darken Preset", ChatColor.GRAY + "Left click clip to toggle darken"));
        inv.setItem(48, createControlItem(Material.COMPARATOR, ChatColor.YELLOW + "Set Transition", ChatColor.GRAY + "Right click clip to type ticks strength"));
        return inv;
    }

    private ItemStack createClipItem(int index, TimelineClip clip) {
        ItemStack item = new ItemStack(Material.REPEATER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            TransitionMetadata transition = clip.getTransition();
            meta.setDisplayName(ChatColor.AQUA + "Clip " + index + ChatColor.GRAY + " -> " + ChatColor.WHITE + clip.getTrackId());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Effect: " + ChatColor.WHITE + transition.getEffect().name());
            lore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + transition.getDurationTicks() + " ticks");
            lore.add(ChatColor.GRAY + "Strength: " + ChatColor.WHITE + transition.getStrength());
            lore.add("");
            lore.add(ChatColor.GREEN + "Left Click: Toggle NONE/DARKEN_FADE");
            lore.add(ChatColor.YELLOW + "Right Click: Set ticks and strength");
            lore.add(ChatColor.RED + "Shift Right: Remove clip");
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
