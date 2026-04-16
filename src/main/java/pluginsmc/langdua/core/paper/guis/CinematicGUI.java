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

public class CinematicGUI {
    private final Core instance;

    public CinematicGUI(Core instance) {
        this.instance = instance;
    }

    public Inventory getCinematicListGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Cinematics List");
        var cinematics = instance.getGame().getCinematics();

        for (Cinematic cine : cinematics.values()) {
            ItemStack item = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + cine.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Total Frames: " + cine.getFrames().size());
                lore.add(ChatColor.GRAY + "Focus: " + (cine.hasFocus() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
                lore.add(ChatColor.GRAY + "Shake: " + cine.getShakeIntensity());
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to open Dashboard");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.addItem(item);
        }
        return inv;
    }
}