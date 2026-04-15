package pluginsmc.langdua.core.paper.guis;

import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CinematicFrameGUI {

    private final Core instance;

    public CinematicFrameGUI(Core instance) {
        this.instance = instance;
    }

    public Inventory getCinematicFrameGUI(Player player, Cinematic cinematic, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, "Frames: " + cinematic.getName() + " - P" + page);

        List<Frame> frames = cinematic.getFrames();
        int itemsPerPage = 45;
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, frames.size());

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + "Frame " + i);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "World: " + ChatColor.WHITE + frames.get(i).getWorld());
                lore.add("");
                lore.add(ChatColor.GREEN + "Left-click to edit commands");
                lore.add(ChatColor.RED + "Right-click to delete frame");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(slot++, item);
        }

        // Pagination controls
        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Previous Page");
                prev.setItemMeta(meta);
            }
            gui.setItem(45, prev);
        }

        if (endIndex < frames.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Next Page");
                next.setItemMeta(meta);
            }
            gui.setItem(53, next);
        }

        // Visualize Button
        ItemStack viewPath = new ItemStack(Material.ENDER_EYE);
        ItemMeta viewMeta = viewPath.getItemMeta();
        if (viewMeta != null) {
            viewMeta.setDisplayName(ChatColor.AQUA + "Visualize Camera Path");
            List<String> viewLore = new ArrayList<>();
            viewLore.add(ChatColor.GRAY + "Spawns particles along the");
            viewLore.add(ChatColor.GRAY + "camera path for 10 seconds.");
            viewMeta.setLore(viewLore);
            viewPath.setItemMeta(viewMeta);
        }
        gui.setItem(49, viewPath);

        // Back Button
        ItemStack back = new ItemStack(Material.OAK_DOOR);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Back to Cinematics List");
            back.setItemMeta(backMeta);
        }
        gui.setItem(48, back);

        return gui;
    }
}