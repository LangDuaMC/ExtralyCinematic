package pluginsmc.langdua.core.paper.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;

import java.util.ArrayList;
import java.util.List;

public class CinematicFrameGUI {
    private final Core instance;

    public CinematicFrameGUI(Core instance) {
        this.instance = instance;
    }

    public Inventory getCinematicFrameGUI(Player player, Cinematic cinematic, int page) {
        return getCinematicFrameGUI(player, cinematic, Cinematic.DEFAULT_TRACK_ID, page);
    }

    public Inventory getCinematicFrameGUI(Player player, Cinematic cinematic, String trackId, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "Frames: " + cinematic.getName() + " - T" + trackId + " - P" + page);

        int maxFramesPerPage = 45;
        List<pluginsmc.langdua.core.paper.objects.Frame> frames = cinematic.getOrCreateTrack(trackId).getFrames();
        int totalFrames = frames.size();
        int startIndex = (page - 1) * maxFramesPerPage;
        int endIndex = Math.min(startIndex + maxFramesPerPage, totalFrames);

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "Frame " + i);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Track: " + ChatColor.WHITE + trackId);
                lore.add(ChatColor.GRAY + "Commands: " + frames.get(i).getCommands().size());

                String title = frames.get(i).getTitle();
                if (title != null && !title.isEmpty()) {
                    lore.add(ChatColor.GRAY + "Title: " + ChatColor.translateAlternateColorCodes('&', title));
                }

                lore.add("");
                lore.add(ChatColor.GREEN + "Left Click: Teleport & Edit");
                lore.add(ChatColor.RED + "Right Click: Delete Frame");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i - startIndex, item);
        }
        if (page > 1) {
            inv.setItem(45, createControlItem(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        }
        if (endIndex < totalFrames) {
            inv.setItem(53, createControlItem(Material.ARROW, ChatColor.YELLOW + "Next Page"));
        }

        inv.setItem(49, createControlItem(Material.OAK_DOOR, ChatColor.RED + "Back", ChatColor.GRAY + "Return to Dashboard"));
        inv.setItem(48, createControlItem(Material.ENDER_EYE, ChatColor.LIGHT_PURPLE + "Visualize Path", ChatColor.GRAY + "Show path with particles"));
        inv.setItem(50, createControlItem(Material.REPEATER, ChatColor.AQUA + "Back To Tracks", ChatColor.GRAY + "Return to track manager"));

        return inv;
    }

    private ItemStack createControlItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
