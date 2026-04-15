package me.aleiv.core.paper.guis;

import me.aleiv.core.paper.Core;
import me.aleiv.core.paper.objects.Cinematic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CinematicGUI {

    private Core instance;

    public CinematicGUI(Core instance) {
        this.instance = instance;
    }

    public Inventory getCinematicListGUI(Player player) {
        var cinematics = instance.getGame().getCinematics();
        int size = (int) (Math.ceil(cinematics.size() / 9.0) + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, "Cinematics");

        for (Cinematic cinematic : cinematics.values()) {
            ItemStack item = new ItemStack(Material.FILLED_MAP);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + cinematic.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Frames: " + ChatColor.WHITE + cinematic.getFrames().size());
            lore.add("");
            lore.add(ChatColor.GREEN + "Left-click to edit");
            lore.add(ChatColor.RED + "Right-click to delete");
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        return gui;
    }
}
