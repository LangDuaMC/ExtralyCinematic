package me.aleiv.core.paper.guis;

import me.aleiv.core.paper.Core;
import me.aleiv.core.paper.objects.Cinematic;
import me.aleiv.core.paper.objects.Frame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CinematicFrameGUI {

    private Core instance;
    private Cinematic cinematic;

    public CinematicFrameGUI(Core instance, Cinematic cinematic) {
        this.instance = instance;
        this.cinematic = cinematic;
    }

    public Inventory getGUI() {
        // We'll use a 54-slot inventory (6 rows) for frames and navigation/control buttons
        Inventory gui = Bukkit.createInventory(null, 54, "Editing: " + cinematic.getName());

        List<Frame> frames = cinematic.getFrames();
        for (int i = 0; i < frames.size() && i < 45; i++) { // Max 45 frames per page for now
            Frame frame = frames.get(i);
            ItemStack frameItem = new ItemStack(Material.PAPER);
            ItemMeta meta = frameItem.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Frame " + i);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "World: " + ChatColor.WHITE + frame.getWorld());
            lore.add(ChatColor.GRAY + "X: " + ChatColor.WHITE + String.format("%.2f", frame.getX()));
            lore.add(ChatColor.GRAY + "Y: " + ChatColor.WHITE + String.format("%.2f", frame.getY()));
            lore.add(ChatColor.GRAY + "Z: " + ChatColor.WHITE + String.format("%.2f", frame.getZ()));
            lore.add(ChatColor.GRAY + "Yaw: " + ChatColor.WHITE + String.format("%.2f", frame.getYaw()));
            lore.add(ChatColor.GRAY + "Pitch: " + ChatColor.WHITE + String.format("%.2f", frame.getPitch()));
            lore.add("");
            lore.add(ChatColor.GRAY + "Commands: " + ChatColor.WHITE + frame.getCommands().size());
            lore.add("");
            lore.add(ChatColor.GREEN + "Left-click to view/edit commands");
            lore.add(ChatColor.RED + "Right-click to delete frame");
            meta.setLore(lore);
            frameItem.setItemMeta(meta);
            gui.setItem(i, frameItem);
        }

        // Add navigation/control buttons (e.g., back button)
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back to Cinematics List");
        backButton.setItemMeta(backMeta);
        gui.setItem(48, backButton); // Example slot for back button

        return gui;
    }
}
